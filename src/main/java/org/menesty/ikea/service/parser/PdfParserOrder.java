package org.menesty.ikea.service.parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.text.PDFTextStripper;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.parse.RawItem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Menesty on
 * 9/8/15.
 * 17:39.
 */
public class PdfParserOrder {
  private static final Pattern LINK_ART_NUMBER_PATTERN = Pattern.compile("\\w{0,}\\d+");
  private Pattern LINE_PATTERN = Pattern.compile("(\\d+)\\s([0-9\\s]+,{0,1}\\d{0,2}).*?");
  private Pattern IKEA_FAMILY_PATTERN = Pattern.compile("(\\d+)\\s+Cena IKEA");
  private Pattern IKEA_FAMILY_PRICE_PATTERN = Pattern.compile("(\\d+,{0,1}\\d{0,2})\\s+PLN");
  private Pattern IKEA_FAMILY_TOTAL_AMOUNT = Pattern.compile("Suma IKEA FAMILY z VAT\\s(.*)\\s.*");
  private Pattern TOTAL_AMOUNT = Pattern.compile("Suma z VAT\\s(\\d+\\W{0,1}\\d+,{0,1}\\d+)\\s.*");

  public ParseResult parse(InputStream orderIs) {
    ParseResult result = new ParseResult();
    List<RawItem> rawOrderItems = new ArrayList<>();
    result.setRawOrderItems(rawOrderItems);

    BigDecimal totalAmount = null;

    try {
      PdfParseRawData pdfParseRawData = getDocumentContent(orderIs);
      Scanner scanner = new Scanner(pdfParseRawData.getContent());

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

                rawOrderItem = createRawOrderItem(pdfParseRawData.getArtNumber(artNumber), countStr, totalPriceStr);
              }

            } else {
              Matcher matcher = LINE_PATTERN.matcher(countPrice);

              if (matcher.find()) {
                String countStr = matcher.group(1);
                String totalPriceStr = matcher.group(2);

                rawOrderItem = createRawOrderItem(pdfParseRawData.getArtNumber(artNumber), countStr, totalPriceStr);
              }
            }
          } catch (RuntimeException e) {
            result.addParseWarning(I18n.UA.getString(I18nKeys.PARSING_ROW_EXCEPTION), e.getMessage());
          }

          if (rawOrderItem != null) {
            rawOrderItems.add(rawOrderItem);
          }
        } else {

          if (totalAmount == null) {
            Matcher orderTotalAmountMatcher = IKEA_FAMILY_TOTAL_AMOUNT.matcher(line);

            if (orderTotalAmountMatcher.find()) {
              totalAmount = new BigDecimal(orderTotalAmountMatcher.group(1).replaceAll("\\s+", "").replace(",", "."));
            }
          }

          if (totalAmount == null) {
            Matcher orderTotalAmountMatcher = TOTAL_AMOUNT.matcher(line);

            if (orderTotalAmountMatcher.find()) {
              totalAmount = new BigDecimal(orderTotalAmountMatcher.group(1).replaceAll("\\s+", "").replace(",", "."));
            }
          }
        }
      }
    } catch (IOException e) {
      result.setParseWarnings(Collections.singletonList(new ErrorMessage(I18n.UA.getString(I18nKeys.READ_FILE_EXCEPTION), e.getMessage())));
    }

    result.setTotalAmount(totalAmount);
    return result;
  }

  private RawItem createRawOrderItem(String artNumber, String count, String price) {
    if (StringUtils.isBlank(count) || StringUtils.isBlank(price)) {
      throw new RuntimeException(String.format("Cant parse count or price for artNumber : %s , current count %s, price %s", artNumber, count, price));
    }

    RawItem rawOrderItem = new RawItem();

    rawOrderItem.setArtNumber(artNumber);
    rawOrderItem.setCount(new BigDecimal(count));
    rawOrderItem.setCombo(artNumber.startsWith("S"));

    BigDecimal totalPrice = new BigDecimal(price.replaceAll("\\s", "").replace(",", "."));

    rawOrderItem.setPrice(totalPrice.divide(rawOrderItem.getCount(), 2, BigDecimal.ROUND_HALF_UP));

    return rawOrderItem;
  }

  private PdfParseRawData getDocumentContent(final InputStream stream) throws IOException {
    PDDocument p = PDDocument.load(stream);
    PDFTextStripper pdfTextStripper = new PDFTextStripper();
    String content = pdfTextStripper.getText(p);

    Set<String> artNumbers = extractLinks(p);
    p.close();

    return new PdfParseRawData(content, artNumbers);
  }

  private Set<String> extractLinks(PDDocument doc) throws IOException {
    Set<String> links = new HashSet<>();

    for (PDPage page : doc.getPages()) {

      for (PDAnnotation annot : page.getAnnotations()) {
        if (annot instanceof PDAnnotationLink) {
          PDAnnotationLink link = (PDAnnotationLink) annot;
          PDAction action = link.getAction();

          if (action instanceof PDActionURI) {
            PDActionURI uri = (PDActionURI) action;
            links.add(getArtNumber(uri.getURI()));
          }

        }
      }
    }

    return links;
  }

  private String getArtNumber(String artNumber) {
    Matcher m = LINK_ART_NUMBER_PATTERN.matcher(artNumber.replace(".", ""));

    if (m.find())
      return m.group().trim();

    return "";
  }

  class PdfParseRawData {
    private final String content;

    private final Set<String> artNumbers;

    public PdfParseRawData(String content, Set<String> artNumbers) {
      this.content = content;
      this.artNumbers = artNumbers;
    }

    public String getContent() {
      return content;
    }

    public String getArtNumber(String artNumber) {
      String preparedArtNumber = artNumber.replaceAll("\\.", "");

      if (!artNumbers.contains(preparedArtNumber) && artNumbers.contains("S" + preparedArtNumber)) {
        return "S" + preparedArtNumber;
      }

      return preparedArtNumber;
    }
  }

  public static void main(String... arg) throws FileNotFoundException {
    PdfParserOrder pdfParserOrder = new PdfParserOrder();

    ParseResult parseResult = pdfParserOrder.parse(new FileInputStream("/Users/andrewhome/Downloads/bogdan/BOGD 1-06.pdf"));
    parseResult.getRawOrderItems().stream().forEach(System.out::println);
    System.out.print(parseResult.getTotalAmount());
  }

}
