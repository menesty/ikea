package org.menesty.ikea.order;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import net.sf.jxls.reader.*;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.xml.sax.SAXException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Menesty
 * Date: 9/21/13
 * Time: 4:01 PM
 */
public class DraftOrderProcessor {
    private static final Pattern artNumberPattern = Pattern.compile("\\w{0,}\\d+");

    public static void main(String... arg) throws IOException, SAXException, InvalidFormatException {

      //  DraftOrderProcessor draftOrderProcessor = new DraftOrderProcessor();
      //  draftOrderProcessor.process();
       /* URL location = DraftOrderProcessor.class.getProtectionDomain().getCodeSource().getLocation();
        System.out.println(location.getFile());
*/
        ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "db/data.db");
    }


    public void process() throws IOException, SAXException, InvalidFormatException {


        InputStream inputXML = getClass().getResourceAsStream("/config/config.xml");
        XLSReader mainReader = ReaderBuilder.buildFromXML(inputXML);
        InputStream inputXLS = getClass().getResourceAsStream("/config/ikea35_original.xlsx");
        ReaderConfig.getInstance().setUseDefaultValuesForPrimitiveTypes(true);
        List<RawOrderItem> rawOrderItems = new ArrayList<>();
        Map<String, Object> beans = new HashMap<>();
        beans.put("rawOrderItems", rawOrderItems);
        XLSReadStatus readStatus = mainReader.read(inputXLS, beans);

        for (XLSReadMessage message : (List<XLSReadMessage>) readStatus.getReadMessages()) {
            System.out.println(message.getMessage());
        }

        ReduceResult result = reduce(rawOrderItems);
        beans.clear();
        beans.put("orderItems", result.getGeneral());
        XLSTransformer transformer = new XLSTransformer();
        List<String> templateSheetNameList = Arrays.asList("factura", "combo", "color");
        List<String> sheetNameList = Arrays.asList("factura1", "combo1", "color1");
        List<Map<String, Object>> mapBeans = new ArrayList<>();

        double totalSum = 0d;


        {
            Map<String, Object> bean = new HashMap<>();
            bean.put("orderItems", result.getCombo());
            double total = getTotal(result.getGeneral());
            totalSum += totalSum;
            bean.put("total", total);
            mapBeans.add(bean);
        }

        {
            Map<String, Object> bean = new HashMap<>();
            bean.put("orderItems", result.getSeparate());
            double total = getTotal(result.getGeneral());
            totalSum += totalSum;
            bean.put("total", total);
            mapBeans.add(bean);
        }

        {
            Map<String, Object> bean = new HashMap<>();
            bean.put("orderItems", result.getGeneral());
            double total = getTotal(result.getGeneral());
            totalSum += totalSum;
            bean.put("total", total);
            bean.put("totalSum", totalSum);
            mapBeans.add(bean);
        }
        Workbook workbook = transformer.transformXLS(getClass().getResourceAsStream("/config/reduce.xlsx"), templateSheetNameList, sheetNameList, mapBeans);
        workbook.write(new FileOutputStream("D:/ikea/result.xlsx"));

    }

    private ReduceResult reduce(List<RawOrderItem> list) {
        Map<String, OrderItem> reduceGeneral = new HashMap<>();
        Map<String, OrderItem> reduceCombo = new HashMap<>();
        Map<String, OrderItem> reduceSeparate = new HashMap<>();

        for (RawOrderItem rawOrderItem : list) {
            if (rawOrderItem.getArtNumber() == null) continue;
            String artNumber = getArtNumber(rawOrderItem.getArtNumber());
            if (!artNumber.isEmpty()) {
                Map<String, OrderItem> current;
                if (rawOrderItem.getCombo() == null || rawOrderItem.getCombo().isEmpty()) {
                    if (rawOrderItem.getComment() == null || rawOrderItem.getComment().isEmpty())
                        current = reduceGeneral;
                    else
                        current = reduceSeparate;

                } else current = reduceCombo;

                OrderItem orderItem = current.get(artNumber);
                if (orderItem == null) {
                    orderItem = new OrderItem();
                    orderItem.setArtNumber(artNumber);
                    orderItem.setComment(rawOrderItem.getComment());
                    orderItem.setCount(rawOrderItem.getCount());
                    orderItem.setName(rawOrderItem.getDescription());
                    orderItem.setPrice(rawOrderItem.getPrice());
                    current.put(artNumber, orderItem);
                } else
                    orderItem.setCount(orderItem.getCount() + rawOrderItem.getCount());

            }

        }
        return new ReduceResult(new ArrayList<>(reduceGeneral.values()), new ArrayList<>(reduceCombo.values()), new ArrayList<>(reduceSeparate.values()));
    }

    private String getArtNumber(String artNumber) {
        Matcher m = artNumberPattern.matcher(artNumber);
        if (m.find())
            return m.group().trim();
        return "";
    }

    private double getTotal(List<OrderItem> orderItems) {
        double total = 0d;

        for (OrderItem item : orderItems) {
            total += item.getTotal();
        }
        return total;
    }

    private class ReduceResult {
        private List<OrderItem> general;

        private List<OrderItem> combo;

        private List<OrderItem> separate;

        private ReduceResult(List<OrderItem> general, List<OrderItem> combo, List<OrderItem> separate) {
            this.general = general;
            this.combo = combo;
            this.separate = separate;
        }


        public List<OrderItem> getGeneral() {
            return general;
        }

        public void setGeneral(List<OrderItem> general) {
            this.general = general;
        }

        public List<OrderItem> getCombo() {
            return combo;
        }

        public void setCombo(List<OrderItem> combo) {
            this.combo = combo;
        }

        public List<OrderItem> getSeparate() {
            return separate;
        }

        public void setSeparate(List<OrderItem> separate) {
            this.separate = separate;
        }
    }
}
