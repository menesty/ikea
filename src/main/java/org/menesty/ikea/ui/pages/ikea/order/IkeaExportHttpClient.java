package org.menesty.ikea.ui.pages.ikea.order;

import org.apache.http.Header;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.menesty.ikea.domain.User;
import org.menesty.ikea.exception.LoginIkeaException;
import org.menesty.ikea.ui.pages.ikea.order.export.ExportCategory;
import org.menesty.ikea.util.NumberUtil;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Menesty on
 * 10/1/15.
 * 23:12.
 */
public class IkeaExportHttpClient implements Closeable {
  public final String ADD_PRODUCT_URL = "http://www.ikea.com/webapp/wcs/stores/servlet/IrwInterestItemAddByPartNumber";
  public final String LOGIN_URL = "https://secure.ikea.com/webapp/wcs/stores/servlet/Logon";
  public final String GET_CATEGORY_URL = "http://www.ikea.com/webapp/wcs/stores/servlet/InterestItemDisplay?storeId=19&langId=-27";
  public final String DET_CATEGORY_URL = "http://www.ikea.com/webapp/wcs/stores/servlet/IrwDeleteShoppingList?langId=-27&storeId=19&slId=";
  public final String LOGOUT_URL = "https://secure.ikea.com/webapp/wcs/stores/servlet/Logoff?langId=-27&storeId=19&rememberMe=false";
  public final String AVAILABILITY_URL = "http://www.ikea.com/pl/pl/iows/catalog/availability/";

  private final static Pattern LIST_ID_PATTERN = Pattern.compile("listId=(\\d+)");

  private CloseableHttpClient httpClient;

  public IkeaExportHttpClient() {
    RequestConfig rc = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
    httpClient = HttpClients.custom()
        .setDefaultRequestConfig(rc)
        .build();


  }

  @Override
  public void close() throws IOException {
    httpClient.close();
  }

  public String login(User user) throws LoginIkeaException {
    HttpUriRequest request = RequestBuilder.post()
        .setUri(LOGIN_URL)
        .addParameter("storeId", "19")
        .addParameter("langId", "-27")
        .addParameter("logonId", user.getLogin())
        .addParameter("logonPassword", user.getPassword()).build();

    String forwardUrl;

    try {
      try (CloseableHttpResponse response = httpClient.execute(request)) {
        Header[] locations = response.getHeaders("location");
        forwardUrl = locations[0].getValue();

        if (!forwardUrl.contains("MyProfile")) {
          throw new LoginIkeaException("Invalid login or password for user : " + user.getLogin());
        }
      }

      try (CloseableHttpResponse response = httpClient.execute(RequestBuilder.get(forwardUrl).build())) {
        EntityUtils.consume(response.getEntity());
      }
    } catch (IOException e) {
      throw new LoginIkeaException(e);
    }


    return forwardUrl;
  }

  public void logout() throws IOException {
    httpClient.execute(new HttpGet(LOGOUT_URL)).close();
  }

  public List<ExportCategory> createCategories(Collection<String> groups) throws IOException {
    for (String group : groups) {
      HttpGet request = new HttpGet("http://www.ikea.com/webapp/wcs/stores/servlet/IrwWSCreateInterestList?langId=-27&storeId=19&slName=" + group);

      try (CloseableHttpResponse response = httpClient.execute(request)) {
        EntityUtils.consume(response.getEntity());
      }
    }

    HttpGet request = new HttpGet("http://www.ikea.com/webapp/wcs/stores/servlet/InterestItemDisplay?storeId=19&langId=-27");
    List<ExportCategory> categories = new ArrayList<>();

    try (CloseableHttpResponse response = httpClient.execute(request)) {
      String html = EntityUtils.toString(response.getEntity());
      List<String> ids = getCategoriesIds(html);
      Document document = Jsoup.parse(html);

      for (String id : ids)
        try {
          String group = document.select("#listId" + id).text();
          categories.add(new ExportCategory(group, id));
        } catch (Exception e) {
          //skip
        }
    }

    return categories;
  }

  public void addProductToCategory(String categoryId, String artNumber, double quantity) throws IOException {
    HttpUriRequest request = RequestBuilder.post()
        .setUri(ADD_PRODUCT_URL)
        .addParameter("storeId", "19")
        .addParameter("langId", "-27")
        .addParameter("catalogId", "null")
        .addParameter("partNumber", artNumber)
        .addParameter("priceexclvat", "")
        .addParameter("listId", categoryId)
        .addParameter("quantity", NumberUtil.toString(quantity)).build();

    try (CloseableHttpResponse response = httpClient.execute(request)) {
      EntityUtils.consume(response.getEntity());
    }
  }

  public void deleteCategories() throws IOException {
    List<String> ids = getCategories();

    for (String id : ids) {
      deleteCategory(id);
    }
  }

  protected List<String> getCategories() throws IOException {
    HttpGet request = new HttpGet(GET_CATEGORY_URL);
    List<String> ids;


    try (CloseableHttpResponse response = httpClient.execute(request)) {
      ids = getCategoriesIds(EntityUtils.toString(response.getEntity()));
    }
    return ids;
  }

  private List<String> getCategoriesIds(String html) {
    List<String> result = new ArrayList<>();
    Document document = Jsoup.parse(html);
    Elements categories = document.select(".navigationBlock .navItem a");

    for (Element link : categories) {
      Matcher m = LIST_ID_PATTERN.matcher(link.attr("href"));

      if (m.find())
        result.add(m.group(1));
    }

    return result;
  }

  private void deleteCategory(String listId) throws IOException {
    HttpGet request = new HttpGet(DET_CATEGORY_URL + listId);

    try (CloseableHttpResponse response = httpClient.execute(request)) {
      EntityUtils.consume(response.getEntity());
    }
  }


  public Map<Integer, StockAvailability> checkAvailability(String artNumber) {
    int tryCount = 0;
    Map<Integer, StockAvailability> result = new HashMap<>();

    while (tryCount != 3) {
      try {
        Document document = Jsoup.connect(AVAILABILITY_URL + artNumber).get();
        Elements elements = document.select("availability localStore");

        for (Element element : elements) {
          try {
            StockAvailability stock = new StockAvailability();
            stock.setShopId(Integer.valueOf(element.attr("buCode")));

            Elements forcasts = element.select("forecasts forcast availableStock");

            if (forcasts.size() == 1) {
              stock.setAvailable(new BigDecimal(forcasts.get(0).html()));
            }

            if (forcasts.size() > 1) {
              stock.setAvailable2(new BigDecimal(forcasts.get(1).html()));
            }

            if (forcasts.size() > 2) {
              stock.setAvailable3(new BigDecimal(forcasts.get(2).html()));
            }

            result.put(stock.getShopId(), stock);
          } catch (NumberFormatException e) {
            //skip
          }
        }
        break;
      } catch (IOException e) {
        tryCount++;
      }
    }

    return result;
  }

}
