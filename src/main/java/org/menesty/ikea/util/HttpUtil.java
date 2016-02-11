package org.menesty.ikea.util;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.menesty.ikea.service.ServiceFacade;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class HttpUtil {

  public static void initAuthenticator() {
    Authenticator.setDefault(new Authenticator() {
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(
            ServiceFacade.getApplicationPreference().getWarehouseUser(),
            ServiceFacade.getApplicationPreference().getWarehousePassword().toCharArray());
      }
    });
  }

  public static CredentialsProvider credentialsProvider(final HttpHost targetHost) {
    CredentialsProvider credsProvider = new BasicCredentialsProvider();
    credsProvider.setCredentials(
        new AuthScope(targetHost.getHostName(), targetHost.getPort()),
        new UsernamePasswordCredentials(
            ServiceFacade.getApplicationPreference().getWarehouseUser(),
            ServiceFacade.getApplicationPreference().getWarehousePassword()
        )
    );

    return credsProvider;
  }

  public static HttpClientContext context(final HttpHost targetHost) {
    AuthCache authCache = new BasicAuthCache();
    DigestScheme digestAuth = new DigestScheme();
    digestAuth.overrideParamter("realm", "Authentication require");
    digestAuth.overrideParamter("nonce", "1");
    authCache.put(targetHost, digestAuth);

    // Add AuthCache to the execution context
    HttpClientContext localContext = HttpClientContext.create();
    localContext.setAuthCache(authCache);

    return localContext;
  }
}
