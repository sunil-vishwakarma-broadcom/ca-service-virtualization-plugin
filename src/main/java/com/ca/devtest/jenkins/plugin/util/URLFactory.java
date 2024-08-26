package com.ca.devtest.jenkins.plugin.util;

import com.ca.devtest.jenkins.plugin.exception.InvalidInputException;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;

public class URLFactory {

    private final URIBuilder builder;

    public URLFactory(String protocol, String host, String port) {
        builder = new URIBuilder();
        builder.setScheme(protocol);
        builder.setHost(host);
        if(!Utils.isValidPort(port)){
        throw new InvalidInputException(String.format("Invalid Port [%s] provided. Please provide a valid Port number.", port));
        }
        builder.setPort(Integer.parseInt(port));

    }


    public String buildUrl(String url) {
        try {
            builder.setPath(url);
            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new InvalidInputException(e.getMessage());
        }
    }


}
