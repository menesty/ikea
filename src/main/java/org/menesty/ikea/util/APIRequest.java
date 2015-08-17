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

import java.io.IOException;
import java.net.URL;

/**
* Created by Menesty on
* 7/21/15.
* 08:44.
*/
public class APIRequest {
   private final URL url;

   public APIRequest(URL url) {
       this.url = url;
   }

   private String loadData() {

       HttpHost targetHost = new HttpHost(url.getHost(), url.getPort());

       CredentialsProvider credsProvider = HttpUtil.credentialsProvider(targetHost);

       try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
           HttpClientContext localContext = HttpUtil.context(targetHost);

           HttpGet httpPost = new HttpGet(url.toURI());

           try (CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, localContext)) {
               return EntityUtils.toString(response.getEntity());
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
       return null;
   }

   public <T> T getData(Class<T> clazz) throws IOException {
       String response = loadData();
       ObjectMapper objectMapper = new ObjectMapper();

       return objectMapper.readValue(response, clazz);
   }

   public <T> T getData(TypeReference<T> typeReference) throws IOException {
       String response = loadData();
       ObjectMapper objectMapper = new ObjectMapper();

       return objectMapper.readValue(response, typeReference);
   }
}
