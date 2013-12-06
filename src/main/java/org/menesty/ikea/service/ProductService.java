package org.menesty.ikea.service;

import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.menesty.ikea.domain.PackageInfo;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.domain.ProductPart;
import org.menesty.ikea.exception.ProductFetchException;
import org.menesty.ikea.order.OrderItem;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.ui.controls.search.ProductItemSearchData;
import org.menesty.ikea.util.NumberUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
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
                if (Character.isDigit(artNumber.charAt(0)))
                    product = loadFromIkea(artNumber);
                else
                    product = loadComboProduct(artNumber);
                if (product == null)
                    return null;
            } else
                updateProductPrice(product);

            DatabaseService.get().store(product);
            return product;
        } catch (IOException e) {
            throw new ProductFetchException();
        }
    }

    private void updateProductPrice(ProductInfo productInfo) throws IOException {
        String requestUrl = PRODUCT_DETAIL_URL + productInfo.getOriginalArtNum();
        Document doc = Jsoup.connect(requestUrl).get();
        productInfo.setPrice(NumberUtil.parse(doc.select("#price1").text()));
    }

    public ProductInfo findByArtNumber(final String artNumber) {

        ProductInfo product = new ProductInfo();
        product.setOriginalArtNum(artNumber.replaceAll("\\D", ""));

        ObjectSet<ProductInfo> result = DatabaseService.get().query(new Predicate<ProductInfo>() {
            @Override
            public boolean match(ProductInfo productInfo) {
                if (artNumber.equals(productInfo.getArtNumber()) || artNumber.equals(productInfo.getOriginalArtNum()))
                    return true;
                return false;
            }
        });

        if (!result.isEmpty())
            return result.get(0);

        return null;
    }

    public ProductInfo getProductInfo(final RawInvoiceProductItem invoiceItem) {
        try {
            ProductInfo product = findByArtNumber(invoiceItem.getArtNumber());

            if (product == null) {
                product = loadFromIkea(invoiceItem.getPrepareArtNumber());
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
        productInfo.setGroup(resolveGroup(preparedArtNumber, doc));
        productInfo.setPrice(NumberUtil.parse(doc.select("#price1").text()));
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

    private ProductInfo.Group resolveGroup(String artNumber, Document productDetails) throws IOException {
        Document doc = Jsoup.connect(PRODUCT_AVAILABLE_URL + artNumber).get();
        Elements elements = doc.select("localStore[buCode=" + KATOWICE + "] findIt type");
        if (!elements.isEmpty())
            if ("BOX_SHELF".equals(elements.get(0).text()))
                return ProductInfo.Group.Regal;
        //go to product page check breadCum

        elements = productDetails.select("#packageInfo .texts");

        Matcher m = Patterns.WEIGHT_PATTERN.matcher(elements.text());
        double weight = 0;
        if (m.find()) {
            if ((weight = Double.valueOf(m.group(1).replace(',', '.'))) > 19)
                return ProductInfo.Group.Full;
        }

        String breadCrumbs = productDetails.select("#breadCrumbs").text();
        String content = productDetails.select(".rightContent").text();
        if (breadCrumbs.contains("dziec"))
            return ProductInfo.Group.Kids;
        else if (breadCrumbs.contains("Oświe") || breadCrumbs.contains("Lamp") || breadCrumbs.contains("Klosz") || breadCrumbs.contains("Kabl") || breadCrumbs.contains("Żarówk"))
            return ProductInfo.Group.Lights;
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
                breadCrumbs.contains("Pojemnik na ubran") || breadCrumbs.contains("Budzik") || breadCrumbs.contains("Okablowanie i akcesoria")
                )
            return ProductInfo.Group.Storing;
        else if (breadCrumbs.contains("Dekor") || breadCrumbs.contains("dekor") || breadCrumbs.contains("Karneciki") || breadCrumbs.contains("Lustra"))
            return ProductInfo.Group.Decor;

        else if (breadCrumbs.contains("Jedz") || breadCrumbs.contains("Gotow") || breadCrumbs.contains("kuch") || breadCrumbs.contains("Słoik")
                || breadCrumbs.contains("żywnoś") || breadCrumbs.contains("kieliszek") || breadCrumbs.contains("lodówkach i zamrażarkach") ||
                breadCrumbs.contains("tekstylia do jadalni ") || breadCrumbs.contains("ciast"))
            return ProductInfo.Group.Kitchen;
        else if (breadCrumbs.contains("łazienk") || breadCrumbs.contains("Łazienka"))
            return ProductInfo.Group.Bathroom;
        else if (breadCrumbs.contains("Poduszki") || breadCrumbs.contains("poszewki") || breadCrumbs.contains("Dywany") ||
                breadCrumbs.contains("Kołdry") || breadCrumbs.contains("Pościel") || breadCrumbs.contains("Narzuty") ||
                breadCrumbs.contains("Tkanin") || breadCrumbs.contains("Zasłony i rolety") || breadCrumbs.contains("Koce") || breadCrumbs.contains("Ochraniacze na materace"))
            return ProductInfo.Group.Textile;


        else if (breadCrumbs.contains("Bezpieczeństwo") || breadCrumbs.contains("IKEA FAMILY") || breadCrumbs.contains("AGD / Przechow") || breadCrumbs.contains("Rozwiązania mobilne") || breadCrumbs.contains("Rozwiązania na ścian"))
            return ProductInfo.Group.Family;
        else if (content.contains("dziec"))
            return ProductInfo.Group.Kids;
        else if (content.contains("Dekor") || content.contains("dekor"))
            return ProductInfo.Group.Decor;
        else if (weight != 0 && weight > 5) return ProductInfo.Group.Full;

        return ProductInfo.Group.Unknown;
    }

    public static String generateShortName(String name, String artNumber, int boxCount) {
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
        String preparedArtNumber = artNumber.replaceAll("-", "").trim();
        Document doc = Jsoup.connect(PRODUCT_DETAIL_URL + preparedArtNumber).get();
        String name = getProductName(doc);

        if (name == null) {
            System.out.println("Product not found on site " + preparedArtNumber + " " + PRODUCT_DETAIL_URL + preparedArtNumber);
            return null;
        }

        ProductInfo combo = new ProductInfo();
        combo.setArtNumber(artNumber);
        combo.setOriginalArtNum(preparedArtNumber);
        combo.setName(name);
        combo.setShortName(name);
        combo.setGroup(ProductInfo.Group.Combo);
        combo.setPackageInfo(parseProductPackageInfo(doc));
        combo.setPrice(NumberUtil.parse(doc.select("#price1").text()));

        List<ProductPart> parts = new ArrayList<>();
        String content = doc.html();

        Document rowDetailsDoc = Jsoup.connect("http://www.ikea.com/pl/pl/catalog/packagepopup/" + preparedArtNumber).get();
        Elements rows = rowDetailsDoc.select(".rowContainerPackage .colArticle");

        for (Element row : rows) {
            Matcher m = Patterns.ART_NUMBER_PART_PATTERN.matcher(row.html());

            if (m.find())
                parts.add(parsePartDetails(m.group(1).replace(".", "-"), content));
        }

        combo.setParts(parts);
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
            part.setName(parsePartName(part.getArtNumber(), content));
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

    public void save(ProductInfo productInfo) {
        DatabaseService.get().store(productInfo);
    }

    public List<ProductInfo> load(final ProductItemSearchData data) {
        return DatabaseService.get().query(new Predicate<ProductInfo>() {

            @Override
            public boolean match(ProductInfo productInfo) {
                if (data.artNumber != null && !productInfo.getArtNumber().contains(data.artNumber))
                    return false;

                if (data.productGroup != null && data.productGroup != productInfo.getGroup())
                    return false;

                return true;
            }
        });
    }

    public void export(String path) {
        export(load(new ProductItemSearchData()), path);
    }

    public void export(List<ProductInfo> items, String path) {
        StringBuffer text = new StringBuffer();
        String NL = System.getProperty("line.separator");
        char delimiter = '\t';
        for (ProductInfo item : items)
            if (item.isVerified() && item.getPackageInfo().hasAllSize()) {
                text.append(item.getArtNumber()).append(delimiter);
                text.append(item.getOriginalArtNum()).append(delimiter);
                text.append(item.getName()).append(delimiter);
                text.append(item.getShortName()).append(delimiter);
                text.append(item.getGroup()).append(delimiter);
                text.append(item.getPrice()).append(delimiter);
                text.append(item.getWat()).append(delimiter);

                text.append(item.getPackageInfo().getBoxCount()).append(delimiter);
                text.append(item.getPackageInfo().getWeight()).append(delimiter);
                text.append(item.getPackageInfo().getHeight()).append(delimiter);
                text.append(item.getPackageInfo().getLength()).append(delimiter);
                text.append(item.getPackageInfo().getWidth()).append(NL);
            }
        try (OutputStream os = Files.newOutputStream(FileSystems.getDefault().getPath(path), StandardOpenOption.CREATE_NEW)) {
            os.write(text.toString().getBytes("utf8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importProduct(String path) {
        try (Scanner scanner = new Scanner(Files.newInputStream(FileSystems.getDefault().getPath(path), StandardOpenOption.READ), "utf8")) {

            while (scanner.hasNextLine()) {
                StringTokenizer tokenizer = new StringTokenizer(scanner.nextLine(), "\t");
                if (tokenizer.countTokens() != 12)
                    continue;
                String artNumber = tokenizer.nextToken().trim();
                String originalArtNum = tokenizer.nextToken().trim();
                String name = tokenizer.nextToken().trim();
                String shortName = tokenizer.nextToken().trim();
                ProductInfo.Group group = ProductInfo.Group.valueOf(tokenizer.nextToken().trim());
                double price = getDouble(tokenizer.nextToken());
                int wat = getInt(tokenizer.nextToken());

                int boxCount = getInt(tokenizer.nextToken());
                int weight = getInt(tokenizer.nextToken());

                int height = getInt(tokenizer.nextToken());
                int length = getInt(tokenizer.nextToken());
                int width = getInt(tokenizer.nextToken());

                ProductInfo productInfo = findByArtNumber(artNumber);

                if (productInfo == null) {
                    productInfo = new ProductInfo();
                    productInfo.setArtNumber(artNumber);
                    productInfo.setOriginalArtNum(originalArtNum);
                }

                productInfo.setName(name);
                productInfo.setShortName(shortName);
                productInfo.setGroup(group);
                productInfo.setPrice(price);
                productInfo.setWat(wat);
                productInfo.getPackageInfo().setBoxCount(boxCount);
                productInfo.getPackageInfo().setWeight(weight);
                productInfo.getPackageInfo().setHeight(height);
                productInfo.getPackageInfo().setWidth(width);
                productInfo.getPackageInfo().setLength(length);

                save(productInfo);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getInt(String value) {
        return (int) getDouble(value);
    }

    private double getDouble(String value) {
        return NumberUtil.parse(value.trim());
    }

    public void save(OrderItem orderItem) {
        DatabaseService.get().store(orderItem);
    }
}
