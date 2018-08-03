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

import com.ca.codesv.engine.junit4.VirtualServerRule;
import com.ca.codesv.sdk.annotation.TransactionClassRepository;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.model.Result;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;
import java.util.Collections;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;

/**
 * Unit tests for {@link DevTestUndeployVs}.
 *
 * @author jakro01
 */
public class DevTestUndeployVsTest extends AbstractDevTestBuildStepTest {

	@Rule
	@TransactionClassRepository(repoClasses = {ApiRepository.class})
	public VirtualServerRule vs = new VirtualServerRule(this);

	@Test
	public void testConfigRoundtrip() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin("VSE", "webservices-vs\n webservices-vs2"));
		project = jenkins.configRoundtrip(project);
		jenkins
				.assertEqualDataBoundBeans(createPlugin("VSE", "webservices-vs\n webservices-vs2"),
						project.getBuildersList().get(0));
	}

	@Test
	public void testConfigRoundtripWithComma() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin("VSE", "webservices-vs, webservices-vs2"));
		project = jenkins.configRoundtrip(project);
		jenkins
				.assertEqualDataBoundBeans(createPlugin("VSE", "webservices-vs, webservices-vs2"),
						project.getBuildersList().get(0));
	}

	@Test
	public void testEnvironmentPropertiesInConfig() throws Exception {
		Jenkins.getInstance().getGlobalNodeProperties().replaceBy(
				Collections.singleton(new EnvironmentVariablesNodeProperty(
						new Entry("vse1", "test"), new Entry("vse2", "test2\ntest3"),
						new Entry("host", "1.1.1.1"), new Entry("port", "6666"))));
		FreeStyleProject project = jenkins.createFreeStyleProject();
		DevTestUndeployVs devTestUndeployVs = createPlugin(true, "${host}", "${port}",
				"${vse1}", "${vse2}", "id");
		project.getBuildersList().add(devTestUndeployVs);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));

		jenkins.assertLogContains("Undeploying virtual service: test2 on VSE: test", build);
		jenkins.assertLogContains("To DevTest API located on 1.1.1.1:6666", build);
	}

	@Test
	public void testMultipleVsUndeploy() throws Exception {
		vs.useTransaction("undeployVs");

		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin("VSE", "webservices-vs\n webservices-vs2"));

		FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
		jenkins
				.assertLogContains(
						"Virtual service: webservices-vs on VSE: VSE was successfully undeployed",
						build);
		jenkins
				.assertLogContains(
						"Virtual service: webservices-vs2 on VSE: VSE was successfully undeployed",
						build);
	}

	@Test
	public void testMultipleVsUndeployWithComma() throws Exception {
		vs.useTransaction("undeployVs");

		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin("VSE", "webservices-vs, webservices-vs2"));

		FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
		jenkins
				.assertLogContains(
						"Virtual service: webservices-vs on VSE: VSE was successfully undeployed",
						build);
		jenkins
				.assertLogContains(
						"Virtual service: webservices-vs2 on VSE: VSE was successfully undeployed",
						build);
	}

	@Test
	public void testUndeployVs() throws Exception {
		vs.useTransaction("undeployVs");

		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin());

		FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
		jenkins
				.assertLogContains(
						"Virtual service: webservices-vs on VSE: VSE was successfully undeployed",
						build);
	}

	@Test
	public void testUndeployNotExistingVs() throws Exception {
		vs.useTransaction("undeployVsAndVsDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin("VSE", "vs-doesnt-exist"));

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("<Id>1702</Id>", build);
		jenkins.assertLogContains(
				"<Message>Virtual service name is invalid. Virtual service name:vs-doesnt-exist</Message>",
				build);
	}

	@Test
	public void testUndeployNotExistingVse() throws Exception {
		vs.useTransaction("undeployVsAndVseDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin("vse-doesnt-exist", "webservices-vs"));

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("<Id>1709</Id>", build);
		jenkins.assertLogContains("<Message>Vse name is invalid. vse name:vse-doesnt-exist</Message>",
				build);
	}

	@Test
	public void testVseNameEmpty() throws Exception {
		vs.useTransaction("undeployVsAndVseDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject("test1");
		DevTestUndeployVs builder = createPlugin(true, getHost(), "1505", "",
				"file:///C:/webservices-vs.mar", "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("ERROR: VSE name cannot be empty!", build);
	}

	@Test
	public void testVseNameNull() throws Exception {
		vs.useTransaction("undeployVsAndVseDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject("test1");
		DevTestUndeployVs builder = createPlugin(true, getHost(), "1505", null,
				"file:///C:/webservices-vs.mar", "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("ERROR: VSE name cannot be empty!", build);
	}

	@Test
	public void testVsNameEmpty() throws Exception {
		vs.useTransaction("undeployVsAndVseDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject("test1");
		DevTestUndeployVs builder = createPlugin(true, getHost(), "1505", "VSE",
				"", "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("ERROR: VS names cannot be empty", build);
	}

	@Test
	public void testVsNameNull() throws Exception {
		vs.useTransaction("undeployVsAndVseDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject("test1");
		DevTestUndeployVs builder = createPlugin(true, getHost(), "1505", "VSE",
				null, "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("ERROR: VS names cannot be empty", build);
	}

	@Test
	public void testScriptedPipeline() throws Exception {
		vs.useTransaction("undeployVs");

		String agentLabel = "my-agent";
		jenkins.createOnlineSlave(Label.get(agentLabel));
		WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");

		String pipelineScript
				= "node {\n"
				+ "svUndeployVirtualService useCustomRegistry: true, vseName: \"VSE\", vsNames: \"webservices-vs\", host:\""
				+ getHost() + "\" , port:\"1505\", tokenCredentialId:\"id\"\n"
				+ "}";
		job.setDefinition(new CpsFlowDefinition(pipelineScript, false));
		WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
		jenkins
				.assertLogContains(
						"Virtual service: webservices-vs on VSE: VSE was successfully undeployed",
						completedBuild);
	}

	private DevTestUndeployVs createPlugin() {
		return createPlugin(true, getHost(), "1505", "VSE", "webservices-vs", "id");
	}

	private DevTestUndeployVs createPlugin(String vseName, String vsName) {
		return createPlugin(true, getHost(), "1505", vseName, vsName, "id");
	}

	private DevTestUndeployVs createPlugin(boolean useCustomRegistry, String host, String port,
			String vseName, String vsName, String tokenId) {
		return new DevTestUndeployVs(useCustomRegistry, host, port, vseName, vsName,
				tokenId);
	}
}
