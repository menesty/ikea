package org.menesty.ikea.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.menesty.ikea.lib.domain.ClientOrder;
import org.menesty.ikea.lib.dto.PageResult;
import org.menesty.ikea.service.ServiceFacade;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created by Menesty on
 * 7/4/15.
 * 12:03.
 */
public class HttpServiceUtil {

    public static APIRequest get(String requestUrl) {
        try {
            return new APIRequest(new URL(ServiceFacade.getApplicationPreference().getWarehouseHost() + requestUrl));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to create API Request", e);
        }

    }
}
