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

import java.net.InetAddress;
import java.util.Collections;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.SingleFileSCM;

/**
 * Unit tests for {@link DevTestDeployVs}.
 *
 * @author jakro01
 */
public class DevTestDeployVsTest extends AbstractDevTestBuildStepTest {

	@Rule
	@TransactionClassRepository(repoClasses = {ApiRepository.class})
	public VirtualServerRule vs = new VirtualServerRule(this);

	@Test
	public void testConfigRoundtrip() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin(
				"file:///C:/webservices-vs.mar\n webservices-vs2.mar\n"
						+ " http://test.com/webservices-vs3.mar"));
		project = jenkins.configRoundtrip(project);
		jenkins.assertEqualDataBoundBeans(createPlugin(
				"file:///C:/webservices-vs.mar\n webservices-vs2.mar\n"
						+ " http://test.com/webservices-vs3.mar"),
				project.getBuildersList().get(0));
	}

	@Test
	public void testConfigRoundtripWithComma() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(createPlugin(
				"file:///C:/webservices-vs.mar, webservices-vs2.mar,"
						+ " http://test.com/webservices-vs3.mar"));
		project = jenkins.configRoundtrip(project);
		jenkins.assertEqualDataBoundBeans(createPlugin(
				"file:///C:/webservices-vs.mar, webservices-vs2.mar,"
						+ " http://test.com/webservices-vs3.mar"),
				project.getBuildersList().get(0));
	}

	@Test
	public void testEnvironmentPropertiesInConfig() throws Exception {
		Jenkins.getInstance().getGlobalNodeProperties().replaceBy(
				Collections.singleton(new EnvironmentVariablesNodeProperty(
						new Entry("vse", "test"), new Entry("marfiles", "test2\ntest3"),
						new Entry("host", "1.1.1.1"), new Entry("port", "6666"))));
		FreeStyleProject project = jenkins.createFreeStyleProject();
		DevTestDeployVs devTestDeployVs = createPlugin(true, "${host}", "${port}",
				"${vse}", "${marfiles}", "id");
		project.getBuildersList().add(devTestDeployVs);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("Deploying MAR file: test2", build);
		jenkins.assertLogContains("To DevTest API located on 1.1.1.1:6666", build);
	}

	@Test
	public void testBuildWithFile() throws Exception {
		vs.useTransaction("deployVs");
		//
		FreeStyleProject project = jenkins.createFreeStyleProject("test2");
		DevTestDeployVs builder = createPlugin();
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
		jenkins.assertLogContains("\"modelName\": \"webservices-vs\"", build);
		jenkins.assertLogContains("\"name\": \"webservices-vs\"", build);
	}

	@Test
	public void testDeployMultipleFiles() throws Exception {
		vs.useTransaction("uploadVs");
		vs.useTransaction("deployVsFileFileNotFound");

		FreeStyleProject project = jenkins.createFreeStyleProject("test2");
		project.setScm(
				new SingleFileSCM("webservices-vs.mar", getClass().getResource("/webservices-vs.mar")));
		DevTestDeployVs builder = createPlugin("webservices-vs.mar\n file:///C:/doesntExist.mar");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("\"modelName\": \"webservices-vs\"", build);
		jenkins.assertLogContains("\"name\": \"webservices-vs\"", build);
		jenkins.assertLogContains("\"id\": 1003", build);
		jenkins.assertLogContains(
				"\"message\": \"Can't open file:///C:/doesntExist.mar please check again.\"", build);
	}

	@Test
	public void testDeployMultipleFilesWithComma() throws Exception {
		vs.useTransaction("uploadVs");
		vs.useTransaction("deployVsFileFileNotFound");

		FreeStyleProject project = jenkins.createFreeStyleProject("test2");
		project.setScm(
				new SingleFileSCM("webservices-vs.mar", getClass().getResource("/webservices-vs.mar")));
		DevTestDeployVs builder = createPlugin("webservices-vs.mar, file:///C:/doesntExist.mar");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("\"modelName\": \"webservices-vs\"", build);
		jenkins.assertLogContains("\"name\": \"webservices-vs\"", build);
		jenkins.assertLogContains("\"id\": 1003", build);
		jenkins.assertLogContains(
				"\"message\": \"Can't open file:///C:/doesntExist.mar please check again.\"", build);
	}

	@Test
	public void testBuildWithFileOnJenkins() throws Exception {
		vs.useTransaction("uploadVs");
		FreeStyleProject project = jenkins.createFreeStyleProject("test2");
		project.setScm(
				new SingleFileSCM("webservices-vs.mar", getClass().getResource("/webservices-vs.mar")));
		DevTestDeployVs builder = createPlugin("webservices-vs.mar");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
		jenkins.assertLogContains("\"modelName\": \"webservices-vs\"", build);
		jenkins.assertLogContains("\"name\": \"webservices-vs\"", build);
	}

	@Test
	public void testBuildWithFileOnJenkinsSearchWithWildcard() throws Exception {
		vs.useTransaction("uploadVs");
		FreeStyleProject project = jenkins.createFreeStyleProject("test2");
		project.setScm(
				new SingleFileSCM("webservices-vs.mar", getClass().getResource("/webservices-vs.mar")));
		DevTestDeployVs builder = createPlugin("/*.mar");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
		jenkins.assertLogContains("\"modelName\": \"webservices-vs\"", build);
		jenkins.assertLogContains("\"name\": \"webservices-vs\"", build);
	}

	@Test
	public void testBuildMissingFileHttp() throws Exception {
		vs.useTransaction("deployVsHttpFileNotFound");

		FreeStyleProject project = jenkins.createFreeStyleProject();
		DevTestDeployVs builder = createPlugin("http://test.com/webservices-vs.mar");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("\"id\": 1003", build);
		jenkins.assertLogContains(
				"\"message\": \"Our audit file is either missing or we are not a "
						+ "model archive or are corrupt.\"", build);
	}

	@Test
	public void testBuildMissingFileFile() throws Exception {
		vs.useTransaction("deployVsFileFileNotFound");

		FreeStyleProject project = jenkins.createFreeStyleProject();
		DevTestDeployVs builder = createPlugin("file:///C:/doesntExist.mar");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("\"id\": 1003", build);
		jenkins.assertLogContains(
				"\"message\": \"Can't open file:///C:/doesntExist.mar please check again.\"", build);

	}

	@Test
	public void testBuildMissingFileOnJenkins() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject();
		DevTestDeployVs builder = createPlugin("webservices-vs.mar");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins
				.assertLogContains("File webservices-vs.mar is no present in the workspace of job on machine "
						+ InetAddress.getLocalHost().getHostName(), build);
		jenkins.assertLogContains(
				"ERROR: Cannot located file with relative path webservices-vs.mar in workspace of job",
				build);
	}

	@Test
	public void testBuildWrongVse() throws Exception {
		vs.useTransaction("deployVsAndVseDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject("test1");
		DevTestDeployVs builder = createPlugin(true, getHost(), "1505", "vse-doesnt-exist",
				"file:///C:/webservices-vs.mar", "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("id\": 1709", build);
		jenkins.assertLogContains("\"message\": \"vse-doesnt-exist not exist\"", build);
	}

	@Test
	public void testVseNameEmpty() throws Exception {
		vs.useTransaction("deployVsAndVseDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject("test1");
		DevTestDeployVs builder = createPlugin(true, getHost(), "1505", "",
				"file:///C:/webservices-vs.mar", "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("ERROR: VSE name cannot be empty!", build);
	}

	@Test
	public void testVseNameNull() throws Exception {
		vs.useTransaction("deployVsAndVseDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject("test1");
		DevTestDeployVs builder = createPlugin(true, getHost(), "1505", null,
				"file:///C:/webservices-vs.mar", "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("ERROR: VSE name cannot be empty!", build);
	}

	@Test
	public void testMarFileEmpty() throws Exception {
		vs.useTransaction("deployVsAndVseDoesntExist");

		FreeStyleProject project = jenkins.createFreeStyleProject("test1");
		DevTestDeployVs builder = createPlugin(true, getHost(), "1505", "VSE",
				"", "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("ERROR: Paths to MAR files cannot be empty", build);
	}

	@Test
	public void testMarFileNull() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject("test1");
		DevTestDeployVs builder = createPlugin(true, getHost(), "1505", "VSE",
				null, "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("ERROR: Paths to MAR files cannot be empty", build);
	}

	@Test
	public void testScriptedPipeline() throws Exception {
		vs.useTransaction("deployVs");

		String agentLabel = "my-agent";
		jenkins.createOnlineSlave(Label.get(agentLabel));
		WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");

		String pipelineScript
				= "node {\n"
				+ "svDeployVirtualService useCustomRegistry: true, vseName: \"VSE\", marFilesPaths: \"file:///C:/webservices-vs.mar\", host:\""
				+ getHost() + "\" , port:\"1505\", tokenCredentialId:\"id\"\n"
				+ "}";
		job.setDefinition(new CpsFlowDefinition(pipelineScript, false));
		WorkflowRun completedBuild = jenkins.assertBuildStatus(Result.SUCCESS, job.scheduleBuild2(0));
		jenkins.assertLogContains("\"modelName\": \"webservices-vs\"", completedBuild);
		jenkins.assertLogContains("\"name\": \"webservices-vs\"", completedBuild);
	}

	private DevTestDeployVs createPlugin() {
		return createPlugin(true, getHost(), "1505", "VSE",
				"file:///C:/webservices-vs.mar", "id");
	}

	private DevTestDeployVs createPlugin(String marFilesPaths) {
		return createPlugin(true, getHost(), "1505", "VSE", marFilesPaths, "id");
	}

	private DevTestDeployVs createPlugin(boolean useCustomRegistry, String host, String port,
			String vseName, String marFilesPaths, String tokenId) {
		return new DevTestDeployVs(useCustomRegistry, host, port, vseName, marFilesPaths, tokenId, false);
	}
}
