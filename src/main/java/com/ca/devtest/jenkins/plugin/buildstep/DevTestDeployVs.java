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

package com.ca.devtest.jenkins.plugin.buildstep;

import static com.ca.devtest.jenkins.plugin.util.Utils.createBasicAuthHeader;

import com.ca.devtest.jenkins.plugin.DevTestPluginConfiguration;
import com.ca.devtest.jenkins.plugin.Messages;
import com.ca.devtest.jenkins.plugin.util.MyFileCallable;
import com.ca.devtest.jenkins.plugin.util.Utils;
import com.ca.devtest.jenkins.plugin.data.DeployMarData;
import com.ca.devtest.jenkins.plugin.data.DevTestReturnValue;
import com.ca.devtest.jenkins.plugin.slavecallable.DevTestDeployVsCallable;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Item;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.util.FormValidation;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
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
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.AncestorInPath;

/**
 * Build step for deploying virtual service.
 *
 * @author jakro01
 */
public class DevTestDeployVs extends DefaultBuildStep {

	private List<String> marFilesPaths;
	private String vseName;
	//private String urlPath;
	private String separator;

	/**
	 * Constructor.
	 *
	 * @param useCustomRegistry if we are overriding default Registry
	 * @param host              host for custom Registry
	 * @param port              port for custom Registry
	 * @param vseName           name of VSE where we want to undeploy the VS
	 * @param marFilesPaths     path to mar file
	 * @param tokenCredentialId credentials token id
	 * @param secured           if https should be for custom registry
	 */
	@DataBoundConstructor
	public DevTestDeployVs(boolean useCustomRegistry, String host, String port, String vseName,
			String marFilesPaths, String tokenCredentialId, boolean secured) {
		super(useCustomRegistry, host, port, tokenCredentialId, secured);
		if (marFilesPaths != null && !marFilesPaths.isEmpty()) {
			if (marFilesPaths.contains(",")) {
				this.marFilesPaths = Arrays.asList(marFilesPaths.split("\\s*,\\s*"));
				this.separator = ", ";
			} else {
				this.marFilesPaths = Arrays.asList(marFilesPaths.split("\\s*\n\\s*"));
				this.separator = "\n";
			}
		} else {
			this.marFilesPaths = new ArrayList<>();
		}
		this.vseName = vseName;
	}

	public String getMarFilesPaths() {
		return StringUtils.join(marFilesPaths, separator);
	}

	public String getVseName() {
		return vseName;
	}

	@Override
	public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		Utils.checkRegistryEndpoint(this);
		this.updateCredentails(run);

		if (vseName == null || vseName.isEmpty()) {
			throw new AbortException("VSE name cannot be empty!");
		}
		if (marFilesPaths == null || marFilesPaths.isEmpty()) {
			throw new AbortException("Paths to MAR files cannot be empty");
		}

		String currentHost = isUseCustomRegistry() ? super.getHost()
				: DevTestPluginConfiguration.get().getHost();

		currentHost = Utils.resolveParameter(currentHost, run, listener);

		String currentPort = isUseCustomRegistry() ? super.getPort()
				: DevTestPluginConfiguration.get().getPort();

		currentPort = Utils.resolveParameter(currentPort, run, listener);

		String currentProtocol;
		if (isUseCustomRegistry()) {
			currentProtocol = isSecured() ? "https://" : "http://";
		} else {
			currentProtocol = DevTestPluginConfiguration.get().isSecured() ? "https://" : "http://";
		}

		String currentUsername = isUseCustomRegistry() ? super.getUsername()
				: DevTestPluginConfiguration.get().getUsername();
		String currentPassword = isUseCustomRegistry() ? super.getPassword().getPlainText()
				: DevTestPluginConfiguration.get().getPassword().getPlainText();

		String resolvedVseName = Utils.resolveParameter(vseName, run, listener);
		String urlPath = "/api/Dcm/VSEs/" + resolvedVseName + "/actions/deployMar/";

		List<String> resolvedMarFilesPaths = Utils.getFilesMatchingWildcardList(
				Utils.resolveParameters(marFilesPaths, run, listener), workspace.absolutize().toString());

		if (resolvedMarFilesPaths != null && currentHost!=null && currentPort !=null && currentProtocol != null &&
			currentUsername !=null && currentPassword!=null && resolvedVseName!=null) {
			DeployMarData deployMarData = new DeployMarData(currentHost, currentPort, currentProtocol,
					currentUsername, currentPassword, resolvedVseName, resolvedMarFilesPaths, urlPath);

			if ( deployMarData != null && launcher != null) {
				VirtualChannel channel = launcher.getChannel();
				if (channel != null) {
					DevTestReturnValue devTestReturnValue = channel.call(new DevTestDeployVsCallable(workspace, listener, deployMarData));
					if (devTestReturnValue != null) {
						listener.getLogger().println("Job executed on node - " + devTestReturnValue.getNode());
						if (devTestReturnValue.isSuccess()) {
							listener.getLogger().println(devTestReturnValue.getMessage());
							return;
						} else {
							throw new AbortException(devTestReturnValue.getMessage());
						}
					}
				}
			}
		}
		throw new AbortException("Internal error while Deploying VS");
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
					//we need to read remote file in advance to be available for httpclient
					byte[] data = IOUtils.toByteArray(marFile.read());
					cbody = new ByteArrayBody(data, marFilePath);
				} else {
					cbody = new FileBody(file);
				}
				return MultipartEntityBuilder.create()
																		 .addPart("file", cbody)
																		 .build();
			} else {
				listener.getLogger()
								.println("File " + marFilePath + " is no present in the workspace of job");
				throw new FileNotFoundException(
						"Cannot located file with relative path " + marFilePath + " in workspace of job");
			}
		}
	}


	// Localization -> Messages generated by maven plugin check target folder
	@Symbol("svDeployVirtualService")
	@Extension
	public static final class DescriptorImpl extends DefaultDescriptor {

		/**
		 * Checker for VSE name input.
		 *
		 * @param vseName VSE name
		 *
		 * @return form validation
		 */
		public FormValidation doCheckVseName(@QueryParameter String vseName) {
			if (vseName.length() == 0) {
				return FormValidation.error(Messages.DevTestPlugin_DescriptorImpl_MissingVse());
			}
			return FormValidation.ok();
		}

		/**
		 * Checker for MAR files paths input.
		 *
		 * @param marFilesPaths MAR files paths
		 *
		 * @return form validation
		 */
		public FormValidation doCheckMarFilesPaths(@QueryParameter String marFilesPaths) {
			if (marFilesPaths.length() == 0) {
				return FormValidation
						.error(Messages.DevTestDeployVs_DescriptorImpl_MissingMarFile());

			}

			return FormValidation.ok();
		}

		/**
		 * Checker for host inputs for Registry.
		 *
		 * @param useCustomRegistry if custom Registry endpoint should used
		 * @param host              host for Registry
		 * @param port              port for Registry
		 * @param tokenCredentialId credentials token id
		 *
		 * @return form validation
		 */
		public FormValidation doCheckHost(@AncestorInPath Item context, @QueryParameter boolean useCustomRegistry,
				@QueryParameter String host, @QueryParameter String port,
				@QueryParameter String tokenCredentialId) {
			return Utils.doCheckHost(context, useCustomRegistry, host, port, tokenCredentialId);
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> abstractClass) {
			return true;
		}

		/**
		 * Method used for setting name of the component in Jenkins GUI. In this case it is name of
		 * build step for deploying VS.
		 *
		 * @return name of build step
		 */
		@Override
		public String getDisplayName() {
			return Messages.DevTestDeployVs_DescriptorImpl_DisplayName();
		}
	}

}
