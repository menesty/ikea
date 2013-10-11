package org.menesty.ikea.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Menesty
 * Date: 10/6/13
 * Time: 11:42 AM
 */
public class PdfService {
    private final static Pattern linePattern = Pattern.compile("(\\d+) (\\d{3}-\\d{3}-\\d{2}) (.*) (SZT\\.) (\\d+) (\\S+) (\\d+,\\d+%) (\\S+,\\d{2})");
    private final static Pattern totalPattern = Pattern.compile("DO ZAPŁATY:(.*)");

    public static void main(String... arg) throws IOException {
        PdfService pdfService = new PdfService();

        String[] files = new String[]{"599 self.pdf", "600 tekstylia.pdf", "601 kuchnia.pdf", "602 kuchnia.pdf", "603 dekoracje.pdf", "604 famili.pdf", "606 full.pdf", "607 self.pdf", "608 oswitlenie.pdf",
                "610 self.pdf", "613 lazienka.pdf"};

        FileOutputStream fos = new FileOutputStream("data/pdf/result.csv");
        for (String fileName : files) {
            InvoicePdf invoicePdf = pdfService.parseInvoice(PdfService.class.getResourceAsStream("/pdf/" + fileName), fileName);

            for (RawInvoiceProductItem item : invoicePdf.getProducts()) {
                fos.write(item.toString().getBytes());
                fos.write(';');
                fos.write(fileName.getBytes());
                fos.write('\n');
            }

        }
        fos.close();

    }

    private void render(List<InvoicePdf> invoicePdfs) {

    }


    private InvoicePdf parseInvoice(InputStream stream, String name) throws IOException {
        InvoicePdf result = new InvoicePdf(name);

        PDDocument p = PDDocument.load(stream);
        PDFTextStripper t = new PDFTextStripper();


        String content = t.getText(p);

        Scanner scanner = new Scanner(content);


        List<RawInvoiceProductItem> products = new ArrayList<>();
        result.setProducts(products);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Matcher m = linePattern.matcher(line);
            if (m.find()) {
                System.out.println(m.group());
                RawInvoiceProductItem product = new RawInvoiceProductItem();
                product.setArtNumber(m.group(2));
                product.setOriginalArtNumber(m.group(2).replace("-", ""));
                product.setName(m.group(3));
                product.setCount(Integer.valueOf(m.group(5)));
                product.setPriceStr(m.group(6));
                product.setWat(m.group(7));
                products.add(product);
            }
        }

        Matcher m = totalPattern.matcher(content);

        if (m.find()) {
            double price = Double.valueOf(m.group(1).trim().replaceAll("[\\s\\u00A0]+", "").replace(",", "."));
            result.setPrice(price);
        }
        p.close();
        return result;

    }
}

class InvoicePdf {

    public InvoicePdf(String name) {
        setName(name);
    }

    private double price;

    private String name;

    private List<RawInvoiceProductItem> products;

    double getPrice() {
        return price;
    }

    void setPrice(double price) {
        this.price = price;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    List<RawInvoiceProductItem> getProducts() {
        return products;
    }

    void setProducts(List<RawInvoiceProductItem> products) {
        this.products = products;
    }
}
