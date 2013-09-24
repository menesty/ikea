package org.menesty.ikea.processor.invoice;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import net.sf.jxls.reader.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.menesty.ikea.domain.ProductInfo;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Menesty
 * Date: 9/23/13
 * Time: 10:59 PM
 */
public class InvoiceProcessor {


    public static void main(String... arg) throws IOException, SAXException, InvalidFormatException {
        InvoiceProcessor processor = new InvoiceProcessor();
        processor.loadFromIkea("002-287-05");

      /*  List<RawInvoiceProductItem> invoiceItems = processor.process();
        ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "db/data.db");

        for (RawInvoiceProductItem item : invoiceItems) {
            List<ProductInfo> product = processor.findByArtNumber(db, item.getArtNumber());
            if (product.isEmpty()) {

            }
        }*/
    }

    private List<ProductInfo> loadFromIkea(final RawInvoiceProductItem invoiceItem) throws IOException {
        String preparedArtNumber = invoiceItem.getArtNumber().replaceAll("-", "");
        Document doc = Jsoup.connect("http://www.ikea.com/pl/pl/catalog/products/" + preparedArtNumber).get();
        String name = doc.select("#type").get(0).textNodes().get(0).text().trim();
        Elements numberOfPackages = doc.select("#numberOfPackages");

        int boxCount = 1;
        if (!numberOfPackages.isEmpty())
            boxCount = Integer.valueOf(numberOfPackages.get(0).text().trim());
        List<ProductInfo> result = new ArrayList<>();
        for (int i = 1; i <= boxCount; i++) {
            ProductInfo productInfo = new ProductInfo();
            productInfo.setArtNumber(invoiceItem.getArtNumber());
            productInfo.setOriginalArtNum(invoiceItem.getOriginalArtNumber());
            productInfo.setPrice(invoiceItem.getPrice());

            result.add(productInfo);
        }

        return null;

    }

    public List<ProductInfo> findByArtNumber(ObjectContainer db, String artNumber) {
        ProductInfo product = new ProductInfo();
        product.setArtNumber(artNumber);
        ObjectSet<ProductInfo> result = db.queryByExample(product);
        return result;
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

        return rawProductItems;
    }

    /*private List<ProductInfo> reduce(final List<RawInvoiceProductItem> rawProductItems) {
        List<ProductInfo> reduce = new ArrayList<>();
        for (RawInvoiceProductItem item : rawProductItems) {
            if (item.getArtNumber() != null && item.getArtNumber() != null && item.getName() != null) {
                ProductInfo productInfo = new ProductInfo();
                productInfo.setOriginalArtNum(item.getOriginalArtNumber());
                productInfo.setArtNumber(item.getArtNumber());
                productInfo.setName(item.getName().replace(item.getNamePl(), ""));
                reduce.add(productInfo);
            }
        }
        return reduce;
    }*/
}
