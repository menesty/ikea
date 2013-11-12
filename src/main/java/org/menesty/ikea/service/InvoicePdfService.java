package org.menesty.ikea.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.exception.ProductFetchException;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.util.NumberUtil;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Menesty
 * Date: 10/6/13
 * Time: 11:42 AM
 */
public class InvoicePdfService {

    private final static Pattern linePattern = Pattern.compile("(\\d+) (\\d{3}-\\d{3}-\\d{2}) (.*) (SZT\\.) (\\d{0,},{0,}\\d+) (\\S+) (\\d+,\\d+%) (\\S+,\\d{2})");

    private final static Pattern totalPattern = Pattern.compile("DO ZAPŁATY:(.*)");

    private ProductService productService;

    public InvoicePdfService() {
        productService = new ProductService();
    }

    public static void main(String... arg) throws IOException {

        System.out.print(Integer.valueOf("s001".replaceAll("\\D+", "")));
        /*InvoicePdfService pdfService = new InvoicePdfService();

        String[] files = new String[]{"306-S-12021.pdf", "306-S-12022.pdf", "306-S-12023.pdf", "599 self.pdf", "600 tekstylia.pdf", "601 kuchnia.pdf", "602 kuchnia.pdf", "603 dekoracje.pdf", "604 famili.pdf", "606 full.pdf", "607 self.pdf", "608 oswitlenie.pdf",
                "610 self.pdf", "613 lazienka.pdf"};

        FileOutputStream fos = new FileOutputStream("D:\\113faktur/result.csv");
        for (String fileName : files) {
            InvoicePdf invoicePdf = pdfService.parseInvoice(fileName, new FileInputStream("D:\\113faktur/" + fileName));

            for (RawInvoiceProductItem item : invoicePdf.getProducts()) {
                fos.write(item.toString().getBytes());
                fos.write(';');
                fos.write(fileName.getBytes());
                fos.write('\n');
            }

        }
        fos.close();

*/
        new InvoicePdfService().createInvoicePdf("test", new FileInputStream("/Users/Menesty/Downloads/600tekstylia.pdf"));
    }


    private InvoicePdf parseInvoice(String name, InputStream stream) throws IOException {
        InvoicePdf result = new InvoicePdf(name);

        PDDocument p = PDDocument.load(stream);

        PDFTextStripper t = new PDFTextStripper();
        String content = t.getText(p);
        p.close();

        Scanner scanner = new Scanner(content);

        List<RawInvoiceProductItem> products = new ArrayList<>();


        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Matcher m = linePattern.matcher(line);
            if (m.find()) {
                System.out.println(m.group());
                RawInvoiceProductItem product = new RawInvoiceProductItem();
                product.setArtNumber(m.group(2));
                product.setOriginalArtNumber(m.group(2).replace("-", ""));
                product.setName(m.group(3));
                product.setCount(NumberUtil.parse(m.group(5)));
                product.setPriceStr(m.group(6));
                product.setWat(m.group(7));
                product.setProductInfo(loadProductInfo(product));
                products.add(product);
            } else {
                System.out.println(line);
            }
        }

        Matcher m = totalPattern.matcher(content);

        if (m.find()) {
            double price = Double.valueOf(m.group(1).trim().replaceAll("[\\s\\u00A0]+", "").replace(",", "."));
            result.setPrice(price);
        }

        result.setProducts(reduce(products));

        return result;

    }

    private List<RawInvoiceProductItem> reduce(List<RawInvoiceProductItem> items) {
        Map<String, RawInvoiceProductItem> filtered = new HashMap<>();

        for (RawInvoiceProductItem item : items) {
            RawInvoiceProductItem current = filtered.get(item.getOriginalArtNumber());
            if (current == null)
                filtered.put(item.getOriginalArtNumber(), item);
            else
                current.setCount(NumberUtil.round(current.getCount() + item.getCount()));
        }
        return new ArrayList<>(filtered.values());

    }

    private ProductInfo loadProductInfo(RawInvoiceProductItem product) {
        ProductInfo productInfo = null;
        try {
            productInfo = productService.loadOrCreate(product.getArtNumber());
        } catch (ProductFetchException e) {
        }
        if (productInfo == null) {
            productInfo = new ProductInfo();
            productInfo.setArtNumber(product.getArtNumber());
            productInfo.setOriginalArtNum(product.getOriginalArtNumber());
            productInfo.setName(product.getName());
            productInfo.setShortName(ProductService.generateShortName(product.getName(), product.getArtNumber(), 1));
            productInfo.getPackageInfo().setBoxCount(1);
        }
        productInfo.setWat(product.getIntWat());
        productInfo.setPrice(product.getPrice());
        return productInfo;
    }

    public InvoicePdf createInvoicePdf(String orderName, InputStream is) {
        try {
            return parseInvoice(orderName, is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}


