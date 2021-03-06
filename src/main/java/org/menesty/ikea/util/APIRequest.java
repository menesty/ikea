package org.menesty.ikea.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
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

  public URI getUrl() {
    return url;
  }

  private byte[] loadData(String method) throws Exception {

    HttpHost targetHost = new HttpHost(url.getHost(), url.getPort());

    CredentialsProvider credsProvider = HttpUtil.credentialsProvider(targetHost);

    try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
      HttpClientContext localContext = HttpUtil.context(targetHost);

      HttpRequestBase httpRequest;

      if (HttpDelete.METHOD_NAME.equals(method)) {
        httpRequest = new HttpDelete(url);
      } else {
        httpRequest = new HttpGet(url);
      }

      try (CloseableHttpResponse response = httpClient.execute(targetHost, httpRequest, localContext)) {
        return EntityUtils.toByteArray(response.getEntity());
      }
    }
  }

  private String postRequestData(String method, Object object) throws IOException {
    HttpHost targetHost = new HttpHost(url.getHost(), url.getPort());

    CredentialsProvider credsProvider = HttpUtil.credentialsProvider(targetHost);

    try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
      HttpClientContext localContext = HttpUtil.context(targetHost);

      HttpEntityEnclosingRequestBase httpRequest;

      if (HttpPut.METHOD_NAME.equals(method)) {
        httpRequest = new HttpPut(url);
      } else {
        httpRequest = new HttpPost(url);
      }

      if (object != null) {
        String data = !(object instanceof String) ? getMapper().writeValueAsString(object) : (String) object;
        httpRequest.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));
      }

      try (CloseableHttpResponse response = httpClient.execute(targetHost, httpRequest, localContext)) {
        return EntityUtils.toString(response.getEntity());
      }
    }
  }

  public <T> T postData(Object param, Class<T> clazz) throws Exception {
    return postData(HttpPost.METHOD_NAME, param, clazz);
  }

  public <T> T postData(String method, Object param, Class<T> clazz) throws Exception {
    String response = postRequestData(method, param);
    checkForError(response);

    return getMapper().readValue(response, clazz);
  }

  public void postData(Object param) throws Exception {
    postData(HttpPost.METHOD_NAME, param);
  }

  public void postData(String method, Object param) throws Exception {
    String response = postRequestData(method, param);
    checkForError(response);
  }

  public <T> T postData(Object param, TypeReference<T> typeReference) throws Exception {
    return postData(HttpPost.METHOD_NAME, param, typeReference);
  }

  public <T> T postData(String method, Object param, TypeReference<T> typeReference) throws Exception {
    String response = postRequestData(method, param);
    checkForError(response);

    return getMapper().readValue(response, typeReference);
  }

  public <T> T getData(Class<T> clazz) throws Exception {
    return getData(clazz, HttpGet.METHOD_NAME);
  }

  public <T> T getData(Class<T> clazz, String method) throws Exception {
    String response = toString(loadData(method));
    checkForError(response);

    return getMapper().readValue(response, clazz);
  }

  public void get() throws Exception {
    get(HttpGet.METHOD_NAME);
  }

  public void get(String method) throws Exception {
    String response = toString(loadData(method));
    checkForError(response);
  }

  private String toString(byte[] byteArray) {
    return new String(byteArray);
  }

  private void checkForError(String response) {
    if (response.contains("exception") && !response.contains("\"status\":200")) {
      throw new RuntimeException(response);
    }
  }


  public <T> T getData(TypeReference<T> typeReference) throws Exception {
    return getData(typeReference, HttpGet.METHOD_NAME);
  }

  public <T> T getData(TypeReference<T> typeReference, String method) throws Exception {
    String response = toString(loadData(method));
    checkForError(response);
    ObjectMapper objectMapper = getMapper();

    return objectMapper.readValue(response, typeReference);
  }

  public <T> List<T> getList(TypeReference<List<T>> typeReference) throws Exception {
    return getList(typeReference, HttpGet.METHOD_NAME);
  }

  public <T> List<T> getList(TypeReference<List<T>> typeReference, String method) throws Exception {
    String response = toString(loadData(method));
    checkForError(response);

    return getMapper().readValue(response, typeReference);
  }

  public byte[] getBytes() throws Exception {
    return getBytes(HttpGet.METHOD_NAME);
  }

  public byte[] getBytes(String method) throws Exception {
    byte[] bytes = loadData(method);
    checkForError(toString(bytes));

    return bytes;
  }

  public String getRawData() throws Exception {
    byte[] bytes = getBytes();

    return bytes.length == 0 ? null : new String(bytes);
  }

  private ObjectMapper getMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    return objectMapper;
  }
}
