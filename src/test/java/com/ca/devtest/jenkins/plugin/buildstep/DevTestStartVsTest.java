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
 * Unit tests for {@link DevTestStartVs}.
 *
 * @author jakro01
 */
public class DevTestStartVsTest extends AbstractDevTestBuildStepTest {

	@Rule
	@TransactionClassRepository(repoClasses = {ApiRepository.class})
	public VirtualServerRule vs = new VirtualServerRule(this);

	@Test
	public void testConfigRoundtrip() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin("VSE", "webservices-vs\n webservices-vs2"));
		project = jenkins.configRoundtrip(project);
		jenkins.assertEqualDataBoundBeans(createPlugin("VSE", "webservices-vs\n webservices-vs2"),
				project.getBuildersList().get(0));
	}

	@Test
	public void testConfigRoundtripWithComma() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin("VSE", "webservices-vs, webservices-vs2"));
		project = jenkins.configRoundtrip(project);
		jenkins.assertEqualDataBoundBeans(createPlugin("VSE", "webservices-vs, webservices-vs2"),
				project.getBuildersList().get(0));
	}

	@Test
	public void testEnvironmentPropertiesInConfig() throws Exception {
		Jenkins.getInstance().getGlobalNodeProperties().replaceBy(
				Collections.singleton(new EnvironmentVariablesNodeProperty(
						new Entry("vse1", "test"), new Entry("vse2", "test2\ntest3"),
						new Entry("host", "1.1.1.1"), new Entry("port", "6666"))));
		FreeStyleProject project = jenkins.createFreeStyleProject();
		DevTestStartVs devTestStartVs = createPlugin(true, "${host}", "${port}",
				"${vse1}", "${vse2}", "id");
		project.getBuildersList().add(devTestStartVs);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));

		jenkins.assertLogContains("Starting virtual service: test2 on VSE: test", build);
		jenkins.assertLogContains("To DevTest API located on 1.1.1.1:6666", build);
	}

	@Test
	public void testMultipleVsStart() throws Exception {
		vs.useTransaction("startVs");

		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin("VSE", "webservices-vs\n webservices-vs2"));

		FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
		jenkins.assertLogContains("\"modelName\": \"webservices-vs\"", build);
		jenkins.assertLogContains("\"name\": \"webservices-vs\"", build);

		jenkins.assertLogContains("\"modelName\": \"webservices-vs2\"", build);
		jenkins.assertLogContains("\"name\": \"webservices-vs2\"", build);
	}

	@Test
	public void testMultipleVsStartWithComma() throws Exception {
		vs.useTransaction("startVs");

		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin("VSE", "webservices-vs, webservices-vs2"));

		FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
		jenkins.assertLogContains("\"modelName\": \"webservices-vs\"", build);
		jenkins.assertLogContains("\"name\": \"webservices-vs\"", build);

		jenkins.assertLogContains("\"modelName\": \"webservices-vs2\"", build);
		jenkins.assertLogContains("\"name\": \"webservices-vs2\"", build);
	}

	@Test
	public void testStartVs() throws Exception {
		vs.useTransaction("startVs");

		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin());

		FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
		jenkins.assertLogContains("\"modelName\": \"webservices-vs\"", build);
		jenkins.assertLogContains("\"name\": \"webservices-vs\"", build);
	}

	@Test
	public void testStartNotExistingVs() throws Exception {
		vs.useTransaction("startVsAndVsDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin("VSE", "vs-doesnt-exist"));

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("\"id\": 1007", build);
		jenkins.assertLogContains("\"message\": \"No such virtual service: vs-doesnt-exist\"", build);
	}

	@Test
	public void testStartNotExistingVse() throws Exception {
		vs.useTransaction("startVsAndVseDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin("vse-doesnt-exist", "webservices-vs"));

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("\"id\": 1709,", build);
		jenkins.assertLogContains(
				"\"message\": \"VSE name is invalid, either you type a wrong name, or the VSE is not started. VSE name:vse-doesnt-exist\"",
				build);
	}

	@Test
	public void testBuildOverrideDefaultSettings() throws Exception {
		//todo
	}

	@Test
	public void testVseNameEmpty() throws Exception {
		vs.useTransaction("startVsAndVseDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject("test1");
		DevTestStartVs builder = createPlugin(true, getHost(), "1505", "",
				"file:///C:/webservices-vs.mar", "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("ERROR: VSE name cannot be empty!", build);
	}

	@Test
	public void testVseNameNull() throws Exception {
		vs.useTransaction("startVsAndVseDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject("test1");
		DevTestStartVs builder = createPlugin(true, getHost(), "1505", null,
				"file:///C:/webservices-vs.mar", "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("ERROR: VSE name cannot be empty!", build);
	}

	@Test
	public void testVsNameEmpty() throws Exception {
		vs.useTransaction("startVsAndVseDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject("test1");
		DevTestStartVs builder = createPlugin(true, getHost(), "1505", "VSE",
				"", "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("ERROR: VS names cannot be empty", build);
	}

	@Test
	public void testVsNameNull() throws Exception {
		vs.useTransaction("startVsAndVseDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject("test1");
		DevTestStartVs builder = createPlugin(true, getHost(), "1505", "VSE",
				null, "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("ERROR: VS names cannot be empty", build);
	}

	@Test
	public void testScriptedPipeline() throws Exception {
		vs.useTransaction("startVs");

		String agentLabel = "my-agent";
		jenkins.createOnlineSlave(Label.get(agentLabel));
		WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");

		String pipelineScript
				= "node {\n"
				+ "svStartVirtualService useCustomRegistry: true, vseName: \"VSE\", vsNames: \"webservices-vs\", host:\""
				+ getHost() + "\" , port:\"1505\", tokenCredentialId:\"id\"\n"
				+ "}";
		job.setDefinition(new CpsFlowDefinition(pipelineScript, false));
		WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
		jenkins.assertLogContains("\"modelName\": \"webservices-vs\"", completedBuild);
		jenkins.assertLogContains("\"name\": \"webservices-vs\"", completedBuild);
	}

	private DevTestStartVs createPlugin() {
		return createPlugin(true, getHost(), "1505", "VSE", "webservices-vs", "id");
	}

	private DevTestStartVs createPlugin(String vseName, String vsName) {
		return createPlugin(true, getHost(), "1505", vseName, vsName, "id");
	}

	private DevTestStartVs createPlugin(boolean useCustomRegistry, String host, String port,
			String vseName, String vsName, String tokenId) {
		return new DevTestStartVs(useCustomRegistry, host, port,
				vseName, vsName, tokenId, false, false);
	}
}
