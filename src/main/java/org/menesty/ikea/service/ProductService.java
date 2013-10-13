package org.menesty.ikea.service;

import com.db4o.ObjectSet;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.menesty.ikea.domain.PackageInfo;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.domain.ProductPart;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductService {

    private static final String PRODUCT_DETAIL_URL = "http://www.ikea.com/pl/pl/catalog/products/";

    private static final String PRODUCT_AVAILABLE_URL = "http://www.ikea.com/pl/pl/iows/catalog/availability/";

    private static final String KATOWICE = "306";


    public ProductService() {

    }

    public ProductInfo loadOrCreate(String artNumber) {
        try {
            ProductInfo product = findByArtNumber(artNumber);

            if (product == null) {
                if (StringUtils.isNumeric(artNumber.charAt(0) + ""))
                    product = loadFromIkea(artNumber);
                else
                    product = loadComboProduct(artNumber);
                if (product == null)
                    return null;
                product.setOriginalArtNum(artNumber);
            }

            DatabaseService.get().store(product);
            return product;
        } catch (IOException e) {
            System.out.println("Problem with open product : " + artNumber);
            e.printStackTrace();
        }
        return null;
    }

    public ProductInfo findByArtNumber(String artNumber) {
        ProductInfo product = new ProductInfo();
        product.setOriginalArtNum(artNumber.replace(".", "").replace("-", ""));

        ObjectSet<ProductInfo> result = DatabaseService.get().queryByExample(product);
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
            DatabaseService.get().store(product);
            return product;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private ProductInfo loadFromIkea(String artNumber) throws IOException {
        String preparedArtNumber = artNumber.replaceAll("-", "").trim();
        String requestUrl = PRODUCT_DETAIL_URL + preparedArtNumber;
        Document doc = Jsoup.connect(requestUrl).get();

        String name = getProductName(doc);
        if (name == null) {
            System.out.println("Product not found on site " + artNumber + " " + requestUrl);
            return null;
        }

        ProductInfo productInfo = new ProductInfo();
        productInfo.setArtNumber(artNumber);
        productInfo.setOriginalArtNum(preparedArtNumber);
        productInfo.setPackageInfo(parseProductPackageInfo(doc));
        productInfo.setName(name);
        productInfo.setShortName(generateShortName(name, artNumber, productInfo.getPackageInfo().getBoxCount()));
        productInfo.setGroup(resolveGroup(preparedArtNumber));
        return productInfo;
    }

    private PackageInfo parseProductPackageInfo(Document document) {
        PackageInfo packageInfo = parsePartPackageInfo(document.html());
        packageInfo.setBoxCount(getBoxCount(document));
        if (!packageInfo.hasAllSize()) {
            String productSize = document.select("#measuresPart #metric").text();

            if (packageInfo.getHeight() == 0) {
                Matcher m = Patterns.HEIGHT_PATTERN.matcher(productSize);
                if (m.find())
                    packageInfo.setHeight((int) ((Double.valueOf(m.group(1)) * 10)));
            }

            if (packageInfo.getWidth() == 0) {
                Matcher m = Patterns.WIDTH_PATTERN.matcher(productSize);
                if (m.find())
                    packageInfo.setWidth((int) ((Double.valueOf(m.group(1)) * 10)));
            }

            if (packageInfo.getLength() == 0) {
                Matcher m = Patterns.LENGHT_PATTERN.matcher(productSize);
                if (m.find())
                    packageInfo.setLength((int) ((Double.valueOf(m.group(2)) * 10)));
            }

        }

        return packageInfo;
    }

    private String getProductName(Document doc) {
        Elements nameEl = doc.select("#type");
        String name = null;
        if (!nameEl.isEmpty())
            name = nameEl.get(0).textNodes().get(0).text().trim();
        return name;
    }

    private int getBoxCount(Document doc) {
        Elements numberOfPackages = doc.select("#numberOfPackages");
        int boxCount = 1;
        if (!numberOfPackages.isEmpty())
            boxCount = Integer.valueOf(numberOfPackages.text().trim());
        return boxCount;
    }

    private static ProductInfo.Group resolveGroup(String artNumber) throws IOException {
        Document doc = Jsoup.connect(PRODUCT_AVAILABLE_URL + artNumber).get();
        Elements elements = doc.select("localStore[buCode=" + KATOWICE + "] findIt type");
        if (!elements.isEmpty())
            if ("BOX_SHELF".equals(elements.get(0).text()))
                return ProductInfo.Group.Regal;
        //go to product page check breadCum
        doc = Jsoup.connect("http://www.ikea.com/pl/pl/catalog/products/" + artNumber).get();

        elements = doc.select("#packageInfo .texts");

        Matcher m = Patterns.WEIGHT_PATTERN.matcher(elements.text());
        if (m.find()) {
            if (Double.valueOf(m.group(1).replace(',', '.')) > 19)
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
        else if (breadCrumbs.contains("Poduszki") || breadCrumbs.contains("poszewki") || breadCrumbs.contains("Dywany") ||
                breadCrumbs.contains("Kołdry") || breadCrumbs.contains("Pościel") || breadCrumbs.contains("Narzuty") ||
                breadCrumbs.contains("Tkanina") || breadCrumbs.contains("Zasłony i rolety") || breadCrumbs.contains("Koce"))
            return ProductInfo.Group.Textile;

        else if (breadCrumbs.contains("kosz") ||
                breadCrumbs.contains("Kosze") ||
                breadCrumbs.contains("do montażu") ||
                breadCrumbs.contains("Akcesoria do przech") ||
                breadCrumbs.contains("Półka") ||
                breadCrumbs.contains("Organizatory") ||
                breadCrumbs.contains("Suszarki") ||
                breadCrumbs.contains("Sortowanie odpadów") ||
                breadCrumbs.contains("Podpórka") ||
                breadCrumbs.contains("Kosze") ||
                breadCrumbs.contains("Wspornik") ||
                breadCrumbs.contains("Wieszak") ||
                breadCrumbs.contains("wieszaki") ||
                breadCrumbs.contains("Akcesoria do czyszczenia") ||
                breadCrumbs.contains("Deski do prasowania") ||
                breadCrumbs.contains("Wkład do kosza") ||
                breadCrumbs.contains("Łyżka do butów") ||
                breadCrumbs.contains("Pojemnik na ubran")
                )
            return ProductInfo.Group.Storing;
        else if (breadCrumbs.contains("Bezpieczeństwo") || breadCrumbs.contains("IKEA FAMILY") || breadCrumbs.contains("ADG / Przechow") || breadCrumbs.contains("Podstawa pod laptopa"))
            return ProductInfo.Group.Family;
        return null;
    }

    public static void main(String... arg) throws IOException {
        //ProductInfo productInfo = new ProductService().loadComboProduct("S39002041");
        //System.out.println(productInfo);
        //System.out.println(productInfo.getParts());
        ProductInfo productInfo = new ProductService().loadComboProduct("S39002041");
        System.out.println(productInfo);
    }

    private String generateShortName(String name, String artNumber, int boxCount) {
        int shortNameLength = 28;
        String shortName = " " + artNumber;
        shortNameLength -= artNumber.length() + 1;
        if (boxCount > 1)
            shortNameLength -= 4;
        if (name.length() < shortNameLength)
            shortNameLength = name.length();
        shortName = shortNameLength != 0 ? name.substring(0, shortNameLength - 1) + shortName : shortName;
        return shortName;
    }

    private ProductInfo loadComboProduct(String artNumber) throws IOException {
        Document doc = Jsoup.connect(PRODUCT_DETAIL_URL + artNumber).get();

        String name = getProductName(doc);
        if (name == null) {
            System.out.println("Product not found on site " + artNumber + " " + PRODUCT_DETAIL_URL + artNumber);
            return null;
        }

        ProductInfo combo = new ProductInfo();
        combo.setArtNumber(artNumber);
        combo.setOriginalArtNum(artNumber);
        combo.setName(name);
        combo.setShortName(name);
        combo.setPackageInfo(parseProductPackageInfo(doc));

        List<ProductPart> parts = new ArrayList<>();
        String content = doc.html();

        Document rowDetailsDoc = Jsoup.connect("http://www.ikea.com/pl/pl/catalog/packagepopup/" + artNumber).get();
        Elements rows = rowDetailsDoc.select(".rowContainerPackage .colArticle");
        for (Element row : rows) {
            Matcher m = Patterns.ART_NUMBER_PART_PATTERN.matcher(row.html());
            if (m.find())
                parts.add(parsePartDetails(m.group(1).replace(".", "-"), content));
        }
        combo.setParts(parts);

        List<ProductPart> partsNoSize = new ArrayList<>();
        int partsBox = 0;
        for (ProductPart productPart : combo.getParts()) {
            if (!productPart.getProductInfo().getPackageInfo().hasAllSize())
                partsNoSize.add(productPart);
            else
                partsBox += productPart.getBoxCount();
        }
        int undefinedCount = combo.getPackageInfo().getBoxCount() - partsBox;
        if (undefinedCount > 0 && undefinedCount - partsNoSize.size() > 0) {
            for (ProductPart productInfo : partsNoSize) {

            }
        }

        return combo;
    }

    private ProductPart parsePartDetails(String artNumber, String content) {
        ProductPart productPart = new ProductPart();
        String originalArtNumber = artNumber.replaceAll("\\.|-", "");
        ProductInfo part = findByArtNumber(artNumber);
        if (part == null)
            try {
                part = loadFromIkea(artNumber);
            } catch (IOException e) {

            }

        Pattern p = Pattern.compile("metricPackageInfo.*\\{(.*?" + originalArtNumber + ".*?)\\}");
        Matcher m = p.matcher(content);

        PackageInfo packageInfo = new PackageInfo();
        if (m.find())
            packageInfo = parsePartPackageInfo(m.group(1));

        if (part == null) {
            part = new ProductInfo();

            part.setPackageInfo(packageInfo);
            part.setArtNumber(artNumber.replace(".", "-"));
            part.setOriginalArtNum(originalArtNumber);
            part.setName(parsePartName(originalArtNumber, content));
            part.setShortName(generateShortName(part.getName(), part.getArtNumber(), part.getPackageInfo().getBoxCount()));
            part.getPackageInfo().setBoxCount(1);
        }

        productPart.setCount(packageInfo.getBoxCount());
        productPart.setProductInfo(part);
        return productPart;
    }

    private String parsePartName(String artNumber, String content) {
        Pattern partNamePattern = Pattern.compile("\\{\"attachmentName\":\"([\\w\\sążźćńłśęóĄÅŻŹĆŃŁŚĘÓ]+)\",\"articleNumber\":\"" + artNumber + "\".*?\\}");
        Matcher m = partNamePattern.matcher(content);
        if (m.find())
            return m.group(1);
        return "";
    }

    private PackageInfo parsePartPackageInfo(String content) {
        PackageInfo packageInfo = new PackageInfo();

        Matcher m = Patterns.PACKAGE_INFO_PATTERN.matcher(content);
        if (m.find()) {
            System.out.println(m.group());
            packageInfo.setBoxCount(Integer.valueOf(m.group(1)));
            packageInfo.setLength(Integer.valueOf(m.group(2)));
            packageInfo.setWidth(Integer.valueOf(m.group(3)));
            packageInfo.setWeight(Integer.valueOf(m.group(4)));
            packageInfo.setHeight(Integer.valueOf(m.group(5)));
        }

        return packageInfo;
    }
}
