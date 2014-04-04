package org.menesty.ikea.service.task;

import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.menesty.ikea.domain.WarehouseItemDto;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.util.HttpUtil;
import org.menesty.ikea.util.NumberUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;

public abstract class BaseInvoiceSyncService extends AbstractAsyncService<Boolean> {

    private final BigDecimal margin = BigDecimal.valueOf(1.02);

    protected void sendData(String data) throws IOException {
        URL url = new URL(ServiceFacade.getApplicationPreference().getWarehouseHost() + "/sync/update");
        HttpHost targetHost = new HttpHost(url.getHost());

        CredentialsProvider credsProvider = HttpUtil.credentialsProvider(targetHost);

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
            HttpClientContext localContext = HttpUtil.context(targetHost);

            HttpPost httpPost = new HttpPost(url.toURI());
            httpPost.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, localContext)) {
                String responseData = EntityUtils.toString(response.getEntity());
                System.out.println(responseData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected WarehouseItemDto convert(Integer orderId, InvoiceItem invoiceItem) {
        WarehouseItemDto item = new WarehouseItemDto();
        item.allowed = true;
        item.count = invoiceItem.getCount();
        item.orderId = orderId;
        //add 2%
        item.price = NumberUtil.round(BigDecimal.valueOf(invoiceItem.getPriceWat()).multiply(margin).doubleValue());
        item.productNumber = invoiceItem.getArtNumber();
        item.shortName = invoiceItem.getShortName();
        item.zestav = invoiceItem.isZestav();
        item.visible = invoiceItem.isVisible();
        item.weight = invoiceItem.getWeight();
        item.productId = invoiceItem.getOriginArtNumber();

        return item;
    }
}
