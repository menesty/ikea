package org.menesty.ikea.util.vk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.menesty.ikea.ApplicationPreference;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Menesty on
 * 7/11/16.
 * 20:26.
 */
public class VKAPIRequest implements Closeable {
  private final int APPLICATION_ID;
  private final String SECURE_KEY;
  private final String userName;
  private final String password;
  private final int groupId;

  protected final String API_VERSION = "5.52";
  private final CloseableHttpClient httpClient;
  protected final ObjectMapper objectMapper;
  protected final URI photoServer;


  private static AccessToken accessToken;

  public VKAPIRequest(ApplicationPreference applicationPreference) throws URISyntaxException {
    RequestConfig rc = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD)
        .setRedirectsEnabled(false)
        .setRelativeRedirectsAllowed(false)
        .setConnectTimeout((int) TimeUnit.SECONDS.toMillis(10))
        .setSocketTimeout((int) TimeUnit.SECONDS.toMillis(10))
        .build();
    httpClient = HttpClients.custom().setDefaultRequestConfig(rc).build();
    objectMapper = new ObjectMapper();

    APPLICATION_ID = applicationPreference.getVKApplicationId();
    SECURE_KEY = applicationPreference.getVKSecureKey();
    userName = applicationPreference.getVKUser();
    password = applicationPreference.getVKPassword();
    groupId = applicationPreference.getVKGroupId();

    URI serverURI = new URI(applicationPreference.getWarehouseHost());
    photoServer = new URI(serverURI.getScheme(), serverURI.getHost(), null, null);
  }

  public int getGroupId() {
    return groupId;
  }

  public AccessToken getAccessToken() {
    return accessToken;
  }

  public synchronized void authorize() throws Exception {
    if (accessToken != null) {
      return;
    }

    URI formUrl = new URI("https://oauth.vk.com/authorize?client_id=" + APPLICATION_ID + "&display=mobile&response_type=code&v=" + API_VERSION + "&redirect_uri=https://oauth.vk.com/blank.html&scope=offline,market,photos");
    URI authUrl = new URI("https://login.vk.com/?act=login&soft=1&utf8=1");

    String loginPageContent = getContent(httpClient, new HttpGet(formUrl));
    List<NameValuePair> postParameters = new ArrayList<>();

    Document document = Jsoup.parse(loginPageContent);
    Elements inputs = document.select("input");

    for (Element inputElement : inputs) {
      if ("hidden".equals(inputElement.attr("type"))) {
        postParameters.add(new BasicNameValuePair(inputElement.attr("name"), inputElement.val()));
      }
    }
    postParameters.add(new BasicNameValuePair("email", userName));
    postParameters.add(new BasicNameValuePair("pass", password));


    HttpPost httpRequest = new HttpPost(authUrl);
    httpRequest.setEntity(new UrlEncodedFormEntity(postParameters));

    URI location = new URI(getLocation(httpClient, httpRequest));
    String allowPermissionPageUrl = getLocation(httpClient, new HttpGet(location));

    AccessToken tokenInfo = getToken(getLocation(httpClient, new HttpGet(allowPermissionPageUrl)));

    if (tokenInfo == null) {
      String allowPermissionPageContent = getContent(httpClient, new HttpGet(allowPermissionPageUrl));

      document = Jsoup.parse(allowPermissionPageContent);
      Elements formElements = document.select("form");

      if (!formElements.isEmpty()) {
        String confirmUrl = formElements.get(0).attr("action");

        tokenInfo = getToken(getLocation(httpClient, new HttpPost(confirmUrl)));
      }

    }

    if (tokenInfo != null) {
      accessToken = tokenInfo;
    }
  }

  private AccessToken getToken(String location) throws IOException {
    List<NameValuePair> result = URLEncodedUtils.parse(location, Charset.forName("utf-8"));

    if (!result.isEmpty()) {
      String code = result.get(0).getValue();
      String accessCodeContent = getContent(httpClient, new HttpGet("https://oauth.vk.com/access_token?client_id=" + APPLICATION_ID + "&client_secret=" + SECURE_KEY +
          "&redirect_uri=https://oauth.vk.com/blank.html&code=" + code));

      return objectMapper.readValue(accessCodeContent, AccessToken.class);
    }

    return null;
  }

  private String getContent(CloseableHttpClient httpClient, HttpRequestBase request) throws IOException {
    try (CloseableHttpResponse response = httpClient.execute(request)) {
      return EntityUtils.toString(response.getEntity());
    }
  }

  private String getLocation(CloseableHttpClient httpClient, HttpRequestBase request) throws IOException {

    try (CloseableHttpResponse response = httpClient.execute(request)) {
      return response.getFirstHeader("Location").getValue();
    }
  }

  @Override
  public void close() throws IOException {
    httpClient.close();
  }

  protected String getContent(HttpRequestBase request) throws IOException {
    return getContent(httpClient, request);
  }

  protected String postContent(HttpPost request, List<NameValuePair> params) throws IOException {
    request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    request.setEntity(new UrlEncodedFormEntity(params, Charset.forName("UTF-8")));

    try (CloseableHttpResponse response = httpClient.execute(request)) {
      return EntityUtils.toString(response.getEntity());
    }
  }

  protected String post(HttpPost request) throws IOException {
    try (CloseableHttpResponse response = httpClient.execute(request)) {
      return EntityUtils.toString(response.getEntity());
    }
  }

  protected InputStream getStream(HttpGet request) throws IOException {
    try (CloseableHttpResponse response = httpClient.execute(request)) {
      return response.getEntity().getContent();
    }
  }
}


@JsonIgnoreProperties(ignoreUnknown = true)
class AccessToken {
  @JsonProperty("access_token")
  private String token;
  @JsonProperty("user_id")
  private int userId;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }
}