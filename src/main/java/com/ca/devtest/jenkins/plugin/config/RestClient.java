package com.ca.devtest.jenkins.plugin.config;


import com.ca.devtest.jenkins.plugin.exception.InvalidInputException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class RestClient {

    private static CloseableHttpClient trustAnySSLCertificateClient;

    private RestClient() {
    }

    private static CloseableHttpClient getClient(boolean trustAnySSLCertificate) {
        try {
            if (trustAnySSLCertificate) {
                if (null == trustAnySSLCertificateClient) {
                    trustAnySSLCertificateClient = HttpClients.custom()
                            .setSSLSocketFactory(createSSLConnectionFactory()).build();
                }
                return trustAnySSLCertificateClient;
            } else {
                return HttpClients.createDefault();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create Http Client");
        }
    }

    private static SSLConnectionSocketFactory createSSLConnectionFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial((chain, authType) -> true).build();

        return new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
    }

    public static CloseableHttpResponse executeGet(String url, String username, String password, boolean trustAnySSLCertificate, Map<String, String> headers) {
        return executeRequest(url, username, password, trustAnySSLCertificate, getRequestGet(url, Collections.emptyMap(), headers));
    }

    public static CloseableHttpResponse executePost(String url, String username, String password, boolean trustAnySSLCertificate, HttpEntity entity, Map<String, String> headers) {
        return executeRequest(url, username, password, trustAnySSLCertificate, getRequestPost(url, entity, headers));
    }

    public static CloseableHttpResponse executeDelete(String url, String username, String password, boolean trustAnySSLCertificate) {
        return executeRequest(url, username, password, trustAnySSLCertificate, getRequestDelete(url));
    }


    private static  CloseableHttpResponse executeRequest(String url, String username, String password, boolean trustAnySSLCertificate, HttpRequestBase request) {
        try {
            CloseableHttpClient client = getClient(trustAnySSLCertificate);
            CredentialsProvider credentialsProvider = buildCredentialsProvider(url, username, password);
            HttpClientContext httpClientContext = buildContext(url, credentialsProvider);
            return client.execute(request, httpClientContext);
        } catch (UnknownHostException e) {
            throw new InvalidInputException(String.format("Unknown host: [%s]", e.getMessage()));
        } catch (HttpHostConnectException | URISyntaxException e) {
            throw new InvalidInputException(e.getMessage());
        } catch (ClientProtocolException e) {
            String message = null;
            if (e.getMessage() != null) {
                message = e.getMessage();
            } else if (e.getCause() != null) {
                message = e.getCause().getMessage();
            }
            throw new InvalidInputException(Optional.ofNullable(message).orElse("Client protocol error"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static HttpRequestBase getRequestPost(String urlPath, HttpEntity entity, Map<String, String> headers) {
        HttpPost request = new HttpPost(urlPath);
        if (entity != null) {
            request.setEntity(entity);
        }
        addCrumbsToRequest(request, headers);
        return request;
    }

    private static HttpRequestBase getRequestDelete(String urlPath) {
        return new HttpDelete(urlPath);
    }

    private static HttpRequestBase getRequestGet(String urlPath, Map<String, String> requestParams, Map<String, String> headers) {
        String urlWithParam = null;
        try {
            urlWithParam = addUrlParameters(urlPath, requestParams);
        } catch (URISyntaxException e) {
            throw new InvalidInputException(e.getMessage());
        }
        HttpGet request = new HttpGet(urlWithParam);
        addCrumbsToRequest(request, headers);
        return request;
    }

    private static String addUrlParameters(String url, Map<String, String> urlParams) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(url);
        if (urlParams != null) {
            urlParams.forEach(builder::setParameter);
        }
        return builder.build().toASCIIString();
    }


    private static HttpClientContext buildContext(String url, CredentialsProvider credentialsProvider) throws URISyntaxException {
        HttpClientContext httpClientContext = HttpClientContext.create();
        httpClientContext.setAuthCache(createAuthCache(url));
        httpClientContext.setCredentialsProvider(credentialsProvider);
        return httpClientContext;
    }

    private static void addCrumbsToRequest(HttpRequestBase request, Map<String, String> headers) {
        if(headers != null && !headers.isEmpty()){
            headers.forEach((key, value) -> {
                request.setHeader(key, value);
            });
        }

    }

    private static CredentialsProvider buildCredentialsProvider(String url, String username, String password) throws URISyntaxException {

        if (StringUtils.isEmpty(username) && StringUtils.isEmpty(password)) {
            return null;
        }

        if (StringUtils.isEmpty(url)) {
            throw new InvalidInputException("Empty url parameter received in endpoint definition");
        }

        CredentialsProvider credProvider = new BasicCredentialsProvider();
        URI build = new URIBuilder(url).build();
        HttpHost targetHost = new HttpHost(build.getHost(), build.getPort(), build.getScheme());
        credProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(username, password));

        return credProvider;
    }

    private static AuthCache createAuthCache(String url) throws URISyntaxException {
        URI build = new URIBuilder(url).build();
        HttpHost httpHost = new HttpHost(build.getHost(), build.getPort(), build.getScheme());
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(httpHost, basicAuth);
        return authCache;
    }
}
