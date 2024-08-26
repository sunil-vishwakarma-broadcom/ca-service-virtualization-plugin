package com.ca.devtest.jenkins.plugin.data;

import java.util.List;

public class DeployMarData extends RegistryData{
    private List<String> marFiles;
    public DeployMarData(String registry, String port, String protocol, boolean trustAnySSLCertificate,
                         String username, String password, String vse,
                         List<String> marfiles, String apiURL){
        super( registry,  port,  protocol,
                username,  password,  vse, apiURL, trustAnySSLCertificate);
        this.marFiles = marfiles;
    }
    public void setMarFiles(List<String> marFiles){
        this.marFiles = marFiles;
    }

    public List<String> getMarFiles(){
        return this.marFiles;
    }
}