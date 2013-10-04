package org.menesty.ikea.service;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductService {

    private static final ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "db/data.db");


    private static final String Katowice = "306";

    private static final Pattern WEIGHT_PATTERN = Pattern.compile("(\\d+,{1,}\\d+{1,})kg");

    public ProductService() {

    }

    public ProductInfo loadOrCreate(String artNumber) {
        try {
            ProductInfo product = findByArtNumber(artNumber);

            if (product == null) {
                product = loadFromIkea(artNumber);
                if (product == null)
                    return null;
                product.setOriginalArtNum(artNumber);
            }

            db.store(product);
            return product;
        } catch (IOException e) {
            System.out.println("Problem with open product : " + artNumber);
            e.printStackTrace();
        }
        return null;
    }

    public ProductInfo findByArtNumber(String artNumber) {
        ProductInfo product = new ProductInfo();
        product.setArtNumber(artNumber);

        ObjectSet<ProductInfo> result = db.queryByExample(product);
        if (!result.isEmpty())
            return result.get(0);

        return null;
    }

    public ProductInfo getProductInfo(final RawInvoiceProductItem invoiceItem) {
        try {
            ProductInfo product = findByArtNumber(invoiceItem.getArtNumber());

            if (product == null) {
                product = loadFromIkea(invoiceItem.getArtNumber());
                product.setOriginalArtNum(invoiceItem.getOriginalArtNumber());
            }

            product.setPrice(invoiceItem.getPrice());
            db.store(product);
            return product;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private ProductInfo loadFromIkea(String artNumber) throws IOException {
        String preparedArtNumber = artNumber.replaceAll("-", "").trim();
        String requestUrl = "http://www.ikea.com/pl/pl/catalog/products/" + preparedArtNumber;
        Document doc = Jsoup.connect(requestUrl).get();
        Elements nameEl = doc.select("#type");
        Elements numberOfPackages = doc.select("#numberOfPackages");
        String name;
        if (!nameEl.isEmpty())
            name = nameEl.get(0).textNodes().get(0).text().trim();
        else {
            System.out.println("Product not found on site " + artNumber + " " + requestUrl);
            return null;
        }
        int boxCount = 1;
        if (!numberOfPackages.isEmpty())
            boxCount = Integer.valueOf(numberOfPackages.get(0).text().trim());

        ProductInfo productInfo = new ProductInfo();
        productInfo.setArtNumber(artNumber);
        productInfo.setNumberBox(boxCount);
        productInfo.setName(name);
        productInfo.setShortName(generateShortName(name, artNumber, boxCount));
        productInfo.setGroup(resolveGroup(preparedArtNumber));
        return productInfo;
    }


    private static ProductInfo.Group resolveGroup(String artNumber) throws IOException {
        Document doc = Jsoup.connect("http://www.ikea.com/pl/pl/iows/catalog/availability/" + artNumber).get();
        Elements elements = doc.select("localStore[buCode=" + Katowice + "] findIt type");
        if (!elements.isEmpty())
            if ("BOX_SHELF".equals(elements.get(0).text()))
                return ProductInfo.Group.Regal;
        //go to product page check breadCum
        doc = Jsoup.connect("http://www.ikea.com/pl/pl/catalog/products/" + artNumber).get();

        elements = doc.select("#packageInfo .texts");

        Matcher m = WEIGHT_PATTERN.matcher(elements.text());
        if (m.find()) {
            if (Double.valueOf(m.group(1).replace(',', '.')) > 11)
                return ProductInfo.Group.Full;
        }

        String breadCrumbs = doc.select("#breadCrumbs").text();
        String content = doc.select(".rightContent").text();
        if (breadCrumbs.contains("dziec") || content.contains("dziec"))
            return ProductInfo.Group.Kids;
        else if (breadCrumbs.contains("Dekor") || breadCrumbs.contains("dekor"))
            return ProductInfo.Group.Decor;
        else if (breadCrumbs.contains("Oświe") || breadCrumbs.contains("Lamp") || breadCrumbs.contains("Klosz") || breadCrumbs.contains("Kabl"))
            return ProductInfo.Group.Lights;
        else if (breadCrumbs.contains("Jedz") || breadCrumbs.contains("Gotow") || breadCrumbs.contains("kuch") || breadCrumbs.contains("Słoik")
                || breadCrumbs.contains("żywnoś") || breadCrumbs.contains("kieliszek") || breadCrumbs.contains("lodówkach i zamrażarkach") ||
                breadCrumbs.contains("tekstylia do jadalni ") || breadCrumbs.contains("ciast"))
            return ProductInfo.Group.Kitchen;
        else if (breadCrumbs.contains("łazienk") || breadCrumbs.contains("Łazienka"))
            return ProductInfo.Group.Bathroom;
        else if (breadCrumbs.contains("Sypialnia") || breadCrumbs.contains("szycia") || breadCrumbs.contains("Zasłony i rolety") || breadCrumbs.contains("zasłon"))
            return ProductInfo.Group.Textil;
        return null;
    }

    public static void main(String... arg) throws IOException {
        System.out.println(ProductService.resolveGroup("00208024"));
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
}
