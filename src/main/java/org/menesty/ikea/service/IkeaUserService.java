package org.menesty.ikea.service;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class IkeaUserService {

    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String... arg) throws IOException {
        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        try {
           /* HttpGet httpget = new HttpGet("https://secure.ikea.com/webapp/wcs/stores/servlet/LogonForm?storeId=19&langId=-27&catalogId=11001");

            CloseableHttpResponse response1 = httpclient.execute(httpget);
            try {
                HttpEntity entity = response1.getEntity();

                System.out.println("Login form get: " + response1.getStatusLine());
                EntityUtils.consume(entity);

                System.out.println("Initial set of cookies:");
                List<Cookie> cookies = cookieStore.getCookies();
                if (cookies.isEmpty()) {
                    System.out.println("None");
                } else {
                    for (int i = 0; i < cookies.size(); i++) {
                        System.out.println("- " + cookies.get(i).toString());
                    }
                }
            } finally {
                response1.close();
            }*/

            HttpPost httpost = new HttpPost("https://secure.ikea.com/webapp/wcs/stores/servlet/Logon");
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("storeId", "19"));
            nvps.add(new BasicNameValuePair("langId", "-27"));
            nvps.add(new BasicNameValuePair("logonId", "komb_husar@gmail.com"));
            nvps.add(new BasicNameValuePair("logonPassword", "Mature65"));

            httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

            CloseableHttpResponse response2 = httpclient.execute(httpost);
            try {
                HttpEntity entity = response2.getEntity();

                System.out.println("Login form get: " + response2.getStatusLine());
                System.out.println(EntityUtils.toString(entity) + " =====");

                System.out.println("Post logon cookies:");
                List<Cookie> cookies = cookieStore.getCookies();
                if (cookies.isEmpty()) {
                    System.out.println("None");
                } else {
                    for (int i = 0; i < cookies.size(); i++) {
                        System.out.println("- " + cookies.get(i).toString());
                    }
                }
            } finally {
                response2.close();
            }
        } finally {
            httpclient.close();
        }

    }


}
