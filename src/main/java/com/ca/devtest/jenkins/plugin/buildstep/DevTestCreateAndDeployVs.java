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

package com.ca.devtest.jenkins.plugin.buildstep;

import com.ca.devtest.jenkins.plugin.DevTestPluginConfiguration;
import com.ca.devtest.jenkins.plugin.Messages;
import com.ca.devtest.jenkins.plugin.data.CreateAndDeployVsData;
import com.ca.devtest.jenkins.plugin.data.DevTestReturnValue;
import com.ca.devtest.jenkins.plugin.slavecallable.DevTestCreateAndDeployVsCallable;
import com.ca.devtest.jenkins.plugin.util.Utils;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Item;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;

<<<<<<< HEAD
import hudson.remoting.VirtualChannel;
=======
>>>>>>> ee1b9e1 (Added support for Create and Deploy VS step)
import hudson.util.FormValidation;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.AncestorInPath;

import java.io.IOException;

/**
 * Build step for creating and deploying virtual service.
 *
 * @author sv673714
 */
public class DevTestCreateAndDeployVs extends DefaultBuildStep {

	private String vseName;
	private String config;
	private String deploy;
	private String undeploy;
	private String inputFile1;
	private String inputFile2;
	private String activeConfig;
	private String dataFiles;
	private String swaggerurl;
	private String ramlurl;
	private String wadlurl;

	/**
	 * Constructor.
	 *
	 * @param useCustomRegistry if we are overriding default Registry
	 * @param host              host for custom Registry
	 * @param port              port for custom Registry
	 * @param vseName           name of VSE where we want to create and deploy the VS
	 * @param config            configuration JSON contains virtual service, transport protocol, data protocol details
	 * @param deploy            flag to deploy created service to VSE
	 * @param undeploy          flag to undeploy service with same name if already deployed
	 * @param inputFile1        path to input file to be uploaded
	 * @param inputFile2        path to input file to be uploaded
	 * @param activeConfig      path to active config file to be uploaded
	 * @param dataFiles         patch to data files to be uploaded.
	 * @param tokenCredentialId credentials token id
	 * @param secured           if https should be for custom registry
	 */
	@DataBoundConstructor
	public DevTestCreateAndDeployVs(boolean useCustomRegistry, String host, String port, String vseName,
									String config, String deploy, String undeploy, String inputFile1, String inputFile2, String activeConfig, String dataFiles,
									String swaggerurl, String ramlurl, String wadlurl, String tokenCredentialId,
									boolean secured) {
		super(useCustomRegistry, host, port, tokenCredentialId, secured);
		this.vseName = vseName;
		this.config = config;
		this.deploy = deploy;
		this.undeploy = undeploy;
		this.inputFile1 = inputFile1;
		this.inputFile2 = inputFile2;
		this.activeConfig = activeConfig;
		this.dataFiles = dataFiles;
		this.swaggerurl = swaggerurl;
		this.ramlurl = ramlurl;
		this.wadlurl = wadlurl;
	}

	public String getVseName() {
		return vseName;
	}

	public String getConfig() {
		return config;
	}

	public String getDeploy() {
		return deploy;
	}

	public String getUndeploy() {
		return undeploy;
	}

	public void setUndeploy(String undeploy) {
		this.undeploy = undeploy;
	}

	public String getInputFile1() {
		return inputFile1;
	}

	public String getInputFile2() {
		return inputFile2;
	}

	public String getActiveConfig() {
		return activeConfig;
	}

	public String getDataFiles() {
		return dataFiles;
	}

	public String getSwaggerurl() {
		return swaggerurl;
	}

	public String getRamlurl() {
		return ramlurl;
	}

	public String getWadlurl() {
		return wadlurl;
	}

	@Override
	public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		Utils.checkRegistryEndpoint(this);
		this.updateCredentails(run);

		if (vseName == null || vseName.isEmpty()) {
			throw new AbortException("VSE name cannot be empty!");
		}
		if (config == null || config.isEmpty()) {
			throw new AbortException("Configuration JSON cannot be empty!");
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
		String urlPath = "/lisa-virtualize-invoke/api/v3/vses/" + resolvedVseName + "/services";

		String resolvedConfig = Utils.resolveParameter(getConfig(), run, listener);
		String resolvedDeploy = Utils.resolveParameter(getDeploy(), run, listener);
		String resolvedUndeploy = Utils.resolveParameter(getUndeploy(), run, listener);
		String resolvedInputFile1Path = Utils.resolveParameter(getInputFile1(), run, listener);
		String resolvedInputFile2Path = Utils.resolveParameter(getInputFile2(), run, listener);
		String resolvedActiveConfig = Utils.resolveParameter(getActiveConfig(), run, listener);
		String resolvedDataFiles = Utils.resolveParameter(getDataFiles(), run, listener);
		String resolvedSwaggerUrl = Utils.resolveParameter(getSwaggerurl(), run, listener);
		String resolvedRamlUrl = Utils.resolveParameter(getRamlurl(), run, listener);
		String resolvedWadlUrl = Utils.resolveParameter(getWadlurl(), run, listener);

		CreateAndDeployVsData createVsdata = new CreateAndDeployVsData(currentHost, currentPort, currentProtocol,
				currentUsername, currentPassword, resolvedVseName, urlPath, resolvedConfig, Boolean.parseBoolean(resolvedDeploy),
				Boolean.parseBoolean(resolvedUndeploy), resolvedInputFile1Path, resolvedInputFile2Path, resolvedActiveConfig,
				resolvedDataFiles, resolvedSwaggerUrl, resolvedRamlUrl, resolvedWadlUrl);

			if(launcher != null) {
				VirtualChannel channel = launcher.getChannel();
				if (channel != null) {
					DevTestReturnValue devTestReturnValue = channel.call(new DevTestCreateAndDeployVsCallable(workspace,
							listener, createVsdata));
					if (devTestReturnValue != null) {
						listener.getLogger().println("Job executed on node - " + devTestReturnValue.getNode());
						listener.getLogger().println("Invoke registry with user - " + currentUsername);

						if (devTestReturnValue.isSuccess()) {
							listener.getLogger().println(devTestReturnValue.getMessage());
							return;
						} else {
							throw new AbortException(devTestReturnValue.getMessage());
						}
					}
				}
			}
		throw new AbortException("Internal error while Creating and Deploying VS");
	}

	// Localization -> Messages generated by maven plugin check target folder
	@Symbol("svCreateAndDeployVirtualService")
	@Extension
	public static final class DescriptorImpl extends DefaultDescriptor {

		/**
		 * Checker for VSE name input.
		 *
		 * @param vseName VSE name
		 * @return form validation
		 */
		public FormValidation doCheckVseName(@QueryParameter String vseName) {
			if (vseName.length() == 0) {
				return FormValidation.error(Messages.DevTestPlugin_DescriptorImpl_MissingVse());
			}
			return FormValidation.ok();
		}

		/**
		 * Checker for Configuration JSON input.
		 *
		 * @param config configuration JSON contains virtual service, transport protocol, data protocol details
		 * @return form validation
		 */
		public FormValidation doCheckConfig(@QueryParameter String config) {
			if (config.length() == 0) {
				return FormValidation
						.error(Messages.DevTestCreateAndDeployVs_DescriptorImpl_MissingConfig());

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
		 * @return form validation
		 */
		public FormValidation doCheckHost(@AncestorInPath Item context,
										  @QueryParameter boolean useCustomRegistry,
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
		 * build step for creating VS.
		 *
		 * @return name of build step
		 */
		@Override
		public String getDisplayName() {
			return Messages.DevTestCreateAndDeployVs_DescriptorImpl_DisplayName();
		}
	}
}
