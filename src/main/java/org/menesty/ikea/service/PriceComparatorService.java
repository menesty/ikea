package org.menesty.ikea.service;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.menesty.ikea.domain.ComparatorProductPrice;
import org.menesty.ikea.lib.domain.order.IkeaClientOrderItemDto;
import org.menesty.ikea.lib.domain.order.IkeaOrderDetail;
import org.menesty.ikea.lib.util.NumberUtil;
import org.menesty.ikea.lib.util.PageDownloadTask;
import org.menesty.ikea.service.xls.XlsExportService;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
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

  public List<ComparatorProductPrice> comparePrice(Set<String> artNumbers, List<String> sites) {
    final Map<String, IkeaSitePriceParser> parserMap = new HashMap<>();

    sites.stream().forEach(s -> {
      try {
        parserMap.put(s, getParser(s));
      } catch (RuntimeException e) {
        System.out.println(e.getMessage());
      }
    });


    return artNumbers.stream().map(artNumber -> {
      ComparatorProductPrice comparatorProductPrice = new ComparatorProductPrice(artNumber);

      try {
        TimeUnit.SECONDS.sleep(1);
        System.out.println(artNumber);

        parserMap.entrySet().stream().forEach(ikeaSitePriceParserEntry -> {
          BigDecimal price = ikeaSitePriceParserEntry.getValue().getPrice(artNumber);

          if ("ru".equals(ikeaSitePriceParserEntry.getKey())) {
            comparatorProductPrice.setPriceRu(price);
          } else if ("pl".equals(ikeaSitePriceParserEntry.getKey())) {
            comparatorProductPrice.setPricePl(price);
          } else if ("lt".equals(ikeaSitePriceParserEntry.getKey())) {
            comparatorProductPrice.setPriceLt(price);
          } else if ("ro".equals(ikeaSitePriceParserEntry.getKey())) {
            comparatorProductPrice.setPriceRo(price);
          }
        });
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      return comparatorProductPrice;
    }).collect(Collectors.toList());

  }

  private IkeaSitePriceParser getParser(String site) {
    if ("ru".equals(site)) {
      return new BaseIkeaSitePriceParser("http://www.ikea.com/ru/ru/catalog/products/");
    } else if ("pl".equals(site)) {
      return new BaseIkeaSitePriceParser("http://www.ikea.com/pl/pl/catalog/products/");
    } else if ("lt".equals(site)) {
      return new BaseIkeaSitePriceParser("http://www.ikea.com/lt/lt/catalog/products/");
    } else if ("ro".equals(site)) {
      return new BaseIkeaSitePriceParser("http://www.ikea.com/ro/ro/catalog/products/");
    }

    throw new RuntimeException("No Parser for site : " + site);
  }

  private Set<String> getOrderProducts(Long ikeaOrderProcessOrderId) throws Exception {
    APIRequest apiRequest = HttpServiceUtil.get("/ikea-order-detail/" + ikeaOrderProcessOrderId);

    IkeaOrderDetail ikeaOrderDetail = apiRequest.getData(IkeaOrderDetail.class);

    return ikeaOrderDetail.getIkeaClientOrderItemDtos()
        .stream()
        .map(IkeaClientOrderItemDto::getIkeaOrderItems)
        .map(ikeaOrderItems ->
                ikeaOrderItems.stream()
                    .map(ikeaOrderItem -> ikeaOrderItem.getProduct().getArtNumber())
                    .collect(Collectors.toList())
        )
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }


  public static void main(String... arg) throws Exception {
    PriceComparatorService service = new PriceComparatorService();


    List<ComparatorProductPrice> productPrices = service.comparePrice(150l, Arrays.asList("pl", "ru", "lt", "ro"));

    XlsExportService xlsExportService = new XlsExportService();

    xlsExportService.exportPriceComparator(new File("/Users/andrewhome/Documents/320.xls"), productPrices);
  }
}

interface IkeaSitePriceParser {
  BigDecimal getPrice(String artNumber);
}


class BaseIkeaSitePriceParser implements IkeaSitePriceParser {
  private final String siteUrl;

  public BaseIkeaSitePriceParser(String siteUrl) {
    this.siteUrl = siteUrl;
  }

  public BigDecimal getPrice(String artNumber) {
    try {
      Document document = new PageDownloadTask(siteUrl + artNumber).call();

      Elements ikeaPrice = document.select("span.ikeaFamilyPrice");
      Elements normalPrice = document.select("span.packagePrice");

      if (!ikeaPrice.isEmpty() && !StringUtils.isBlank(ikeaPrice.text())) {
        return NumberUtil.parseBigDecimal(ikeaPrice.text(), BigDecimal.ZERO);
      }

      if (!normalPrice.isEmpty()) {
        return NumberUtil.parseBigDecimal(normalPrice.text(), BigDecimal.ZERO);
      }

    } catch (Exception e) {
      System.out.println(e);
    }

    return BigDecimal.ZERO;
  }
}
