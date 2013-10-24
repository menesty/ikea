package org.menesty.ikea.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class IkeaUserService {

    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String ... arg) throws IOException {
        new IkeaUserService().run();
    }

    public void run() throws IOException {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

        Map<String, String> params = new HashMap<>();
        params.put("storeId", "19");
        params.put("langId", "-27");
        params.put("logonId", "komb_husar@gmail.com");
        params.put("logonPassword", "Mature65");
        params.put("rememberMe", "");



        Document response = sendPost("https://secure.ikea.com/webapp/wcs/stores/servlet/Logon", params);
        System.out.println(response.html());

    }


    private Document sendPost(String address, Map<String, String> params) throws IOException {
        URL url = new URL(address);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Host", url.getHost());
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        /*for (String cookie : this.cookies) {
            conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
        }*/
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Referer", "https://secure.ikea.com/webapp/wcs/stores/servlet/LogonForm?storeId=19&langId=-27&catalogId=11001");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() != 0)
                sb.append("&");
            sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        conn.setRequestProperty("Content-Length", Integer.toString(sb.length()));

        conn.setDoOutput(true);
        conn.setDoInput(true);


        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(sb.toString());
        wr.flush();
        wr.close();

        int responseCode = conn.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + sb.toString());
        System.out.println("Response Code : " + responseCode);


        /*try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }*/

        return Jsoup.parse(conn.getInputStream(), "UTF-8", url.getHost());


    }

}
