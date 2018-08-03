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
import com.ca.devtest.jenkins.plugin.util.Utils;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Build step for starting virtual service.
 *
 * @author jakro01
 */
public class DevTestStartVs extends DefaultBuildStep {

	private String vseName;
	private List<String> vsNames;


	/**
	 * Constructor.
	 *
	 * @param useCustomRegistry if we are overriding default Registry
	 * @param host              host for custom Registry
	 * @param port              port for custom Registry
	 * @param vseName           name of VSE where we want to undeploy the VS
	 * @param vsNames           names of the VSs delimetered by ,
	 * @param tokenCredentialId credentials token id
	 */
	@DataBoundConstructor
	public DevTestStartVs(boolean useCustomRegistry, String host, String port, String vseName,
			String vsNames, String tokenCredentialId) {
		super(useCustomRegistry, host, port, tokenCredentialId);
		this.vseName = vseName;
		if (vsNames != null && !vsNames.isEmpty()) {
			if (vsNames.contains(",")) {
				this.vsNames = Arrays.asList(vsNames.split("\\s*,\\s*"));
			} else {
				this.vsNames = Arrays.asList(vsNames.split("\\s*\n\\s*"));
			}
		} else {
			this.vsNames = new ArrayList<String>();
		}
	}

	public String getVseName() {
		return vseName;
	}

	public String getVsNames() {
		return StringUtils.join(vsNames, "\n");
	}

	@Override
	public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace,
			@Nonnull Launcher launcher, @Nonnull TaskListener listener)
			throws InterruptedException, IOException {
		Utils.checkRegistryEndpoint(this);
		String currentHost = isUseCustomRegistry() ? super.getHost()
				: DevTestPluginConfiguration.get().getResolvedHost();
		currentHost = Utils.resolveParameter(currentHost, run, listener);

		String currentPort = isUseCustomRegistry() ? super.getPort()
				: DevTestPluginConfiguration.get().getResolvedPort();
		currentPort = Utils.resolveParameter(currentPort, run, listener);

		String currentUsername = isUseCustomRegistry() ? super.getUsername()
				: DevTestPluginConfiguration.get().getUsername();
		String currentPassword = isUseCustomRegistry() ? super.getPassword().getPlainText()
				: DevTestPluginConfiguration.get().getPassword().getPlainText();

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
							.println(Messages.DevTestStartVs_starting(vsName, resolvedVseName));
			listener.getLogger()
							.println(Messages.DevTestPlugin_devTestLocation(currentHost, currentPort));

			String urlPath = "/api/Dcm/VSEs/" + resolvedVseName + "/" + vsName + "/actions/start/";

			HttpPost httpPost = new HttpPost("http://" + currentHost + ":" + currentPort + urlPath);
			httpPost.addHeader("Authorization", createBasicAuthHeader(currentUsername, currentPassword));
			httpPost.addHeader("Accept", "application/vnd.ca.lisaInvoke.virtualService+json");

			try (CloseableHttpClient client = HttpClients.createDefault();
					CloseableHttpResponse response = client.execute(httpPost)) {

				String responseBody = EntityUtils.toString(response.getEntity());
				int statusCode = response.getStatusLine().getStatusCode();

				// This is a workaround because DevTest API return 200 with empty body for wrong credentials
				if (statusCode == 200 && responseBody.isEmpty()) {
					// Invalid credentials for some REASON.....
					listener.getLogger().println(Messages.DevTestStartVs_error(vsName));
					throw new AbortException(Messages.DevTestPlugin_invalidCredentials());
				} else if (statusCode == 200) {
					// Everything is ok
					listener.getLogger().println(Messages.DevTestPlugin_responseBody(responseBody));
					listener.getLogger().println(Messages.DevTestStartVs_success(vsName, resolvedVseName));
				} else {
					// Some other problem
					listener.getLogger().println(Messages.DevTestStartVs_error(vsName));
					throw new AbortException(Messages.DevTestPlugin_responseStatus(statusCode, responseBody));
				}
			}
		}
	}

	// Localization -> Messages generated by maven plugin check target folder
	@Symbol("svStartVirtualService")
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
		 * @param vsNames VS name
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
		public FormValidation doCheckHost(@QueryParameter boolean useCustomRegistry,
				@QueryParameter String host, @QueryParameter String port,
				@QueryParameter String tokenCredentialId) {
			return Utils.doCheckHost(useCustomRegistry, host, port, tokenCredentialId);
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> abstractClass) {
			return true;
		}

		/**
		 * Method used for setting name of the component in Jenkins GUI. In this case it is name of
		 * build step for starting VS.
		 *
		 * @return name of build step
		 */
		@Override
		public String getDisplayName() {
			return Messages.DevTestStartVs_DescriptorImpl_DisplayName();
		}
	}
}
