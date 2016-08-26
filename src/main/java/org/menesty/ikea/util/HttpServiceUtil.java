package org.menesty.ikea.util;

import org.apache.http.client.utils.URIBuilder;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.util.vk.VKMarketAPIRequest;

import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by Menesty on
 * 7/4/15.
 * 12:03.
 */
public class HttpServiceUtil {

  public static APIRequest get(String requestUrl, Map<String, String> params) {
    try {
      URIBuilder uriBuilder = new URIBuilder(ServiceFacade.getApplicationPreference().getWarehouseHost() + requestUrl);

      if (params != null) {
        params.entrySet().stream().forEach(param -> uriBuilder.addParameter(param.getKey(), param.getValue()));
      }

      return new APIRequest(uriBuilder.build());
    } catch (URISyntaxException e) {
      throw new RuntimeException("Failed to create API Request", e);
    }
  }

  public static APIRequest get(String requestUrl) {
    return get(requestUrl, null);

  }

  public static VKMarketAPIRequest getVKMarketAPI() {
    try {
      return new VKMarketAPIRequest(ServiceFacade.getApplicationPreference());
    } catch (URISyntaxException e) {
      ServiceFacade.getErrorConsole().add(e);
      throw new RuntimeException("configure VK account");
    }
  }
}
