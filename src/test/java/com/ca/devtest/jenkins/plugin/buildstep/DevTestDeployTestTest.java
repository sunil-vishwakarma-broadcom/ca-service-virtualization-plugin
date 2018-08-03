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
import hudson.slaves.DumbSlave;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;
import java.util.Collections;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.SingleFileSCM;

/**
 * Unit tests for {@link DevTestDeployTest}.
 *
 * @author jakro01
 */
public class DevTestDeployTestTest extends AbstractDevTestBuildStepTest {

	@Rule
	@TransactionClassRepository(repoClasses = {ApiRepository.class})
	public VirtualServerRule vs = new VirtualServerRule(this);

	@Test
	public void testMarFileNameEmpty() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject("test1");
		DevTestDeployTest builder = createPlugin(true, getHost(), "1505",
				"", "tests", "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains("Path to MAR file cannot be empty", build);
	}

	@Test
	public void testEnvironmentPropertiesInConfig() throws Exception {
		Jenkins.getInstance().getGlobalNodeProperties().replaceBy(
				Collections.singleton(new EnvironmentVariablesNodeProperty(
						new Entry("vse", "test"), new Entry("marfiles", "test2\ntest3"))));
		FreeStyleProject project = jenkins.createFreeStyleProject();
		DevTestDeployTest devTestDeployVs = createPlugin(true, getHost(), "1505",
				"${marfiles}", "", "id");
		project.getBuildersList().add(devTestDeployVs);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		//currently support only 1 test per 1 buildstep
		jenkins.assertLogContains("Deploying Test MAR file: test2", build);
		jenkins.assertLogNotContains("test3", build);
	}

	@Test
	public void testStartTestWrongMarFile() throws Exception {
		vs.useTransaction("startTestWrongMarFile");
		FreeStyleProject project = jenkins.createFreeStyleProject("test1");
		project.setScm(new SingleFileSCM("wrong-test.mar",
				getClass().getResource("/wrong-test.mar")));
		DevTestDeployTest builder = createPlugin(true, getHost(), "1505",
				"wrong-test.mar", "tests", "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
		jenkins.assertLogContains(
				"Our audit file is either missing or we are not a model archive or are corrupt", build);
	}

	@Test
	public void testStartTestMarFile() throws Exception {
		vs.useTransaction("startTestMarFileTest");
		vs.useTransaction("virtualizeStartTestGetTestStatus");

		FreeStyleProject project = jenkins.createFreeStyleProject("test2");
		project.setScm(new SingleFileSCM("rest-example.mar",
				getClass().getResource("/rest-example.mar")));
		DevTestDeployTest builder = createPlugin(true, getHost(), "1505",
				"rest-example.mar", "tests", "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.SUCCESS, project.scheduleBuild2(0));
		jenkins.assertLogContains("Test/Suite from MAR file was successfully deployed, id = ", build);
		jenkins.assertLogContains("Test/Suite run finished with status: PASSED", build);
	}

	@Test
	public void testStartSuiteMarFile() throws Exception {
		vs.useTransaction("startTestMarFileSuite");
		vs.useTransaction("virtualizeStartTestGetSuiteStatus");

		FreeStyleProject project = jenkins.createFreeStyleProject("test3");
		project.setScm(new SingleFileSCM("testsuite.mar",
				getClass().getResource("/testsuite.mar")));
		DevTestDeployTest builder = createPlugin(true, getHost(), "1505",
				"testsuite.mar", "suites", "id");
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.assertBuildStatus(Result.SUCCESS, project.scheduleBuild2(0));
		jenkins.assertLogContains("Test/Suite from MAR file was successfully deployed, id = ", build);

	}

	@Test
	public void testScriptedPipeline() throws Exception {
		vs.useTransaction("startTestMarFileTest");
		vs.useTransaction("virtualizeStartTestGetTestStatus");
		String agentLabel = "my-agent";
		DumbSlave dumbSlave = jenkins.createOnlineSlave(Label.get(agentLabel));
		WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");

		String pipelineScript
				= "node {\n"
				+ "writeFile file:  \"rest-example.mar\", text: \"123\" \n"
				+ "svDeployTest useCustomRegistry: true, host:\""
				+ getHost()
				+ "\" , port:\"1505\", marFilePath: \"rest-example.mar\", testType: \"tests\", tokenCredentialId:\"id\"\n"
				+ "}";
		job.setDefinition(new CpsFlowDefinition(pipelineScript, false));
		WorkflowRun completedBuild = jenkins.assertBuildStatus(Result.SUCCESS, job.scheduleBuild2(0));
		jenkins.assertLogContains("Test/Suite from MAR file was successfully deployed, id = ",
				completedBuild);
		jenkins.assertLogContains("Test/Suite run finished with status: PASSED", completedBuild);
	}

	private DevTestDeployTest createPlugin(boolean useCustomRegistry, String host, String port,
			String marFilesPath, String testType, String tokenId) {
		return new DevTestDeployTest(useCustomRegistry, host, port, marFilesPath, testType, tokenId);
	}
}
