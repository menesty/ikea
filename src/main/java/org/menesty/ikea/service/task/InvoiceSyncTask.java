package org.menesty.ikea.service.task;

import com.google.gson.Gson;
import javafx.concurrent.Task;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.domain.WarehouseItemDto;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.service.ServiceFacade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InvoiceSyncTask extends Task<Void> {
    private CustomerOrder customerOrder;

    @Override
    protected Void call() throws Exception {
        List<WarehouseItemDto> result = new ArrayList<>();

        for (InvoicePdf invoicePdf : customerOrder.getInvoicePdfs()) {
            for (InvoiceItem item : ServiceFacade.getInvoiceItemService().load(invoicePdf))
                result.add(convert(invoicePdf, item));

        }

        try {
            sendData(new Gson().toJson(result));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void sendData(String data) throws IOException {
        HttpHost targetHost = new HttpHost(ServiceFacade.getApplicationPreference().getWarehouseHost());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(
                        ServiceFacade.getApplicationPreference().getWarehouseUser(),
                        ServiceFacade.getApplicationPreference().getWarehousePassword()
                )
        );

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
            // Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            DigestScheme digestAuth = new DigestScheme();
            authCache.put(targetHost, digestAuth);

            // Add AuthCache to the execution context
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);


            HttpPost httpPost = new HttpPost("/sync");
            httpPost.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));


            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                EntityUtils.consume(response.getEntity());
            }
        }
    }

    private WarehouseItemDto convert(InvoicePdf invoicePdf, InvoiceItem invoiceItem) {
        WarehouseItemDto item = new WarehouseItemDto();
        item.allowed = true;
        item.clientId = invoiceItem.getId();
        item.count = invoiceItem.getCount();
        item.orderId = customerOrder.getId();
        item.price = invoiceItem.getPriceWat();
        item.productNumber = invoiceItem.getArtNumber();
        item.invoicePdf = invoicePdf.getId();
        item.shortName = invoiceItem.getShortName();
        item.zestav = invoiceItem.isZestav();
        item.visible = invoiceItem.isVisible();
        return item;
    }
}
