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

public class ProductService {

    private static final ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "db/data.db");

    public  ProductService(){

    }

    public ProductInfo getProductInfo(final RawInvoiceProductItem invoiceItem) {
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
}
