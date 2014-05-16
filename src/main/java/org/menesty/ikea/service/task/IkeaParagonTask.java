package org.menesty.ikea.service.task;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.concurrent.Task;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.menesty.ikea.ApplicationPreference;
import org.menesty.ikea.db.DatabaseService;
import org.menesty.ikea.domain.IkeaParagon;
import org.menesty.ikea.service.ServiceFacade;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Menesty on 5/14/14.
 */
public class IkeaParagonTask extends Task<Boolean> {
    private SimpleDateFormat sdf;

    private static final Logger logger = Logger.getLogger(IkeaParagonTask.class.getName());

    private Gson gson;

    private Type collectionType;

    public IkeaParagonTask() {
        sdf = new SimpleDateFormat("dd.MM.yyyy");
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ProductDto.class, new ProductDtoAdapter());

        gson = gsonBuilder.create();

        collectionType = new TypeToken<Collection<ProductDto>>() {
        }.getType();
    }

    @Override
    protected Boolean call() throws Exception {
        boolean result = true;
        try {
            DatabaseService.begin();

            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setUserAgent("Mozilla/5.0 (Macintosh; U; PPC Max OS X Mach-O; en-US; rv:1.8.0.7) Gecko/200609211 Camino/1.0.3")
                    .build()) {

                if (login(httpClient))
                    start(httpClient);
                else
                    result = false;

            }
            DatabaseService.commit();
        } catch (Exception e) {
            DatabaseService.rollback();
            result = false;

            logger.log(Level.SEVERE, "Ikea family parse site problem", e);
        }

        return result;
    }

    private void start(CloseableHttpClient httpClient) throws IOException {
        HttpUriRequest firstPage = RequestBuilder.get().setUri("https://www.ikeafamily.eu/Moje-zakupy.aspx").build();

        int pageCount = 0;
        boolean nextPage;
        Map<String, String> viewState = null;

        try (CloseableHttpResponse response = httpClient.execute(firstPage)) {
            Document document = Jsoup.parse(EntityUtils.toString(response.getEntity()));

            if (nextPage = parsePage(httpClient, document)) {
                pageCount = getPageCount(document);
                viewState = getViewState(document);
            }
        }

        int currentPage = 2;

        while (nextPage && currentPage <= pageCount) {
            RequestBuilder rb = RequestBuilder.post().setUri("https://www.ikeafamily.eu/Moje-zakupy.aspx");

            for (Map.Entry<String, String> entry : viewState.entrySet())
                rb.addParameter(entry.getKey(), entry.getValue());

            rb.addParameter("__EVENTARGUMENT", currentPage + "");

            try (CloseableHttpResponse response = httpClient.execute(rb.build())) {
                Document document = Jsoup.parse(EntityUtils.toString(response.getEntity()));
                nextPage = parsePage(httpClient, document);

                if (pageCount != currentPage)
                    viewState = getViewState(document);
            }

            currentPage++;
        }
    }

    private Map<String, String> getViewState(Document document) {
        Map<String, String> map = new HashMap<>();

        map.put("__VIEWSTATE", document.select("#__VIEWSTATE").get(0).val());
        map.put("__VIEWSTATE1", document.select("#__VIEWSTATE1").get(0).val());
        map.put("__VIEWSTATE2", document.select("#__VIEWSTATE2").get(0).val());
        map.put("__VIEWSTATE3", document.select("#__VIEWSTATE3").get(0).val());
        map.put("__VIEWSTATE4", document.select("#__VIEWSTATE4").get(0).val());
        map.put("__VIEWSTATEFIELDCOUNT", "5");
        map.put("__EVENTTARGET", "p$lt$ctl01$PagePlaceHolder$p$lt$ctl00$ListaZakupow$pagerPurchaseList");
        map.put("lng", "pl-PL");
        map.put("query", "");
        map.put("manScript_HiddenField", "");

        return map;
    }

    private int getPageCount(Document document) {
        Elements pages = document.select("span.pages a.page");

        if (pages.size() > 0)
            try {
                return Integer.valueOf(pages.get(pages.size() - 1).html());
            } catch (NumberFormatException e) {
                return 1;
            }
        return 1;
    }

    private boolean parsePage(CloseableHttpClient httpClient, Document document) throws IOException {
        Elements rows = document.select(".productsList .purchases-table tr.main-row");

        for (Element row : rows) {
            Elements columns = row.select("td");

            if (columns.size() == 6) {
                try {
                    Date date = sdf.parse(columns.get(0).text());
                    String paragonName = columns.get(1).text();
                    String shopName = columns.get(3).text();
                    String payment = columns.get(4).text();

                    //check if exist in database
                    IkeaParagon paragon = ServiceFacade.getIkeaParagonService().findByName(paragonName);

                    if (paragon == null || !paragon.getCreatedDate().equals(date)) {
                        //execute import
                        Elements actionElement = columns.get(5).select("input");

                        String cardNumber = actionElement.get(0).val();
                        String purchaseDate = actionElement.get(1).val();
                        String receiptNumber = actionElement.get(2).val();
                        String storeName = actionElement.get(3).val();
                        String tillNumber = actionElement.get(4).val();

                        HttpUriRequest purchaseDetails = RequestBuilder.get()
                                .setUri("https://www.ikeafamily.eu/PurchaseDetailsHandler.ashx")
                                .addParameter("cardNumber", cardNumber)
                                .addParameter("purchaseDate", purchaseDate)
                                .addParameter("receiptNumber", receiptNumber)
                                .addParameter("storeName", storeName)
                                .addParameter("tillNumber", tillNumber)
                                .build();

                        double price = getPrice(httpClient, purchaseDetails);

                        paragon = new IkeaParagon();

                        paragon.setCreatedDate(date);
                        paragon.setPaymentType(payment);
                        paragon.setShopName(shopName);
                        paragon.setPrice(price);
                        paragon.setName(paragonName);

                        boolean uploaded = ServiceFacade.getInvoicePdfService().findByParagon(paragon.getCreatedDate(), paragon.getName()) != null;

                        paragon.setUploaded(uploaded);

                        ServiceFacade.getIkeaParagonService().save(paragon);
                    } else
                        return false;

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    private double getPrice(CloseableHttpClient httpClient, HttpUriRequest purchaseDetails) throws IOException {
        BigDecimal result = BigDecimal.ZERO;

        try (CloseableHttpResponse response = httpClient.execute(purchaseDetails)) {
            String data = EntityUtils.toString(response.getEntity());
            Collection<ProductDto> dataList = gson.fromJson(data, collectionType);

            for (ProductDto item : dataList)
                result = result.add(item.getTotal());
        }

        return result.doubleValue();
    }

    private boolean login(CloseableHttpClient httpClient) throws Exception {
        String viewState = "";
        {
            HttpUriRequest login = RequestBuilder.get().setUri("https://www.ikeafamily.eu/Logowanie.aspx").build();

            try (CloseableHttpResponse response = httpClient.execute(login)) {
                Document document = Jsoup.parse(EntityUtils.toString(response.getEntity()));

                Elements elements = document.select("#__VIEWSTATE");

                if (elements.size() > 0)
                    viewState = elements.get(0).val();
            }
        }

        boolean isLogin;

        {
            ApplicationPreference preference = ServiceFacade.getApplicationPreference();

            HttpUriRequest login = RequestBuilder.post()
                    .setUri("https://www.ikeafamily.eu/Logowanie.aspx")
                    .addParameter("p$lt$ctl00$Logowanie_DuzyBoks$txtLogin", preference.getIkeaUser())
                    .addParameter("p$lt$ctl00$Logowanie_DuzyBoks$txtPassword", preference.getIkeaPassword())
                    .addParameter("manScript_HiddenField", "")
                    .addParameter("__EVENTARGUMENT", "")
                    .addParameter("__EVENTTARGET", "p$lt$ctl00$Logowanie_DuzyBoks$btnLogin")
                    .addParameter("__VIEWSTATE", viewState)
                    .addParameter("lng", "pl-PL")
                    .build();

            try (CloseableHttpResponse response = httpClient.execute(login)) {
                Document document = Jsoup.parse(EntityUtils.toString(response.getEntity()));

                Elements elements = document.select("#p_lt_ctl00_Logowanie_DuzyBoks_divLoginError");

                isLogin = elements.size() == 0;
            }
        }

        return isLogin;
    }
}

class ProductDto {
    public double price;
    public double count;

    public BigDecimal getTotal() {
        return BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(count)).setScale(3, BigDecimal.ROUND_CEILING);
    }
}

class ProductDtoAdapter implements JsonDeserializer<ProductDto> {

    @Override
    public ProductDto deserialize(JsonElement element, Type type,
                                  JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonElement = element.getAsJsonObject();

        ProductDto entity = new ProductDto();
        entity.price = jsonElement.get("LineItemSaleAmount").getAsDouble();
        entity.count = jsonElement.get("LineItemQuantity").getAsDouble();

        return entity;
    }
}
