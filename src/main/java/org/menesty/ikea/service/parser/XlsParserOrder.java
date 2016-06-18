package org.menesty.ikea.service.parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jxls.reader.*;
import org.menesty.ikea.domain.RawOrderItem;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.parse.RawItem;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 9/7/15.
 * 20:15.
 */
public class XlsParserOrder {
  private static final Pattern ART_NUMBER_PATTERN = Pattern.compile("\\w{0,}\\d+");

  @SuppressWarnings("unchecked")
  public ParseResult parse(InputStream orderIs, InputStream configIs) {
    ParseResult result = new ParseResult();
    List<ErrorMessage> warnings = new ArrayList<>();

    try {
      XLSReader mainReader = ReaderBuilder.buildFromXML(configIs);
      ReaderConfig.getInstance().setSkipErrors(true);

      List<RawOrderItem> rawOrderItems = new ArrayList<>();
      result.setParseWarnings(warnings);

      Map<String, Object> beans = new HashMap<>();
      beans.put("rawOrderItems", rawOrderItems);

      XLSReadStatus readStatus = mainReader.read(orderIs, beans);

      warnings.addAll(((List<XLSReadMessage>) readStatus.getReadMessages())
          .stream()
          .map(message -> new ErrorMessage(message.getMessage(), message.getException().getMessage()))
          .collect(Collectors.toList()));

      List<RawItem> items = rawOrderItems.stream().filter(rawOrderItem ->
              StringUtils.isNotBlank(rawOrderItem.getArtNumber()) && !rawOrderItem.getArtNumber().trim().toUpperCase().startsWith("K")
                  && rawOrderItem.getPrice() != null && rawOrderItem.getCount() != null
                  && getArtNumber(rawOrderItem.getArtNumber()).matches("\\w{0,1}\\d{8}")
      )
          .map(rawOrderItem -> {
            RawItem rawItem = new RawItem();

            rawItem.setArtNumber(getArtNumber(rawOrderItem.getArtNumber()));
            rawItem.setCombo(rawOrderItem.getCombo() != null && rawOrderItem.getCombo().trim().toLowerCase().equals("K"));
            rawItem.setComment(rawOrderItem.getComment());
            rawItem.setCount(BigDecimal.valueOf(rawOrderItem.getCount()));
            rawItem.setPrice(BigDecimal.valueOf(rawOrderItem.getPrice()).divide(rawItem.getCount(), 2, BigDecimal.ROUND_HALF_UP));

            return rawItem;
          }).collect(Collectors.toList());

      result.setRawOrderItems(items);
    } catch (SAXException e) {
      result.setParseWarnings(Collections.singletonList(new ErrorMessage(I18n.UA.getString(I18nKeys.INVALID_XML_CONFIGURATION), e.getMessage())));
    } catch (InvalidFormatException e) {
      result.setParseWarnings(Collections.singletonList(new ErrorMessage(I18n.UA.getString(I18nKeys.INVALID_XLS_FORMAT), e.getMessage())));
    } catch (IOException e) {
      result.setParseWarnings(Collections.singletonList(new ErrorMessage(I18n.UA.getString(I18nKeys.READ_FILE_EXCEPTION), e.getMessage())));
    } catch (Exception e) {
      result.setParseWarnings(Collections.singletonList(new ErrorMessage(I18n.UA.getString(I18nKeys.PARSING_FILE_EXCEPTION), e.getMessage())));
    }
    return result;
  }

  private String getArtNumber(String artNumber) {
    Matcher m = ART_NUMBER_PATTERN.matcher(artNumber.replace(".", ""));

    if (m.find())
      return m.group().trim();

    return "";
  }
}
