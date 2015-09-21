package org.menesty.ikea.service.parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;

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
 * 9/8/15.
 * 17:39.
 */
public class PdfParserOrder {
    private Pattern LINE_PATTERN = Pattern.compile("(\\d+)\\s+(\\d+,{0,1}\\d+).*?");
    private Pattern IKEA_FAMILY_PATTERN = Pattern.compile("(\\d+)\\s+Cena IKEA");
    private Pattern IKEA_FAMILY_PRICE_PATTERN = Pattern.compile("(\\d+,{0,1}\\d+)\\s+PLN");

    public ParseResult parse(InputStream orderIs) {
        ParseResult result = new ParseResult();
        List<RawItem> rawOrderItems = new ArrayList<>();
        result.setRawOrderItems(rawOrderItems);

        try {
            String content = getDocumentContent(orderIs);
            Scanner scanner = new Scanner(content);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.equals("Numer artykułu:")) {
                    String artNumber = scanner.nextLine();
                    String countPrice = scanner.nextLine();

                    RawItem rawOrderItem = null;
                    try {
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

                                rawOrderItem = createRawOrderItem(artNumber, countStr, totalPriceStr);
                            }

                        } else {
                            Matcher matcher = LINE_PATTERN.matcher(countPrice);

                            if (matcher.find()) {
                                String countStr = matcher.group(1);
                                String totalPriceStr = matcher.group(2);

                                rawOrderItem = createRawOrderItem(artNumber, countStr, totalPriceStr);
                            }
                        }
                    } catch (RuntimeException e) {
                        result.addParseWarning(I18n.UA.getString(I18nKeys.PARSING_ROW_EXCEPTION), e.getMessage());
                    }

                    if (rawOrderItem != null) {
                        rawOrderItems.add(rawOrderItem);
                    }
                }
            }
        } catch (IOException e) {
            result.setParseWarnings(Collections.singletonList(new ErrorMessage(I18n.UA.getString(I18nKeys.READ_FILE_EXCEPTION), e.getMessage())));
        }

        return result;
    }

    private RawItem createRawOrderItem(String artNumber, String count, String price) {
        if (StringUtils.isBlank(count) || StringUtils.isBlank(price)) {
            throw new RuntimeException(String.format("Cant parse count or price for artNumber : %s , current count %s, price %s", artNumber, count, price));
        }

        RawItem rawOrderItem = new RawItem();

        rawOrderItem.setArtNumber(artNumber);
        rawOrderItem.setCount(new BigDecimal(count));

        BigDecimal totalPrice = new BigDecimal(price.replace(",", "."));

        rawOrderItem.setPrice(totalPrice.divide(rawOrderItem.getCount(), 2, BigDecimal.ROUND_HALF_UP));

        return rawOrderItem;
    }

    private String getDocumentContent(final InputStream stream) throws IOException {
        PDDocument p = PDDocument.load(stream);
        PDFTextStripper t = new PDFTextStripper();

        String content = t.getText(p);

        p.close();

        return content;
    }
}
