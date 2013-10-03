package org.menesty.ikea.processor.invoice;

import net.sf.jxls.reader.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.service.ProductService;
import org.mvel.integration.VariableResolverFactory;
import org.mvel.integration.impl.MapVariableResolverFactory;
import org.mvel.templates.TemplateRuntime;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * User: Menesty
 * Date: 9/23/13
 * Time: 10:59 PM
 */
public class InvoiceProcessor {


    public static void main(String... arg) throws IOException, SAXException, InvalidFormatException {
        new InvoiceProcessor().convert();
    }

    private void convert() throws IOException, InvalidFormatException, SAXException {
        InvoiceProcessor processor = new InvoiceProcessor();
        List<InvoiceItem> invoiceItems = new ArrayList<>();


        List<RawInvoiceProductItem> rawInvoiceItems = processor.process();
        ProductService productService = new ProductService();
        for (RawInvoiceProductItem item : rawInvoiceItems) {
            ProductInfo product = productService.getProductInfo(item);
            invoiceItems.addAll(InvoiceItem.get(product, item.getCount()));
        }

        int index = 0;
        for (InvoiceItem invoiceItem : invoiceItems)

            invoiceItem.setIndex(++index);


        Map<String, Object> map = new HashMap<>();
        map.put("invoiceItems", invoiceItems);
        VariableResolverFactory vrf = new MapVariableResolverFactory(map);

        String fileName = "D:\\development\\workspace\\ikea\\src\\main\\resources\\themes\\invoice\\invoice-order-2.epp";


        StringBuilder text = new StringBuilder();
        String NL = System.getProperty("line.separator");
        Scanner scanner = new Scanner(new FileInputStream(fileName), "ISO-8859-2");
        try {
            while (scanner.hasNextLine()) {
                text.append(scanner.nextLine() + NL);
            }
        } finally {
            scanner.close();
        }
        String template = new String(text.toString().getBytes("utf8"));

        // System.out.print(template);

        String s = (String) TemplateRuntime.eval(template, null, vrf, null);
        System.out.print(s);
        //OutputStreamWriter fos = new OutputStreamWriter (new FileOutputStream("D:\\development\\workspace\\ikea\\src\\main\\resources\\themes\\invoice/result.epp"),"ISO-8859-2");
        //fos.write(s);

        new FileOutputStream("D:\\development\\workspace\\ikea\\src\\main\\resources\\themes\\invoice/result.epp").write(s.getBytes("ISO-8859-2"));


        /*String bla = new String(s.getBytes("ISO-8859-2"));
        Latin2Writer  writer = new Latin2Writer(new OutputStreamWriter( new FileOutputStream("D:\\development\\workspace\\ikea\\src\\main\\resources\\themes\\invoice/result_lat.epp")),"cp1252");
        for(char c : bla.toCharArray())
        writer.write(c);*/

    }


    public List<RawInvoiceProductItem> process() throws IOException, SAXException, InvalidFormatException {
        InputStream inputXML = getClass().getResourceAsStream("/config/invoice-config.xml");
        XLSReader mainReader = ReaderBuilder.buildFromXML(inputXML);
        InputStream inputXLS = getClass().getResourceAsStream("/config/invoice.xlsx");

        ReaderConfig.getInstance().setUseDefaultValuesForPrimitiveTypes(true);

        List<RawInvoiceProductItem> rawProductItems = new ArrayList<>();
        Map<String, Object> beans = new HashMap<>();
        beans.put("rawProductItems", rawProductItems);

        XLSReadStatus readStatus = mainReader.read(inputXLS, beans);

        for (XLSReadMessage message : (List<XLSReadMessage>) readStatus.getReadMessages()) {
            System.out.println(message.getMessage());
        }

        return reduce(rawProductItems);
    }

    private List<RawInvoiceProductItem> reduce(final List<RawInvoiceProductItem> rawProductItems) {
        List<RawInvoiceProductItem> reduce = new ArrayList<>();
        for (RawInvoiceProductItem item : rawProductItems) {
            if (item.getArtNumber() == null || item.getPriceStr() == null) continue;
            reduce.add(item);
        }
        return reduce;
    }
}
