package org.menesty.ikea.util;

import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.menesty.ikea.service.ServiceFacade;

import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created by Menesty on
 * 7/4/15.
 * 12:03.
 */
public class HttpServiceUtil {

    public static <T> Callable<T> get(String requestUrl) {
        return () -> {
            URL url = new URL(ServiceFacade.getApplicationPreference().getWarehouseHost() + requestUrl);
            HttpHost targetHost = new HttpHost(url.getHost(), url.getPort());

            CredentialsProvider credsProvider = HttpUtil.credentialsProvider(targetHost);

            try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
                HttpClientContext localContext = HttpUtil.context(targetHost);

                HttpGet httpPost = new HttpGet(url.toURI());

                try (CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, localContext)) {
                    String responseData = EntityUtils.toString(response.getEntity());
                    System.out.println(responseData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };

    }
}
