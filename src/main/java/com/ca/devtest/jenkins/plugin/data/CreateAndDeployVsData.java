package com.ca.devtest.jenkins.plugin.data;

/**
 * CreateVsData keeps the data for Creating Virtual Service
 *
 * @author sv673714
 */
public class CreateAndDeployVsData extends RegistryData {
    private String config;
    private boolean deploy;
    private boolean undeploy;
    private String resolvedInputFile1Path;
    private String resolvedInputFile2Path;
    private String resolvedActiveConfig;
    private String resolvedDataFiles;
    private String swaggerurl;
    private String ramlurl;
    private String wadlurl;

    public CreateAndDeployVsData(String registry, String port, String protocol,
                                 String username, String password, String vse, String apiUrl, String config,
                                 boolean deploy, boolean undeploy, String resolvedInputFile1Path, String resolvedInputFile2Path,
                                 String resolvedActiveConfig, String resolvedDataFiles, String swaggerurl,
                                 String ramlurl, String wadlurl) {
        super(registry, port, protocol, username, password, vse, apiUrl);
        this.config = config;
        this.deploy = deploy;
        this.undeploy = undeploy;
        this.resolvedInputFile1Path = resolvedInputFile1Path;
        this.resolvedInputFile2Path = resolvedInputFile2Path;
        this.resolvedActiveConfig = resolvedActiveConfig;
        this.resolvedDataFiles = resolvedDataFiles;
        this.swaggerurl = swaggerurl;
        this.ramlurl = ramlurl;
        this.wadlurl = wadlurl;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public boolean isDeploy() {
        return deploy;
    }

    public void setDeploy(boolean deploy) {
        this.deploy = deploy;
    }

    public boolean isUndeploy() {
        return undeploy;
    }

    public void setUndeploy(boolean undeploy) {
        this.undeploy = undeploy;
    }

    public String getResolvedInputFile1Path() {
        return resolvedInputFile1Path;
    }

    public void setResolvedInputFile1Path(String resolvedInputFile1Path) {
        this.resolvedInputFile1Path = resolvedInputFile1Path;
    }

    public String getResolvedInputFile2Path() {
        return resolvedInputFile2Path;
    }

    public void setResolvedInputFile2Path(String resolvedInputFile2Path) {
        this.resolvedInputFile2Path = resolvedInputFile2Path;
    }

    public String getResolvedActiveConfig() {
        return resolvedActiveConfig;
    }

    public void setResolvedActiveConfig(String resolvedActiveConfig) {
        this.resolvedActiveConfig = resolvedActiveConfig;
    }

    public String getResolvedDataFiles() {
        return resolvedDataFiles;
    }

    public void setResolvedDataFiles(String resolvedDataFiles) {
        this.resolvedDataFiles = resolvedDataFiles;
    }

    public String getSwaggerurl() {
        return swaggerurl;
    }

    public void setSwaggerurl(String swaggerurl) {
        this.swaggerurl = swaggerurl;
    }

    public String getRamlurl() {
        return ramlurl;
    }

    public void setRamlurl(String ramlurl) {
        this.ramlurl = ramlurl;
    }

    public String getWadlurl() {
        return wadlurl;
    }

    public void setWadlurl(String wadlurl) {
        this.wadlurl = wadlurl;
    }
}
