/******************************************************************************
 *
 * Copyright (c) 2018 CA.  All rights reserved.
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

import com.ca.devtest.jenkins.plugin.data.DeployMarData;
import com.ca.devtest.jenkins.plugin.util.MyFileCallable;
import com.ca.devtest.jenkins.plugin.data.DevTestReturnValue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.io.*;
import hudson.model.TaskListener;
import org.apache.http.HttpEntity;
import hudson.FilePath;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.ca.devtest.jenkins.plugin.Messages;
import java.net.InetAddress;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import static com.ca.devtest.jenkins.plugin.util.Utils.createBasicAuthHeader;

/**
 * Class used for executing the jobs on slave
 */
public class DevTestDeployVsCallable extends AbstractDevTestMasterToSlaveCallable {

    private static final long serialVersionUID = 1L;

    DeployMarData data;
    public DevTestDeployVsCallable(FilePath workspace, TaskListener listner, DeployMarData data){
        super(workspace, listner);
        this.data = data;
    }

    @Override
    public DevTestReturnValue call() {
            List<String> marFiles = data.getMarFiles();
            DevTestReturnValue devTestReturnValue = new DevTestReturnValue();

        try {
                getListener().getLogger().println("Executing task on node "+InetAddress.getLocalHost().getHostName());
                devTestReturnValue.setNode(InetAddress.getLocalHost().getHostName());
                for (String marFilePath : marFiles) {
                    getListener().getLogger().println(Messages.DevTestDeployVs_deploying(marFilePath));
                    getListener().getLogger()
                            .println(Messages.DevTestPlugin_devTestLocation(data.getRegistry(),
                                    data.getPort()));

                    HttpEntity entity = createPostEntity(getWorkspace(), getListener(), marFilePath);
                    HttpPost httpPost = new HttpPost(data.getProtocol() + data.getRegistry() + ":" + data.getPort() + data.getAPIUrl());
                    httpPost.addHeader("Authorization", createBasicAuthHeader(data.getUsername(), data.getPassword()));
                    httpPost.addHeader("Accept", "application/vnd.ca.lisaInvoke.virtualService+json");
                    httpPost.setEntity(entity);

                    try (CloseableHttpClient client = HttpClients.createDefault();
                         CloseableHttpResponse response = client.execute(httpPost)) {

                        int statusCode = response.getStatusLine().getStatusCode();
                        String responseBody = EntityUtils.toString(response.getEntity());

                        if (statusCode == 201) {
                            getListener().getLogger().println(Messages.DevTestPlugin_responseBody(responseBody));
                            getListener().getLogger().println(Messages.DevTestDeployVs_success(marFilePath));
                            devTestReturnValue.setMessage("Marfile '" + marFilePath + "' deployed sucessfully");
                            devTestReturnValue.setSuccess(true);
                        } else {
                            getListener().getLogger().println(Messages.DevTestDeployVs_error());
                            if (statusCode == 200) {
                                devTestReturnValue.setMessage(Messages.DevTestPlugin_invalidCredentials());
                            } else {
                                devTestReturnValue.setMessage(Messages.DevTestPlugin_responseStatus(statusCode, responseBody));
                            }
                            devTestReturnValue.setSuccess(false);
                            return devTestReturnValue;
                        }
                    }
                }
        } catch(RuntimeException e){
            devTestReturnValue.setMessage(e.getMessage());
        } catch(Exception e){
            devTestReturnValue.setMessage(e.getMessage());
        }
        return devTestReturnValue;
    }

    /**
     * Creates entity for HTTP Post request depending on provided format of path to MAR file. If path
     * to MAR file contains http or file prefix than form with fileURI property is used and this means
     * that DevTest will look for MAR file on the provided HTTP link or on its filesystem. When no
     * prefix is part of the path then MAR file is located in workspace of the job and we upload it as
     * part of request with file property.
     *
     * @param workspace workspace of job
     * @param listener  listener
     *
     * @return created {@link HttpEntity} for deploying VS
     */
    private HttpEntity createPostEntity(FilePath workspace, TaskListener listener, String marFilePath)
            throws IOException, InterruptedException {
        if (StringUtils.containsIgnoreCase(marFilePath, "file") || StringUtils
                .containsIgnoreCase(marFilePath, "http")) {
            FormBodyPart bodyPart = FormBodyPartBuilder.create().setName("fileURI")
                    .setBody(new StringBody(marFilePath)).build();
            return MultipartEntityBuilder.create().addPart(bodyPart).build();
        } else {
            FilePath marFile = workspace.child(marFilePath);
            File file = marFile.act(new MyFileCallable());
            if (file != null) {
                ContentBody cbody = null;
                if (marFile.isRemote()) {
                    listener.getLogger()
                            .println("File read remote mar file in node callable");
                    //we need to read remote file in advance to be available for httpclient
                    byte[] filedata = IOUtils.toByteArray(marFile.read());
                    cbody = new ByteArrayBody(filedata, marFilePath);
                } else {
                    cbody = new FileBody(file);
                }
                return MultipartEntityBuilder.create()
                        .addPart("file", cbody)
                        .build();
            } else {
                listener.getLogger()
                        .println("File " + marFilePath + " is no present in the workspace of job on machine "+InetAddress.getLocalHost().getHostName());
                throw new FileNotFoundException(
                        "Cannot located file with relative path " + marFilePath + " in workspace of job");
            }
        }
    }

}
