package org.menesty.ikea.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.menesty.ikea.domain.RawOrderItem;
import org.menesty.ikea.lib.dto.ikea.JsonProduct;
import org.menesty.ikea.lib.util.DownloadProductTask;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Menesty on
 * 8/16/15.
 * 13:29.
 */
public class OrderPdfService {
    private Pattern LINE_PATTERN = Pattern.compile("(\\d+)\\s+(\\d+,{0,1}\\d+).*?");
    private Pattern IKEA_FAMILY_PATTERN = Pattern.compile("(\\d+)\\s+Cena IKEA");
    private Pattern IKEA_FAMILY_PRICE_PATTERN = Pattern.compile("(\\d+,{0,1}\\d+)\\s+PLN");

    private String parseDocument(final InputStream stream) throws IOException {
        PDDocument p = PDDocument.load(stream);
        PDFTextStripper t = new PDFTextStripper();

        String content = t.getText(p);

        p.close();

        return content;
    }

    public List<RawOrderItem> getRawOrderItems(final List<InputStream> streams) throws IOException {
        List<RawOrderItem> rawOrderItems = new ArrayList<>();

        streams.stream().forEach(stream -> {
                    if (stream == null) {
                        return;
                    }

                    try {
                        String content = parseDocument(stream);
                        Scanner scanner = new Scanner(content);

                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();

                            if (line.equals("Numer artykułu:")) {
                                String artNumber = scanner.nextLine();
                                String countPrice = scanner.nextLine();


                                System.out.println(countPrice);

                                if (countPrice.contains("Cena IKEA")) {
                                    Matcher matcher = IKEA_FAMILY_PATTERN.matcher(countPrice);

                                    if (matcher.find()) {
                                        String countStr = matcher.group(1);
                                        scanner.nextLine();//it should be FAMILY

                                        String priceStr = scanner.nextLine();

                                        matcher = IKEA_FAMILY_PRICE_PATTERN.matcher(priceStr);
                                        String totalPriceStr = null;

                                        if (matcher.find()) {
                                            totalPriceStr = matcher.group(1);
                                        }

                                        RawOrderItem rawOrderItem = createOrderItem(artNumber, countStr, totalPriceStr);
                                        updateProductInfo(rawOrderItem);

                                        rawOrderItems.add(rawOrderItem);
                                    }

                                } else {
                                    Matcher matcher = LINE_PATTERN.matcher(countPrice);

                                     if (matcher.find()) {
                                        String countStr = matcher.group(1);
                                        String totalPriceStr = matcher.group(2);

                                        RawOrderItem rawOrderItem = createOrderItem(artNumber, countStr, totalPriceStr);

                                        updateProductInfo(rawOrderItem);
                                        rawOrderItems.add(rawOrderItem);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

        );

        return rawOrderItems;
    }

    private RawOrderItem createOrderItem(String artNumber, String count, String price) {
        RawOrderItem rawOrderItem = new RawOrderItem();

        rawOrderItem.setArtNumber(artNumber);
        rawOrderItem.setCount(Double.parseDouble(count));

        if (price != null) {
            BigDecimal totalPrice = new BigDecimal(price.replace(",", "."));

            rawOrderItem.setPrice(totalPrice.divide(BigDecimal.valueOf(rawOrderItem.getCount()), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
        }

        return rawOrderItem;
    }

    private void updateProductInfo(RawOrderItem rawOrderItem) {
        try {
            DownloadProductTask downloadProductTask = new DownloadProductTask(rawOrderItem.getArtNumber(), 3000);
            JsonProduct jsonProduct = downloadProductTask.call();
            rawOrderItem.setArtNumber(jsonProduct.getActiveArtNumber());
            rawOrderItem.setDescription(jsonProduct.getActiveProductItem().getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String... arg) throws IOException {
        OrderPdfService orderPdfService = new OrderPdfService();
        List<RawOrderItem> items = orderPdfService.getRawOrderItems(Collections.singletonList(new FileInputStream("/Users/andrewhome/Downloads/Dokup_201.pdf")));
        System.out.println(items.size());
    }
}
