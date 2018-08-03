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

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import java.io.IOException;
import jenkins.model.Jenkins;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

/**
 * File description
 *
 * @author jakro01
 */
public abstract class AbstractDevTestBuildStepTest {

	private static String host = "localhost";
	private static boolean areIntegrationTests;

	@Rule
	public JenkinsRule jenkins = new JenkinsRule();

	@Before
	public void createCredentials() throws IOException {
		CredentialsStore store = CredentialsProvider.lookupStores(Jenkins.getInstance()).iterator()
																								.next();
		store.addCredentials(Domain.global(),
				new UsernamePasswordCredentialsImpl(
						CredentialsScope.GLOBAL, "id", "description", "admin", "admin"));
	}

	@AfterClass
	public static void afterClass() {

	}

	public static String getHost() {
		return host;
	}

	public static boolean isAreIntegrationTests() {
		return areIntegrationTests;
	}

	/**
	 * Setups and saves jenkins configuration.
	 *
	 * @param host     hostname
	 * @param port     port
	 * @param username user name
	 * @param password password
	 */
	public void setupJenkinsConfiguration(String host, String port, String username,
			String password, String token) throws IOException {

		CredentialsStore store = CredentialsProvider.lookupStores(Jenkins.getInstance()).iterator()
																								.next();
		store.addCredentials(Domain.global(),
				new UsernamePasswordCredentialsImpl(
						CredentialsScope.GLOBAL, token, "description", username, password));

		JenkinsRule.WebClient webClient = jenkins.createWebClient();

		HtmlPage page = null;
		try {
			page = webClient.goTo("configure");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		HtmlForm form = page.getFormByName("config");

		HtmlInput hostInput = form.getInputByName("_.host");
		hostInput.setValueAttribute(host);

		HtmlInput portInput = form.getInputByName("_.port");
		portInput.setValueAttribute(port);

		HtmlSelect tokenCredentialDropdown = form.getSelectByName("_.tokenCredentialId");
		tokenCredentialDropdown.getOption(1).click();

		try {
			jenkins.submit(form);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
