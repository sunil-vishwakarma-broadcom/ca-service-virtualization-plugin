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
import com.ca.devtest.jenkins.plugin.data.CreateAndDeployVsData;
import com.ca.devtest.jenkins.plugin.data.DevTestReturnValue;
import com.ca.devtest.jenkins.plugin.util.MyFileCallable;
import hudson.FilePath;
import hudson.model.TaskListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;

import hudson.AbortException;

import static com.ca.devtest.jenkins.plugin.util.Utils.createBasicAuthHeader;

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
<<<<<<< HEAD
    public DevTestReturnValue call() throws RuntimeException{
        DevTestReturnValue devTestReturnValue = new DevTestReturnValue();
        try{
            String nodeName = InetAddress.getLocalHost().getHostName();
            getListener().getLogger().println("\n> Executing task on node "+ nodeName);
            devTestReturnValue.setNode(nodeName);
            //undeploy here
            if (Boolean.valueOf(data.isUndeploy())) {
=======
    public DevTestReturnValue call() throws RuntimeException {
        DevTestReturnValue devTestReturnValue = new DevTestReturnValue();
        try {

            getListener().getLogger().println("\n> Executing task on node "+ InetAddress.getLocalHost().getHostName());
            devTestReturnValue.setNode(InetAddress.getLocalHost().getHostName());
            //undeploy here
            if(Boolean.valueOf(data.isUndeploy())) {
>>>>>>> ee1b9e1 (Added support for Create and Deploy VS step)
                handleUndeploy();
            }
            if(data.isDeploy()) getListener().getLogger().print(Messages.DevTestCreateAndDeployVs_creatingAndDeploying());
            else getListener().getLogger().print(Messages.DevTestCreateAndDeployVs_creating());

            getListener().getLogger().println(" " +Messages.DevTestPlugin_devTestLocation(data.getRegistry(), data.getPort()));

            HttpPost httpPost = new HttpPost(data.getProtocol() + data.getRegistry() + ":" + data.getPort() + data.getAPIUrl());
            httpPost.addHeader("Authorization", createBasicAuthHeader(data.getUsername(), data.getPassword()));
            httpPost.addHeader("Accept", "application/json");


            HttpEntity entity = createPostEntity();
            if(entity != null) httpPost.setEntity(entity);

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(httpPost)) {

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
        } catch(RuntimeException e){
            devTestReturnValue.setMessage(e.getMessage());
        } catch(Exception e){
            devTestReturnValue.setMessage(e.getMessage());
        }
        return devTestReturnValue;
    }

    /** Handle undeploy of existing virtual service **/
    private void handleUndeploy()  throws IOException, InterruptedException {
        JsonElement jelement = new JsonParser().parse(data.getConfig());
        JsonObject  jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("virtualService");
        String vsName = jobject.get("name").getAsString();
        getListener().getLogger().println("Undeploy if virtual service '"+ vsName+"' is running on VSE '" + data.getVse() + "'");

        HttpDelete httpDelete = createUndeployPostEntity(vsName);

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(httpDelete)) {
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

    private HttpDelete createUndeployPostEntity(String vsName) throws IOException, InterruptedException {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setContentType(ContentType.MULTIPART_FORM_DATA);
        String urlPath = "/api/Dcm/VSEs/" + data.getVse() + "/" + vsName + "/";
        HttpDelete httpDelete = new HttpDelete(
                data.getProtocol() + data.getRegistry() + ":" + data.getPort() + urlPath);
        httpDelete
                .addHeader("Authorization", createBasicAuthHeader(data.getUsername(), data.getPassword()));
        return httpDelete;
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
        if(StringUtils.isNotEmpty(String.valueOf(data.isDeploy()))) {
            multipartEntityBuilder.addTextBody("deploy", String.valueOf(data.isDeploy()));
        }
        if(data.getResolvedInputFile1Path() != null && StringUtils.isNotEmpty(data.getResolvedInputFile1Path().trim())) {
            addBodyPart(multipartEntityBuilder, "inputFile1", data.getResolvedInputFile1Path().trim());
        }
        if(data.getResolvedInputFile2Path() != null && StringUtils.isNotEmpty(data.getResolvedInputFile2Path().trim())) {
            addBodyPart(multipartEntityBuilder, "inputFile2", data.getResolvedInputFile2Path().trim());
        }
        if(data.getResolvedActiveConfig() != null && StringUtils.isNotEmpty(data.getResolvedActiveConfig().trim())) {
            addBodyPart(multipartEntityBuilder, "activeConfig", data.getResolvedActiveConfig().trim());
        }
        if(data.getResolvedDataFiles() != null && StringUtils.isNotEmpty(data.getResolvedDataFiles().trim())) {
            addBodyPart(multipartEntityBuilder, "dataFile", data.getResolvedDataFiles().trim());
        }
        if(data.getSwaggerurl() != null && StringUtils.isNotEmpty(data.getSwaggerurl().trim())) {
            multipartEntityBuilder.addTextBody("swaggerurl", data.getSwaggerurl().trim());
            getListener().getLogger().println( "Swagger url: "+ data.getSwaggerurl().trim());
        }
        if(data.getRamlurl() != null && StringUtils.isNotEmpty(data.getRamlurl().trim())) {
            multipartEntityBuilder.addTextBody("ramlurl", data.getRamlurl().trim());
            getListener().getLogger().println( "Raml url: "+ data.getRamlurl().trim());
        }
        if(data.getWadlurl() != null && StringUtils.isNotEmpty(data.getWadlurl().trim())) {
            multipartEntityBuilder.addTextBody("wadlurl", data.getWadlurl().trim());
            getListener().getLogger().println( "Wadl url: "+ data.getWadlurl().trim());
        }
        return multipartEntityBuilder.build();
    }

    /**
     * Creating and adding body part from resolvedInputFilePath in MultipartEntityBuilder
     * @param multipartEntityBuilder
     * @param name
     * @param resolvedInputFilePath
     * @throws IOException
     * @throws InterruptedException
     */
    private void addBodyPart(MultipartEntityBuilder multipartEntityBuilder,String name, String resolvedInputFilePath)
            throws IOException, InterruptedException {
        if (StringUtils.containsIgnoreCase(resolvedInputFilePath, "file:///") ||
                StringUtils.containsIgnoreCase(resolvedInputFilePath, "http://") ||
                StringUtils.containsIgnoreCase(resolvedInputFilePath, "https://")) {
            URL url = new URL(resolvedInputFilePath);
            File file = new File(url.getFile());

            //Copying content from URL to file iff using remote file (i.e. having http:// or https:// in the file path)
            if(StringUtils.containsIgnoreCase(resolvedInputFilePath, "http://") ||
                    StringUtils.containsIgnoreCase(resolvedInputFilePath, "https://"))
                FileUtils.copyURLToFile(url, file);

            //Creating FormBodyPart from File
            FormBodyPart bodyPart = FormBodyPartBuilder.create().setName(name)
                    .setBody(new FileBody(file)).build();
            multipartEntityBuilder.addPart(bodyPart);
        } else {
            FilePath inputFile = getWorkspace().child(resolvedInputFilePath);
            File file = inputFile.act(new MyFileCallable());
            if (file != null) {
                ContentBody cbody = null;
                if (inputFile.isRemote()) {
                    //we need to read remote file in advance to be available for httpclient
                    byte[] fileData = IOUtils.toByteArray(inputFile.read());
                    cbody = new ByteArrayBody(fileData, resolvedInputFilePath);
                } else {
                    cbody = new FileBody(file);
                }
                multipartEntityBuilder.addPart(name, cbody);
            } else {
                getListener().getLogger()
                        .println("File " + resolvedInputFilePath + " is not present in the workspace of job");
                throw new FileNotFoundException(
                        "Cannot located file with relative path " + resolvedInputFilePath + " in workspace of job");
            }
        }
    }

    public void logMessage( TaskListener listener, String file, String filename){
        if(file !=null && !file.isEmpty()){
            listener.getLogger().println(filename+" path is  - "+file);
        }else{
            listener.getLogger().println(filename+" is not provided");
        }
    }
}
