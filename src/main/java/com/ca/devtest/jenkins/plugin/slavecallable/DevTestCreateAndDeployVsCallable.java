/******************************************************************************
 *
 * Copyright (c) 2021 Broadcom Inc.  All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated
 * in any way except as authorized by the applicable license agreement,
 * without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT
 * WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED
 * WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN
 * NO EVENT WILL CA BE LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY
 * LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE USE OF THIS SOFTWARE,
 * INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR
 * DAMAGE.
 *
 * This file is made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 ******************************************************************************/

package com.ca.devtest.jenkins.plugin.slavecallable;

import com.ca.devtest.jenkins.plugin.Messages;
import com.ca.devtest.jenkins.plugin.config.RestClient;
import com.ca.devtest.jenkins.plugin.constants.APIEndpoints;
import com.ca.devtest.jenkins.plugin.data.CreateAndDeployVsData;
import com.ca.devtest.jenkins.plugin.data.DevTestReturnValue;
import com.ca.devtest.jenkins.plugin.exception.InvalidInputException;
import com.ca.devtest.jenkins.plugin.util.URLFactory;
import com.ca.devtest.jenkins.plugin.util.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hudson.AbortException;
import hudson.FilePath;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;

/**
 * DevTestCreateAndDeployVsCallable used for executing the jobs on the specified node
 * @author sv673714
 */
public class DevTestCreateAndDeployVsCallable extends AbstractDevTestMasterToSlaveCallable {

    private static final long serialVersionUID = 1L;

    CreateAndDeployVsData data;
    public DevTestCreateAndDeployVsCallable(FilePath workspace, TaskListener listener, CreateAndDeployVsData data){
        super(workspace, listener);
        this.data = data;
    }

    @Override
    public DevTestReturnValue call(){
        DevTestReturnValue devTestReturnValue = new DevTestReturnValue();
        try {

            getListener().getLogger().println("\n> Executing task on node "+ InetAddress.getLocalHost().getHostName());
            devTestReturnValue.setNode(InetAddress.getLocalHost().getHostName());
            //undeploy here
            if(Boolean.valueOf(data.isUndeploy())) {
                handleUndeploy();
            }
            if(data.isDeploy()) getListener().getLogger().print(Messages.DevTestCreateAndDeployVs_creatingAndDeploying());
            else getListener().getLogger().print(Messages.DevTestCreateAndDeployVs_creating());

            getListener().getLogger().println(" " +Messages.DevTestPlugin_devTestLocation(data.getRegistry(), data.getPort()));

            String createDeployVSUrl  = new URLFactory(data.getProtocol() , data.getRegistry(), data.getPort()).buildUrl(data.getAPIUrl());


            HttpEntity entity = createPostEntity();
            Map<String, String> headers = Collections.singletonMap("Accept", "application/json");
            getListener().getLogger().println("\n> Calling URL  "+ createDeployVSUrl);
            try(CloseableHttpResponse response = RestClient.executePost(createDeployVSUrl, data.getUsername(), data.getPassword(), data.isTrustAnySSLCertificate(), entity, headers)){

                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode == 200) {
                    if(data.isDeploy()) devTestReturnValue.setMessage(Messages.DevTestCreateAndDeployVs_createAndDeploySuccess());
                    else devTestReturnValue.setMessage(Messages.DevTestCreateAndDeployVs_createSuccess());

                    getListener().getLogger().println(Messages.DevTestPlugin_responseBody(responseBody));

                    devTestReturnValue.setSuccess(true);
                } else {
                    if(data.isDeploy()) getListener().getLogger().println(Messages.DevTestCreateAndDeployVs_createAndDeployError());
                    else getListener().getLogger().println(Messages.DevTestCreateAndDeployVs_createError());

                    String message = Messages.DevTestPlugin_responseStatus(statusCode, responseBody);
                    devTestReturnValue.setMessage(message);
                    devTestReturnValue.setSuccess(false);
                }
            }
        } catch(Exception e){
            devTestReturnValue.setMessage(e.getMessage());
        }
        return devTestReturnValue;
    }

    /** Handle undeploy of existing virtual service **/
    private void handleUndeploy()  throws IOException {
        JsonElement jelement = new JsonParser().parse(data.getConfig());
        JsonObject  jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("virtualService");
        String vsName = jobject.get("name").getAsString();
        getListener().getLogger().println("Undeploy if virtual service '"+ vsName+"' is running on VSE '" + data.getVse() + "'");


        String urlPath = String.format(APIEndpoints.UNDEPLOY_VS, data.getVse(), vsName);
        String unDeployVSUrl  = new URLFactory(data.getProtocol() , data.getRegistry(), data.getPort()).buildUrl(urlPath);
        try(CloseableHttpResponse response = RestClient.executeDelete(unDeployVSUrl, data.getUsername(), data.getPassword(), data.isTrustAnySSLCertificate())){
            int statusCode = response.getStatusLine().getStatusCode();
            String message;
            if (statusCode == 204) {
                message = Messages.DevTestUndeployVs_success(vsName, data.getVse());
                getListener().getLogger().println(message);
            }else if (statusCode == 404) {
                message = Messages.DevTestUndeployVs_VSNotFound(vsName,data.getVse());
                getListener().getLogger().println(message);
            } else {
                getListener().getLogger()
                        .println(Messages.DevTestUndeployVs_error(vsName));
                if (statusCode == 200) {
                    // Invalid credentials...
                    message = Messages.DevTestPlugin_invalidCredentials();
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    message = Messages.DevTestPlugin_responseStatus(statusCode, responseBody);
                }
                throw new AbortException(message);
            }
        }
    }



    /**
     * Creates entity for HTTP Post request depending on provided input parameters and format of path to inputFile1/inputFile2 file.
     * If path to inputFile1/inputFile2 file contains http or file prefix than form with fileURI property is used and this means
     * that DevTest will look for inputFile1/inputFile2 file on the provided HTTP link or on its filesystem. When no
     * prefix is part of the path then inputFile1/inputFile2 file is located in workspace of the job and we upload it as
     * part of request with file property.
     *
     * @return created {@link HttpEntity} for deploying VS
     */
    private HttpEntity createPostEntity()
            throws IOException, InterruptedException {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setContentType(ContentType.MULTIPART_FORM_DATA);

        getListener().getLogger().println("Config Object: "+ data.getConfig());
        getListener().getLogger().println( "Deploy: "+ data.isDeploy());
        getListener().getLogger().println( "Undeploy: "+ data.isUndeploy());
        logMessage(getListener(), data.getResolvedInputFile1Path(), "Input file 1");
        logMessage(getListener(), data.getResolvedInputFile2Path(), "Input file 2");
        logMessage(getListener(), data.getResolvedActiveConfig(), "Active Config");
        logMessage(getListener(), data.getResolvedDataFiles(), "Data File");

        //Mandatory inputs
        multipartEntityBuilder.addTextBody("config", data.getConfig());

        //Optional inputs
        if(StringUtils.isNotBlank(String.valueOf(data.isDeploy()))) {
            multipartEntityBuilder.addTextBody("deploy", String.valueOf(data.isDeploy()));
        }
       try {
           if (StringUtils.isNotBlank(data.getResolvedInputFile1Path())) {
               Utils.addBodyPart(getWorkspace(), multipartEntityBuilder, "inputFile1", data.getResolvedInputFile1Path().trim());
           }
           if (StringUtils.isNotBlank(data.getResolvedInputFile2Path())) {
               Utils.addBodyPart(getWorkspace(), multipartEntityBuilder, "inputFile2", data.getResolvedInputFile2Path().trim());
           }
           if (StringUtils.isNotBlank(data.getResolvedActiveConfig())) {
               Utils.addBodyPart(getWorkspace(), multipartEntityBuilder, "activeConfig", data.getResolvedActiveConfig().trim());
           }
           if (StringUtils.isNotBlank(data.getResolvedDataFiles())) {
               Utils.addBodyPart(getWorkspace(), multipartEntityBuilder, "dataFile", data.getResolvedDataFiles().trim());
           }
       }catch(InvalidInputException re){
           String msg = re.getMessage();
           getListener().getLogger().println( String.format("File %s is not present in the workspace of the job", msg));
           throw new FileNotFoundException(String.format("Cannot located file with relative path %s in workspace of job", msg));
       }
        if(StringUtils.isNotBlank(data.getSwaggerurl())) {
            multipartEntityBuilder.addTextBody("swaggerurl", data.getSwaggerurl());
            getListener().getLogger().println( "Swagger url: "+ data.getSwaggerurl().trim());
        }
        if(StringUtils.isNotBlank(data.getRamlurl())) {
            multipartEntityBuilder.addTextBody("ramlurl", data.getRamlurl().trim());
            getListener().getLogger().println( "Raml url: "+ data.getRamlurl().trim());
        }
        if(StringUtils.isNotBlank(data.getWadlurl())) {
            multipartEntityBuilder.addTextBody("wadlurl", data.getWadlurl().trim());
            getListener().getLogger().println( "Wadl url: "+ data.getWadlurl().trim());
        }
        return multipartEntityBuilder.build();
    }




    public void logMessage( TaskListener listener, String file, String filename){
        if(file !=null && !file.isEmpty()){
            listener.getLogger().println(filename+" path is  - "+file);
        }else{
            listener.getLogger().println(filename+" is not provided");
        }
    }
}
