package org.menesty.ikea.service.task;

import com.google.gson.Gson;
import javafx.concurrent.Task;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
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

        Gson gson = new Gson();
        String json = gson.toJson(result);

        HttpClient httpClient = HttpClients.createDefault();

        return null;
    }

    private void sendData() throws IOException {
        HttpHost targetHost = new HttpHost("localhost", 80, "http");
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials("username", "password"));
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider).build();
        try {

            // Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            // Generate DIGEST scheme object, initialize it and add it to the local
            // auth cache
            DigestScheme digestAuth = new DigestScheme();
            // Suppose we already know the realm name
            digestAuth.overrideParamter("realm", "some realm");
            // Suppose we already know the expected nonce value
            digestAuth.overrideParamter("nonce", "whatever");
            authCache.put(targetHost, digestAuth);

            // Add AuthCache to the execution context
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);

            HttpGet httpget = new HttpGet("/");

            System.out.println("executing request: " + httpget.getRequestLine());
            System.out.println("to target: " + targetHost);

            for (int i = 0; i < 3; i++) {
                CloseableHttpResponse response = httpclient.execute(targetHost, httpget, localContext);
                try {
                    HttpEntity entity = response.getEntity();

                    System.out.println("----------------------------------------");
                    System.out.println(response.getStatusLine());
                    if (entity != null) {
                        System.out.println("Response content length: " + entity.getContentLength());
                    }
                    EntityUtils.consume(entity);
                } finally {
                    response.close();
                }
            }
        } finally {
            httpclient.close();
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
