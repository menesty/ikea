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
import java.util.*;

/**
 * User: Menesty
 * Date: 9/23/13
 * Time: 10:59 PM
 */
public class InvoiceProcessor {


    public static void main(String... arg) throws IOException, SAXException, InvalidFormatException {
        /*StringBuilder text = new StringBuilder();
        String NL = System.getProperty("line.separator");
        Scanner scanner = new Scanner(new FileInputStream("db/result.epp"), "utf8");
        try {
            while (scanner.hasNextLine()) {
                text.append(scanner.nextLine() + NL);
            }
        } finally {
            scanner.close();
        }
        //System.out.println(text);
        OutputStream fos = new FileOutputStream("db/result2.epp");
        String bla = new String(text.toString().getBytes("cp1250"));
        fos.write(bla.getBytes());
        System.out.println(bla);
        for (char b : text.toString().toCharArray()) {
            switch (b) {
                case '\u0142':
                    fos.write(0xB3);
                    break;
                case '\u00F3':
                    fos.write(0xF3);
                    break;
                case '\u017A':
                    fos.write(0x9F);
                    break;
                case '\u017E':
                    fos.write(0x9E);
                    break;
                default:
                    fos.write(b);
            }

        }*/
        new InvoiceProcessor().convert();
    }

    private void convert() throws IOException, InvalidFormatException, SAXException {
        InvoiceProcessor processor = new InvoiceProcessor();
        List<InvoiceItem> invoiceItems = new ArrayList<>();


        List<RawInvoiceProductItem> rawInvoiceItems = processor.process();
        ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "db/data.db");

        for (RawInvoiceProductItem item : rawInvoiceItems) {
            ProductInfo product = processor.getProductInfo(db, item);
            invoiceItems.addAll(InvoiceItem.get(product, item.getCount()));
        }



       /* ProductInfo product1 = new ProductInfo();
        product1.setArtNumber("301-763-09");
        product1.setPrice(274.7500);
        product1.setNumberBox(1);
        product1.setName("Rama lozka z szufladami, brzoza, bialy");
        product1.setShortName("Rama lozka 301-763-09");
        invoiceItems.addAll(InvoiceItem.get(product1, 1));*/
        int index = 0;
        for (InvoiceItem invoiceItem : invoiceItems)

            invoiceItem.setIndex(++index);


        Map<String, Object> map = new HashMap<>();
        map.put("invoiceItems", invoiceItems);
        VariableResolverFactory vrf = new MapVariableResolverFactory(map);

        String fileName = "/Users/Menesty/development/workspace/ikea/src/main/resources/themes/invoice/invoice-order-2.epp";


        StringBuilder text = new StringBuilder();
        String NL = System.getProperty("line.separator");
        Scanner scanner = new Scanner(new FileInputStream(fileName), "cp1250");
        try {
            while (scanner.hasNextLine()) {
                text.append(scanner.nextLine() + NL);
            }
        } finally {
            scanner.close();
        }
        String template = new String(text.toString().getBytes("utf8"));

        //System.out.print(template);

        String s = (String) TemplateRuntime.eval(template, null, vrf, null);
        /*s = s.replace('\u0142', 'l');
        s = s.replace('\u00F3', 'o');
        s = s.replace('\u017B', 'Z');
        s = s.replace('\u017C', 'z');
        s = s.replace('\u0105', 'a');
        s = s.replace('\u0119', 'e');
        s = s.replace('\u0119', 'e');
        s = s.replace('\u015B', 's');*/

        System.out.print(s);
        FileOutputStream fos = new FileOutputStream("db/result.epp");
        fos.write(s.getBytes("cp1250"));
        fos.flush();
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

class UnicodeFormatter {

    static public String byteToHex(byte b) {
        // Returns hex String representation of byte b
        char hexDigit[] = {
                '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };
        char[] array = {hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f]};
        return new String(array);
    }

    static public String charToHex(char c) {
        // Returns hex String representation of char c
        byte hi = (byte) (c >>> 8);
        byte lo = (byte) (c & 0xff);
        return byteToHex(hi) + byteToHex(lo);
    }

}