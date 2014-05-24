package org.menesty.ikea.service.task;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.concurrent.Task;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.menesty.ikea.ApplicationPreference;
import org.menesty.ikea.service.ServiceFacade;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;

/**
 * Created by Menesty on
 * 5/23/14.
 */
public abstract class BaseIkeaFamilyTask<Result> extends Task<Result> {

    protected Gson gson;

    protected Type collectionType;

    public BaseIkeaFamilyTask() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ProductDto.class, new ProductDtoAdapter());

        gson = gsonBuilder.create();

        collectionType = new TypeToken<Collection<ProductDto>>() {
        }.getType();
    }

    protected boolean login(CloseableHttpClient httpClient) throws Exception {
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

    protected Collection<ProductDto> getParagonItems(CloseableHttpClient httpClient, HttpUriRequest purchaseDetails) throws IOException {
        try (CloseableHttpResponse response = httpClient.execute(purchaseDetails)) {
            String data = EntityUtils.toString(response.getEntity());

            return gson.fromJson(data, collectionType);
        }
    }
}

class ProductDto {
    public double price;
    public double count;
    public String artNumber;
    public String description;

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
        entity.artNumber = jsonElement.get("ProductSKU").getAsString();
        entity.description = jsonElement.get("ProductDescription").getAsString().trim();

        return entity;
    }
}
