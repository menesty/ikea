package org.menesty.ikea.processor.invoice;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import net.sf.jxls.reader.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.menesty.ikea.domain.ProductInfo;
import org.mvel.integration.VariableResolverFactory;
import org.mvel.integration.impl.MapVariableResolverFactory;
import org.mvel.templates.TemplateRuntime;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;

/**
 * User: Menesty
 * Date: 9/23/13
 * Time: 10:59 PM
 */
public class InvoiceProcessor {


    public static void main(String... arg) throws IOException, SAXException, InvalidFormatException {
        InvoiceProcessor processor = new InvoiceProcessor();

        List<RawInvoiceProductItem> rawInvoiceItems = processor.process();
        ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "db/data.db");

        List<InvoiceItem> invoiceItems = new ArrayList<>();
        for (RawInvoiceProductItem item : rawInvoiceItems) {
            ProductInfo product = processor.getProductInfo(db, item);
            invoiceItems.addAll(InvoiceItem.get(product, item.getCount()));
        }
        int index = 0;
        for (InvoiceItem invoiceItem : invoiceItems)

            invoiceItem.setIndex(++index);


        Map<String, Object> map = new HashMap<>();
        map.put("invoiceItems", invoiceItems);
        VariableResolverFactory vrf = new MapVariableResolverFactory(map);

        String s = (String) TemplateRuntime.eval(processor.getClass().getResourceAsStream("/themes/invoice/invoice-order.chtml"),
                null, vrf, null);

        FileOutputStream fos = new FileOutputStream("/Users/Menesty/development/workspace/ikea/src/main/resources/themes/invoice/result.epp");
        fos.write(s.getBytes());
        fos.close();
        //System.out.println(s);

    }


    private ProductInfo loadFromIkea(final RawInvoiceProductItem invoiceItem) throws IOException {
        String preparedArtNumber = invoiceItem.getArtNumber().replaceAll("-", "").trim();
        Document doc = Jsoup.connect("http://www.ikea.com/pl/pl/catalog/products/" + preparedArtNumber).get();
        Elements nameEl = doc.select("#type");
        Elements numberOfPackages = doc.select("#numberOfPackages");
        String name;
        if (!nameEl.isEmpty())
            name = nameEl.get(0).textNodes().get(0).text().trim();
        else {
            throw new RuntimeException(invoiceItem.getArtNumber());
        }
        int boxCount = 1;
        if (!numberOfPackages.isEmpty())
            boxCount = Integer.valueOf(numberOfPackages.get(0).text().trim());

        ProductInfo productInfo = new ProductInfo();
        productInfo.setArtNumber(invoiceItem.getArtNumber());
        productInfo.setOriginalArtNum(invoiceItem.getOriginalArtNumber());
        productInfo.setPrice(invoiceItem.getPrice());
        productInfo.setNumberBox(boxCount);
        productInfo.setName(name);
        productInfo.setShortName(generateShortName(name, invoiceItem.getArtNumber(), boxCount));


        return productInfo;

    }

    private String generateShortName(String name, String artNumber, int boxCount) {
        int shortNameLength = 28;
        String shortName = " " + artNumber;
        shortNameLength -= artNumber.length() + 1;
        if (boxCount > 1)
            shortNameLength -= 4;
        if (name.length() < shortNameLength)
            shortNameLength = name.length();
        shortName = name.substring(0, shortNameLength - 1) + shortName;
        return shortName;
    }

    public ProductInfo getProductInfo(ObjectContainer db, final RawInvoiceProductItem invoiceItem) {
        try {
            ProductInfo product = new ProductInfo();
            product.setArtNumber(invoiceItem.getArtNumber());

            ObjectSet<ProductInfo> result = db.queryByExample(product);

            if (result.isEmpty())
                product = loadFromIkea(invoiceItem);
            else
                product = result.get(0);

            product.setPrice(invoiceItem.getPrice());
            db.store(product);
            return product;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
