package org.menesty.ikea.service;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.menesty.ikea.domain.ComparatorProductPrice;
import org.menesty.ikea.lib.domain.order.IkeaClientOrderItemDto;
import org.menesty.ikea.lib.domain.order.IkeaOrderDetail;
import org.menesty.ikea.lib.dto.IkeaOrderItem;
import org.menesty.ikea.lib.util.NumberUtil;
import org.menesty.ikea.lib.util.PageDownloadTask;
import org.menesty.ikea.service.xls.XlsExportService;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 6/13/16.
 * 13:08.
 */
public class PriceComparatorService {
  private final static Map<String, String> SITE_URLS = new HashMap<>();

  static {
    SITE_URLS.put("pl", "http://www.ikea.com/pl/pl");
    SITE_URLS.put("lt", "http://www.ikea.com/lt/lt");
    SITE_URLS.put("ru", "http://www.ikea.com/ru/ru");
  }

  public List<ComparatorProductPrice> comparePrice(Long orderId, List<String> sites) throws Exception {
    return comparePrice(getOrderProducts(orderId), sites);
  }

  public List<ComparatorProductPrice> comparePrice(Map<String, BigDecimal> artNumbers, List<String> sites) {
    final Map<String, IkeaSitePriceParser> parserMap = new HashMap<>();

    sites.stream().forEach(s -> {
      try {
        parserMap.put(s, getParser(s));
      } catch (RuntimeException e) {
        System.out.println(e.getMessage());
      }
    });


    return artNumbers.entrySet().stream().map(entry -> {
      ComparatorProductPrice comparatorProductPrice = new ComparatorProductPrice(entry.getKey(), entry.getValue());

      //TimeUnit.SECONDS.sleep(1);
      System.out.println(comparatorProductPrice.getArtNumber());

      parserMap.entrySet().stream().forEach(ikeaSitePriceParserEntry -> {
        BigDecimal price = ikeaSitePriceParserEntry.getValue().getPrice(entry.getKey());

        if ("ru".equals(ikeaSitePriceParserEntry.getKey())) {
          comparatorProductPrice.setPriceRu(price);
        } else if ("pl".equals(ikeaSitePriceParserEntry.getKey())) {
          comparatorProductPrice.setPricePl(price);
        } else if ("lt".equals(ikeaSitePriceParserEntry.getKey())) {
          comparatorProductPrice.setPriceLt(price);
        } else if ("ro".equals(ikeaSitePriceParserEntry.getKey())) {
          comparatorProductPrice.setPriceRo(price);
        } else if ("hu".equals(ikeaSitePriceParserEntry.getKey())) {
          comparatorProductPrice.setPriceHu(price);
        }

      });

      return comparatorProductPrice;
    }).collect(Collectors.toList());

  }

  private IkeaSitePriceParser getParser(String site) {
    if ("ru".equals(site)) {
      return new BaseIkeaSitePriceParser("http://www.ikea.com/ru/ru/catalog/products/", "");
    } else if ("pl".equals(site)) {
      return new BaseIkeaSitePriceParser("http://www.ikea.com/pl/pl/catalog/products/","");
    } else if ("lt".equals(site)) {
      return new BaseIkeaSitePriceParser("http://www.ikea.com/lt/lt/catalog/products/","");
    } else if ("ro".equals(site)) {
      return new BaseIkeaSitePriceParser("http://www.ikea.com/ro/ro/catalog/products/","");
    } else if ("hu".equals(site)) {
      return new BaseIkeaSitePriceParser("http://www.ikea.com/hu/hu/catalog/products/",".");
    }

    throw new RuntimeException("No Parser for site : " + site);
  }

  private Map<String, BigDecimal> getOrderProducts(Long ikeaOrderProcessOrderId) throws Exception {
    APIRequest apiRequest = HttpServiceUtil.get("/ikea-order-detail/" + ikeaOrderProcessOrderId);

    IkeaOrderDetail ikeaOrderDetail = apiRequest.getData(IkeaOrderDetail.class);

    return ikeaOrderDetail.getIkeaClientOrderItemDtos()
        .stream()
        .map(IkeaClientOrderItemDto::getIkeaOrderItems)
        .flatMap(Collection::stream)
        .collect(Collectors.groupingBy(ikeaOrderItem -> ikeaOrderItem.getProduct().getArtNumber(),
            Collectors.mapping(IkeaOrderItem::getCount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
  }


  public static void main(String... arg) throws Exception {
    PriceComparatorService service = new PriceComparatorService();


    List<ComparatorProductPrice> productPrices = service.comparePrice(167l, Arrays.asList("pl", "hu"));

    XlsExportService xlsExportService = new XlsExportService();

    xlsExportService.exportPriceComparator(new File("/Users/andrewhome/Documents/326.xls"), productPrices);
  }
}

interface IkeaSitePriceParser {
  BigDecimal getPrice(String artNumber);
}


class BaseIkeaSitePriceParser implements IkeaSitePriceParser {
  private final String siteUrl;
  private final String thousandSeparator;

  public BaseIkeaSitePriceParser(String siteUrl, String thousandSeparator) {
    this.siteUrl = siteUrl;
    this.thousandSeparator = thousandSeparator;
  }

  public BigDecimal getPrice(String artNumber) {
    try {
      Document document = new PageDownloadTask(siteUrl + artNumber).call();

      Elements ikeaPrice = document.select("span.ikeaFamilyPrice");
      Elements normalPrice = document.select("span.packagePrice");

      if (!ikeaPrice.isEmpty() && !StringUtils.isBlank(ikeaPrice.text())) {
        return NumberUtil.parseBigDecimal(ikeaPrice.text().replace(thousandSeparator, ""), BigDecimal.ZERO);
      }

      if (!normalPrice.isEmpty()) {
        return NumberUtil.parseBigDecimal(normalPrice.text().replace(thousandSeparator, ""), BigDecimal.ZERO);
      }

    } catch (Exception e) {
      System.out.println(e);
    }

    return BigDecimal.ZERO;
  }
}
