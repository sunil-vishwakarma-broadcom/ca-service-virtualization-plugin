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
import com.ca.devtest.jenkins.plugin.postbuild.parser.TestInvokeApiResultParser;
import com.ca.devtest.jenkins.plugin.postbuild.report.Report;
import com.ca.devtest.jenkins.plugin.util.MyFileCallable;
import com.ca.devtest.jenkins.plugin.util.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Build step for starting test/testsuite.
 *
 * @author jakro01
 */
public class DevTestDeployTest extends DefaultBuildStep {

	private static final String REPORT_SUITE_URL = "/lisa-test-invoke/api/v1/suites/reports/";
	private static final String REPORT_TEST_URL = "/lisa-test-invoke/api/v1/tests/reports/";

	private static final String RUN_TEST_URL = "/lisa-test-invoke/api/v1/tests/run";
	private static final String RUN_SUITE_URL = "/lisa-test-invoke/api/v1/suites/run";

	private final List<String> endStates =
			Arrays.asList("ENDED", "PASSED", "FAILED", "ABORTED", "FAILED_TO_STAGE");

	private final String testType;
	private String marFilePath;

	private final Boolean test;

	/**
	 * Constructor.
	 *
	 * @param useCustomRegistry if we are overriding default Registry
	 * @param host              host for custom Registry
	 * @param port              port for custom Registry
	 * @param marFilePath       path to mar file with test/testsuite
	 * @param testType          switch determining if executing test or testsuite (link between
	 *                          frontend and backend) (possible values from UI "tests" or "suites"
	 * @param tokenCredentialId credentials token id
	 */
	@DataBoundConstructor
	public DevTestDeployTest(boolean useCustomRegistry, String host, String port, String marFilePath,
			String testType, String tokenCredentialId) {
		super(useCustomRegistry, host, port, tokenCredentialId);
		this.marFilePath = marFilePath;
		this.testType = testType;
		this.test = testType.equalsIgnoreCase("tests");
	}

	/**
	 * Method for determining if we are executing test or testsuite.
	 *
	 * @return true if we are executing test and false when we are executing testsuite
	 */
	public boolean isTest() {
		return this.test;
	}

	public String getTestType() {
		return testType;
	}

	public String getMarFilePath() {
		return marFilePath;
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

		String baseUrl = "http://" + currentHost + ":" + currentPort + "/";
		FilePath workDir = new FilePath(run.getRootDir());
		String runItemId;
		String runItemStatus;
		//check/validate marFilePath parameter
		if (marFilePath == null || marFilePath.isEmpty()) {
			throw new AbortException(Messages.DevTestDeployTest_missing_mar());
		}
		List<String> resMarFilePath = Utils.resolveParameters(
				Collections.singletonList(marFilePath), run, listener);

		String resolvedMarFilePath = resMarFilePath.get(0);
		listener.getLogger().println(Messages.DevTestDeployTest_deploying(resolvedMarFilePath));
		listener.getLogger().println(Messages.DevTestPlugin_devTestLocation(currentHost, currentPort));

		//send test/suite to devtest
		CloseableHttpClient client = null;

		try {
			client = createHttpClient(currentHost, currentPort);

			HttpEntity entity = createPostEntity(workspace, listener, resolvedMarFilePath);
			String itemUrl = baseUrl + (isTest() ? RUN_TEST_URL : RUN_SUITE_URL);
			HttpPost httpPost = new HttpPost(itemUrl);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setEntity(entity);
			CloseableHttpResponse response = client.execute(httpPost);

			try {
				int statusCode = response.getStatusLine().getStatusCode();
				String responseBody = EntityUtils.toString(response.getEntity());
				if (statusCode == 200) {
					listener.getLogger().println(Messages.DevTestPlugin_responseBody(responseBody));
				} else {
					listener.getLogger().println(Messages.DevTestDeployTest_error());
					throw new AbortException(Messages.DevTestPlugin_responseStatus(statusCode, responseBody));
				}
				runItemId = parseResponseForId(responseBody);
			} finally {
				response.close();
			}

			listener.getLogger()
							.println(Messages.DevTestDeployTest_success(runItemId));

			//			client = createHttpClient(currentHost, currentPort);
			runItemStatus = waitForEnd(client, itemUrl + "/" + runItemId);
			listener.getLogger().println(Messages.DevTestDeployTest_status(runItemStatus));

			//download reports for test/suite
			if (isTest()) {
				downloadTestOutput(client, baseUrl,
						new FilePath(workDir, "report/" + runItemId + "/tests/case"), runItemId,
						listener.getLogger());
			} else {
				downloadSuiteOutput(client, baseUrl,
						new FilePath(workDir, "report/" + runItemId + "/suites/suite"), runItemId,
						listener.getLogger());
			}

			if (runItemStatus.equalsIgnoreCase("FAILED")) {
				run.setResult(Result.UNSTABLE);
			} else if (runItemStatus.equalsIgnoreCase("ABORTED")
					|| runItemStatus.equalsIgnoreCase("FAILED_TO_STAGE")) {
				run.setResult(Result.FAILURE);
			} else {
				run.setResult(Result.SUCCESS);
			}

			//Test/Suite run details: Z tests executed (Y tests passed, X tests failed)
			Report report = new TestInvokeApiResultParser(listener.getLogger())
					.parseStep(run.getRootDir().toString() + "/report/" + runItemId);
			listener.getLogger().println(Messages.DevTestDeployTest_statusDetails(report.getTotalCount(),
					report.getSuccessfullTests().size(),report.getFailCount()));

		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	private CloseableHttpClient createHttpClient(String host, String port) {

		String currentUsername = isUseCustomRegistry() ? super.getUsername()
				: DevTestPluginConfiguration.get().getUsername();
		String currentPassword = isUseCustomRegistry() ? super.getPassword().getPlainText()
				: DevTestPluginConfiguration.get().getPassword().getPlainText();

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
				new AuthScope(host, Integer.parseInt(port)),
				new UsernamePasswordCredentials(currentUsername, currentPassword));

		return HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
	}

	/**
	 * periodically is checking the test status until it's something else than running.
	 */
	private String waitForEnd(CloseableHttpClient client, String checkUrl)
			throws IOException, InterruptedException {
		HttpGet httpGet = new HttpGet(checkUrl);
		CloseableHttpResponse resp = null;
		String body = "";
		String state;
		do {
			try {
				Thread.sleep(1900); //could be configurable if needed in future
				resp = client.execute(httpGet);
				body = EntityUtils.toString(resp.getEntity());
				state = parseResponseForStatus(body);
			} finally {
				resp.close();
			}
		} while (!this.endStates.contains(state));
		return state;
	}

	private HttpEntity createPostEntity(FilePath workspace, TaskListener listener,
			String marFilePath) throws IOException, InterruptedException {
		FilePath marFile = workspace.child(marFilePath);
		File file = marFile.act(new MyFileCallable());
		if (file != null) {
			return MultipartEntityBuilder.create()
																	 .addPart("file", new FileBody(file))
																	 .build();
		} else {
			listener.getLogger()
							.println(Messages.DevTestPlugin_missingFile(marFilePath));
			throw new FileNotFoundException(Messages.DevTestPlugin_fileEx(marFilePath));
		}
	}

	// Localization -> Messages generated by maven plugin check target folder
	@Symbol("svDeployTest")
	@Extension
	public static final class DescriptorImpl extends DefaultDescriptor {

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> abstractClass) {
			return true;
		}

		/**
		 * Method used for setting name of the component in Jenkins GUI. In this case it is name of
		 * build step for starting Test/Testsuite.
		 *
		 * @return name of build step
		 */
		@Override
		public String getDisplayName() {
			return Messages.DevTestDeployTest_DescriptorImpl_DisplayName();
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

		/**
		 * Fils the test type selection.
		 *
		 * @return testType listbox model
		 */
		public ListBoxModel doFillTestTypeItems() {
			ListBoxModel items = new ListBoxModel();
			items.add(new ListBoxModel.Option(Messages.DevTestDeployTest_Test(), "tests"));
			items.add(new ListBoxModel.Option(Messages.DevTestDeployTest_Suite(), "suites"));
			return items;
		}
	}

	private String parseResponseForId(String resp) {
		JsonParser parser = new JsonParser();
		JsonElement jsonTree = parser.parse(resp);

		if (jsonTree.isJsonObject()) {
			JsonObject jsonObject = jsonTree.getAsJsonObject();

			JsonElement id = jsonObject.get("id");
			return id.getAsString();
		}
		return "";
	}

	private String parseResponseForStatus(String resp) {
		JsonParser parser = new JsonParser();
		JsonElement jsonTree = parser.parse(resp);
		if (jsonTree.isJsonObject()) {
			JsonObject jsonObject = jsonTree.getAsJsonObject();
			JsonElement testStatus = jsonObject.get("testStatus");
			return testStatus.getAsString();
		}
		return "";
	}

	private void downloadTestOutput(CloseableHttpClient client, String baseUrl, FilePath storeDir,
			String testId, PrintStream logger) throws IOException, InterruptedException {
		String reportUrl = baseUrl + REPORT_TEST_URL + "/" + testId;
		downloadFile(client, reportUrl, new FilePath(storeDir, "test.json"));
		String cycleUrl = reportUrl + "/cycles";
		downloadFile(client, cycleUrl, new FilePath(storeDir, "cycles.json"));
	}

	private void downloadSuiteOutput(CloseableHttpClient client, String baseUrl, FilePath storeDir,
			String suitId, PrintStream logger) throws IOException, InterruptedException {
		String suiteUrl = baseUrl + REPORT_SUITE_URL + "/" + suitId;
		FilePath suiteJson = new FilePath(storeDir, "suite.json");
		downloadFile(client, suiteUrl, suiteJson);
		//get tests info for suite
		FilePath testsSuiteJson = new FilePath(storeDir, "testsSuite.json");
		downloadFile(client, suiteUrl + "/tests", testsSuiteJson);
		TestInvokeApiResultParser parser = new TestInvokeApiResultParser(logger);
		//get ids for all tests in suite and get reports for them
		List<String> testIds = parser.getCasesIdsFromSuite(testsSuiteJson.readToString());
		int i = 1;
		for (String id : testIds) {
			downloadFile(client, suiteUrl + "/tests/" + id, new FilePath(storeDir,
					"tests/case" + i + "/test.json"));
			downloadFile(client, suiteUrl + "/tests/" + id + "/cycles", new FilePath(storeDir,
					"tests/case" + i + "/cycles.json"));
			i++;
		}
	}

	private void downloadFile(CloseableHttpClient client, String url, FilePath storePath)
			throws IOException, InterruptedException {
		CloseableHttpResponse resp = null;
		HttpGet httpGet = new HttpGet(url);
		String body = "";
		try {
			resp = client.execute(httpGet);
			body = EntityUtils.toString(resp.getEntity());
		} finally {
			if (resp != null) {
				resp.close();
			}
		}
		storePath.write(body, "UTF-8");
	}

}
