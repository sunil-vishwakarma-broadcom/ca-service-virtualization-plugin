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

package com.ca.devtest.jenkins.plugin;

import static com.ca.devtest.jenkins.plugin.util.Utils.createBasicAuthHeader;

import com.ca.devtest.jenkins.plugin.util.Utils;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.Extension;
import hudson.Util;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import java.io.IOException;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Main point for DevTest plugin in jenkins used by all build and post-build steps. This class
 * works with global configuration of DevTest plugin in jenkins. Holding all information specified
 * in global configuration.
 *
 * @author jakro01
 */
@Extension
public class DevTestPluginConfiguration extends JobProperty<Job<?, ?>> {

	/**
	 * Returns {@link DevTestPluginDescriptor} for this Jenkins component for this class.
	 *
	 * @return descriptor
	 */
	public static DevTestPluginDescriptor get() {
		return (DevTestPluginDescriptor) Jenkins.getInstance()
																						.getDescriptor(DevTestPluginConfiguration.class);
	}

	/**
	 * Returns {@link DevTestPluginDescriptor} for this Jenkins component object.
	 *
	 * @return descriptor
	 */
	@Override
	public DevTestPluginDescriptor getDescriptor() {
		return (DevTestPluginDescriptor) Jenkins.getInstance().getDescriptor(getClass());
	}

	@Extension
	public static final class DevTestPluginDescriptor extends JobPropertyDescriptor {

		private String host = "";
		private String port = "";
		private String resolvedHost = "";
		private String resolvedPort = "";
		private String username = "";
		private Secret password;
		private String tokenCredentialId;

		/**
		 * Constructor.
		 */
		public DevTestPluginDescriptor() {
			super(DevTestPluginConfiguration.class);
			load();
		}

		/**
		 * Constructor.
		 *
		 * @param host hostname of default Registry
		 * @param port port of default Registry
		 */
		@DataBoundConstructor
		public DevTestPluginDescriptor(String host, String port) {
			this.host = host;
			this.port = port;

			this.resolvedHost = Utils.resolveParameterFromGlobalConfiguration(host);
			this.resolvedPort = Utils.resolveParameterFromGlobalConfiguration(port);
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			req.bindJSON(this, formData);
			this.host = formData.getJSONObject("DevTestPluginConfiguration").getString("host");
			this.port = formData.getJSONObject("DevTestPluginConfiguration").getString("port");
			this.tokenCredentialId = formData.getJSONObject("DevTestPluginConfiguration")
																			 .getString("tokenCredentialId");

			this.resolvedHost = Utils.resolveParameterFromGlobalConfiguration(host);
			this.resolvedPort = Utils.resolveParameterFromGlobalConfiguration(port);
			StandardUsernamePasswordCredentials credentials = Utils
					.lookupCredentials(this.tokenCredentialId);
			if (credentials != null) {
				this.username = credentials.getUsername();
				this.password = credentials.getPassword();
			} else {
				this.username = null;
				this.password = null;
			}

			save();
			return super.configure(req, formData);
		}

		/**
		 * Test connection to DevTest Registry.
		 *
		 * @param host              host
		 * @param port              port
		 * @param tokenCredentialId tokenCredentialId
		 *
		 * @return form validation
		 */
		public FormValidation doTestConnection(@QueryParameter("host") final String host,
				@QueryParameter("port") final String port,
				@QueryParameter("tokenCredentialId") final String tokenCredentialId) {

			try (CloseableHttpClient client = HttpClients.createDefault()) {
				String testingResolvedHost = Utils.resolveParameterFromGlobalConfiguration(host);
				String testingResolvedPort = Utils.resolveParameterFromGlobalConfiguration(port);

				HttpGet httpGet = new HttpGet(
						"http://" + testingResolvedHost + ":" + testingResolvedPort + "/api/Dcm/");

				StandardUsernamePasswordCredentials credentials = Utils
						.lookupCredentials(tokenCredentialId);

				if (credentials == null) {
					return FormValidation.error(Messages.DevTestPlugin_DescriptorImpl_requiredCred());
				}
				String username = credentials.getUsername();
				String password = Secret.toString(credentials.getPassword());

				httpGet.addHeader("Authorization", createBasicAuthHeader(username, password));
				httpGet.addHeader("Accept", "application/vnd.ca.lisaInvoke.dcm+json");
				CloseableHttpResponse response = client.execute(httpGet);

				int statusCode = response.getStatusLine().getStatusCode();
				String responseBody = EntityUtils.toString(response.getEntity());
				if (statusCode == 200) {
					if (responseBody.isEmpty()) {
						return FormValidation.error(Messages.DevTestPlugin_invalidCredentials());
					} else {
						return FormValidation.ok(Messages.DevTestPlugin_success());
					}
				} else {
					return FormValidation
							.error(Messages.DevTestPlugin_connectionFailed(statusCode, responseBody));
				}
			} catch (IOException e) {
				return FormValidation.error(Messages.DevTestPlugin_clientFailed(e.getMessage()));
			}
		}

		/**
		 * Method used for setting name of the component in Jenkins GUI. In this case it is name of
		 * section in global configuration.
		 *
		 * @return name of section for configuring DevTest plugin in global configuration
		 */
		@Override
		public String getDisplayName() {
			return Messages.DevTestPlugin_DescriptorImpl_DisplayName();
		}

		public String getResolvedHost() {
			return resolvedHost;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public String getResolvedPort() {
			return resolvedPort;
		}

		public String getPort() {
			return port;
		}

		public void setPort(String port) {
			this.port = port;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public Secret getPassword() {
			return password;
		}

		public void setPassword(Secret password) {
			this.password = password;
		}

		public String getTokenCredentialId() {
			return tokenCredentialId;
		}

		public void setTokenCredentialId(String tokenCredentialId) {
			this.tokenCredentialId = Util.fixEmpty(tokenCredentialId);
		}

		/**
		 * Method used to fill dropdown with credential tokens.
		 *
		 * @return listbox model
		 */
		public ListBoxModel doFillTokenCredentialIdItems() {
			if (!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER)) {
				return new ListBoxModel();
			}
			return new StandardListBoxModel()
					.withEmptySelection()
					.withAll(CredentialsProvider.lookupCredentials(
							StandardUsernamePasswordCredentials.class,
							Jenkins.getInstance(),
							ACL.SYSTEM)
					);
		}

	}
}
