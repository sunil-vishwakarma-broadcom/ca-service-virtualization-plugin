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

import static org.junit.Assert.assertEquals;

import com.ca.devtest.jenkins.plugin.DevTestPluginConfiguration.DevTestPluginDescriptor;
import com.ca.devtest.jenkins.plugin.buildstep.AbstractDevTestBuildStepTest;
import com.ca.devtest.jenkins.plugin.buildstep.DevTestStartVs;
import com.ca.devtest.jenkins.plugin.util.Utils;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.model.FreeStyleProject;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;
import hudson.util.Secret;
import java.util.Collections;
import jenkins.model.Jenkins;
import org.junit.Test;

/**
 * File description
 *
 * @author jakro01
 */
public class DevTestPluginTest extends AbstractDevTestBuildStepTest {


	@Test
	public void testConfigurePlugin() throws Exception {
		setupJenkinsConfiguration("10.0.0.1", "1111", "user", "password", "token");
		DevTestPluginDescriptor plugin = new DevTestPluginDescriptor();

		assertEquals(plugin.getHost(), "10.0.0.1");
		assertEquals(plugin.getPort(), "1111");
		StandardUsernamePasswordCredentials jenkinsCredentials = Utils
				.lookupCredentials(null, "token");
		assertEquals(jenkinsCredentials.getUsername(), "user");
		assertEquals(jenkinsCredentials.getPassword(), Secret.fromString("password"));
	}

	@Test
	public void testBuildStepOverridesConfiguration() throws Exception {
		setupJenkinsConfiguration("10.0.0.1", "1111", "user", "password", "token");
		DevTestStartVs plugin = createPlugin(true, "127.0.0.1", "1505", "",
				"file:///C:/webservices-vs.mar", "id");

		assertEquals(plugin.getHost(), "127.0.0.1");
		assertEquals(plugin.getPort(), "1505");

		StandardUsernamePasswordCredentials jenkinsCredentials = Utils
				.lookupCredentials(null, "token");
		assertEquals(jenkinsCredentials.getUsername(), "user");
		assertEquals(jenkinsCredentials.getPassword(), Secret.fromString("password"));
	}

	private DevTestStartVs createPlugin(boolean useCustomRegistry, String host, String port,
			String vseName, String vsName, String tokenId) {
		return new DevTestStartVs(useCustomRegistry, host, port, vseName, vsName, tokenId, false, false);
	}

}
