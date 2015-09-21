package org.menesty.ikea.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.List;

/**
 * Created by Menesty on
 * 7/21/15.
 * 08:44.
 */
public class APIRequest {
    private final URI url;

    public APIRequest(URI url) {
        this.url = url;
    }

    private String loadData() throws Exception {

        HttpHost targetHost = new HttpHost(url.getHost(), url.getPort());

        CredentialsProvider credsProvider = HttpUtil.credentialsProvider(targetHost);

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
            HttpClientContext localContext = HttpUtil.context(targetHost);

            HttpGet httpPost = new HttpGet(url);

            try (CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, localContext)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }

    public <T> T getData(Class<T> clazz) throws Exception {
        String response = loadData();
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(response, clazz);
    }

    public <T> T getData(TypeReference<T> typeReference) throws Exception {
        String response = loadData();
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(response, typeReference);
    }

    public <T> List<T> getList(TypeReference<List<T>> typeReference) throws Exception {
        String response = loadData();
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(response, typeReference);
    }
}
