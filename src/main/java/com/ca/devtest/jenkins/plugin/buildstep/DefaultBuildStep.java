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
 *
 ******************************************************************************/

package com.ca.devtest.jenkins.plugin.buildstep;

import com.ca.devtest.jenkins.plugin.util.Utils;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.util.Secret;
import java.io.IOException;
import javax.annotation.Nonnull;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.AncestorInPath;

/**
 * Parent build step for all specific build steps holding attributes of custom registry.
 *
 * @author jakro01
 */
public abstract class DefaultBuildStep extends Builder implements SimpleBuildStep {

	private boolean useCustomRegistry;
	private final String host;
	private final String port;
	private  String username;
	private  Secret password;
	private final String tokenCredentialId;
	private boolean secured;

	/**
	 * Constructor.
	 *
	 * @param useCustomRegistry if we are overriding default Registry
	 * @param host              host for custom Registry
	 * @param port              port for custom Registry
	 * @param tokenCredentialId credentials token id
	 * @param secured           if https should be for custom registry
	 */
	public DefaultBuildStep(boolean useCustomRegistry, String host, String port,
			String tokenCredentialId, boolean secured) {
		this.useCustomRegistry = useCustomRegistry;
		this.host = host;
		this.port = port;
		this.tokenCredentialId = tokenCredentialId;
		this.secured = secured;
		StandardUsernamePasswordCredentials jenkinsCredentials = Utils
				.lookupCredentials(null, this.tokenCredentialId);
		if (jenkinsCredentials != null) {
			this.username = jenkinsCredentials.getUsername();
			this.password = jenkinsCredentials.getPassword();
		} else {
			username = "";
			password = Secret.fromString("");
		}
	}

	public boolean isUseCustomRegistry() {
		return useCustomRegistry;
	}

	public String getHost() {
		return host;
	}

	public String getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public Secret getPassword() {
		return password;
	}

	public String getTokenCredentialId() {
		return tokenCredentialId;
	}

	public boolean isSecured() {
		return secured;
	}

	protected void updateCredentails(Run<?, ?> run){
		StandardUsernamePasswordCredentials jenkinsCredentials = Utils
				.lookupCredentials(run.getParent(), tokenCredentialId);
		if (jenkinsCredentials != null) {
			this.username = jenkinsCredentials.getUsername();
			this.password = jenkinsCredentials.getPassword();
		}
	}

	@Override
	public abstract void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace,
			@Nonnull Launcher launcher, @Nonnull TaskListener listener)
			throws InterruptedException, IOException;
}
