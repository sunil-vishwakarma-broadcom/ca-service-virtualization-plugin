package com.ca.devtest.jenkins.plugin.data;

public abstract class RegistryData implements iData {
    private String registry;
    private String port;
    private String protocol;
    private String username;
    private String password;
    private String vse;
    private String apiUrl;

    protected RegistryData(String registry, String port, String protocol,
                        String username, String password, String vse, String apiUrl){
        this.registry = registry;
        this.port = port;
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.vse = vse;
        this.apiUrl = apiUrl;
    }

    public String getRegistry(){
        return this.registry;
    }

    public void setRegistry(String registry){
        this.registry = registry;
    }

    public String getPort(){
        return this.port;
    }

    public void setPort( String port){
        this.port = port;
    }

    public String getProtocol(){
        return this.protocol;
    }

    public void setProtocol(String protocol){
        this.protocol = protocol;
    }

    public String getUsername(){
        return this.username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getPassword(){
        return this.password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getVse(){
        return this.vse;
    }

    public void setVse(String vse){
        this.vse = vse;
    }

    public String getAPIUrl(){
        return apiUrl;
    }
}