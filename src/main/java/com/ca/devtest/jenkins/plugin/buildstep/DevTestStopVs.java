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

import com.ca.devtest.jenkins.plugin.DevTestPluginConfiguration;
import com.ca.devtest.jenkins.plugin.Messages;
import com.ca.devtest.jenkins.plugin.config.RestClient;
import com.ca.devtest.jenkins.plugin.constants.APIEndpoints;
import com.ca.devtest.jenkins.plugin.util.URLFactory;
import com.ca.devtest.jenkins.plugin.util.Utils;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

/**
 * Build step for stopping virtual service.
 *
 * @author jakro01
 */
public class DevTestStopVs extends DefaultBuildStep {

	private String vseName;
	private List<String> vsNames;
	private String separator;

	/**
	 * Constructor.
	 *
	 * @param useCustomRegistry if we are overriding default Registry
	 * @param host              host for custom Registry
	 * @param port              port for custom Registry
	 * @param vseName           name of VSE where we want to undeploy the VS
	 * @param vsNames           names of the VSs delimetered by ,
	 * @param tokenCredentialId credentials token id
	 * @param secured           if https should be for custom registry
	 */
	@DataBoundConstructor
	public DevTestStopVs(boolean useCustomRegistry, String host, String port, String vseName,
			String vsNames, String tokenCredentialId, boolean secured, boolean trustAnySSLCertificate) {
		super(useCustomRegistry, host, port, tokenCredentialId, secured, trustAnySSLCertificate);
		this.vseName = vseName;
		if (vsNames != null && !vsNames.isEmpty()) {
			if (vsNames.contains(", ")) {
				this.vsNames = Arrays.asList(vsNames.split("\\s*,\\s*"));
				this.separator = ",";
			} else {
				this.vsNames = Arrays.asList(vsNames.split("\\s*\n\\s*"));
				this.separator = "\n";
			}
		} else {
			this.vsNames = new ArrayList();
		}

	}

	public String getVseName() {
		return vseName;
	}

	public String getVsNames() {
		return StringUtils.join(vsNames, separator);
	}

	@Override
	public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace,
			@Nonnull Launcher launcher, @Nonnull TaskListener listener)
			throws InterruptedException, IOException {
		Utils.checkRegistryEndpoint(this);
		this.updateCredentails(run);

		String currentHost = isUseCustomRegistry() ? super.getHost()
				: DevTestPluginConfiguration.get().getHost();
		currentHost = Utils.resolveParameter(currentHost, run, listener);

		String currentPort = isUseCustomRegistry() ? super.getPort()
				: DevTestPluginConfiguration.get().getPort();
		currentPort = Utils.resolveParameter(currentPort, run, listener);

		String currentUsername = isUseCustomRegistry() ? super.getUsername()
				: DevTestPluginConfiguration.get().getUsername();
		String currentPassword = isUseCustomRegistry() ? super.getPassword().getPlainText()
				: DevTestPluginConfiguration.get().getPassword().getPlainText();

		String currentProtocol;
		if (isUseCustomRegistry()) {
			currentProtocol = isSecured() ? "https" : "http";
		} else {
			currentProtocol = DevTestPluginConfiguration.get().isSecured() ? "https" : "http";
		}
		boolean trustAnySSLCertificate = isUseCustomRegistry() ? super.isTrustAnySSLCertificate(): DevTestPluginConfiguration.get().isTrustAnySSLCertificate();

		if (vseName == null || vseName.isEmpty()) {
			throw new AbortException(Messages.DevTestPlugin_emptyVseName());
		}
		if (vsNames == null || vsNames.isEmpty()) {
			throw new AbortException(Messages.DevTestPlugin_emptyVsNames());
		}

		String resolvedVseName = Utils.resolveParameter(vseName, run, listener);

		List<String> resolvedVsNames = Utils.resolveParameters(vsNames, run, listener);

		for (String vsName : resolvedVsNames) {
			listener.getLogger()
							.println(Messages.DevTestStopVs_stopping(vsName, resolvedVseName));
			listener.getLogger()
							.println(Messages.DevTestPlugin_devTestLocation(currentHost, currentPort));

			String urlPath = String.format(APIEndpoints.STOP_VS, resolvedVseName, vsName);

			Map<String, String> headers = Collections.singletonMap("Accept", "application/vnd.ca.lisaInvoke.virtualService+json");
			String startVSUrl  = new URLFactory(currentProtocol , currentHost, currentPort).buildUrl(urlPath);
			try(CloseableHttpResponse response = RestClient.executePost(startVSUrl , currentUsername, currentPassword, trustAnySSLCertificate, null, headers)){
				String responseBody = EntityUtils.toString(response.getEntity());
				int statusCode = response.getStatusLine().getStatusCode();
				// This is a workaround because DevTest API return 200 with empty body for wrong credentials
				if (statusCode == 200 && responseBody.isEmpty()) {
					// Invalid credentials for some REASON.....
					listener.getLogger().println(Messages.DevTestStopVs_error(vsName));
					throw new AbortException(Messages.DevTestPlugin_invalidCredentials());
				} else if (statusCode == 200) {
					// Everything is ok
					listener.getLogger().println(Messages.DevTestPlugin_responseBody(responseBody));
					listener.getLogger().println(Messages.DevTestStopVs_success(vsName, resolvedVseName));
				} else {
					// Some other problem
					listener.getLogger().println(Messages.DevTestStopVs_error(vsName));
					throw new AbortException(Messages.DevTestPlugin_responseStatus(statusCode, responseBody));
				}
			}
		}
	}

	// Localization -> Messages generated by maven plugin check target folder
	@Symbol("svStopVirtualService")
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
		 * Checker for VS name input.
		 *
		 * @param vsNames VS names
		 *
		 * @return form validation
		 */
		public FormValidation doCheckVsNames(@QueryParameter String vsNames) {
			if (vsNames.length() == 0) {
				return FormValidation
						.error(Messages.DevTestPlugin_DescriptorImpl_MissingVs());
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
		 * build step for stopping VS.
		 *
		 * @return name of build step
		 */
		@Override
		public String getDisplayName() {
			return Messages.DevTestStopVs_DescriptorImpl_DisplayName();
		}
	}
}
