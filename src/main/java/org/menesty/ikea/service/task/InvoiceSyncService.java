package org.menesty.ikea.service.task;

import com.google.gson.Gson;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
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
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.WarehouseItemDto;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.util.HttpUtil;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InvoiceSyncService extends AbstractAsyncService<Void> {
    private SimpleObjectProperty<CustomerOrder> customerOrder = new SimpleObjectProperty<>();

    private void sendData(String data) throws IOException {
        URL url = new URL(ServiceFacade.getApplicationPreference().getWarehouseHost() + "/sync/update");
        HttpHost targetHost = new HttpHost(url.getHost());

        CredentialsProvider credsProvider = HttpUtil.credentialsProvider(targetHost);

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
            HttpClientContext localContext = HttpUtil.context(targetHost);

            HttpPost httpPost = new HttpPost(url.toURI());
            httpPost.setEntity(new StringEntity(data, ContentType.APPLICATION_FORM_URLENCODED));

            try (CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, localContext)) {
                String responseData = EntityUtils.toString(response.getEntity());
                System.out.println(responseData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WarehouseItemDto convert(Integer orderId, InvoiceItem invoiceItem) {
        WarehouseItemDto item = new WarehouseItemDto();
        item.allowed = true;
        item.count = invoiceItem.getCount();
        item.orderId = orderId;
        item.price = invoiceItem.getPriceWat();
        item.productNumber = invoiceItem.getArtNumber();
        item.shortName = invoiceItem.getShortName();
        item.zestav = invoiceItem.isZestav();
        item.visible = invoiceItem.isVisible();
        item.weight = invoiceItem.getWeight();
        item.productId = invoiceItem.getOriginArtNumber();

        return item;
    }

    @Override
    protected Task<Void> createTask() {
        final CustomerOrder _order = customerOrder.get();
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<WarehouseItemDto> result = new ArrayList<>();
                //TODO FIX me change to one query
                for (InvoiceItem item : ServiceFacade.getInvoiceItemService().loadBy(_order))
                    result.add(convert(_order.getId(), item));

                try {
                    sendData(new Gson().toJson(result));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
    }

    public void setCustomerOrder(CustomerOrder customerOrder) {
        this.customerOrder.setValue(customerOrder);
    }
}
