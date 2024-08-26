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
import com.ca.devtest.jenkins.plugin.config.RestClient;
import com.ca.devtest.jenkins.plugin.constants.APIEndpoints;
import com.ca.devtest.jenkins.plugin.exception.InvalidInputException;
import com.ca.devtest.jenkins.plugin.postbuild.parser.TestInvokeApiResultParser;
import com.ca.devtest.jenkins.plugin.postbuild.report.Report;
import com.ca.devtest.jenkins.plugin.util.URLFactory;
import com.ca.devtest.jenkins.plugin.util.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Build step for starting test/testsuite.
 *
 * @author jakro01
 */
public class DevTestDeployTest extends DefaultBuildStep {

	private final List<String> endStates =
			Arrays.asList("ENDED", "PASSED", "FAILED", "ABORTED", "FAILED_TO_STAGE");

	private final String testType;
	private String marFilePath;
	private String stagingDocRelativePath;
	private String stagingDocFilePath;
	private String configRelativePath;
	private String configFilePath;
	private String coordinatorServerName;

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
	 * @param secured           if https should be for custom registry
	 */
	@DataBoundConstructor
	public DevTestDeployTest(boolean useCustomRegistry, String host, String port, String marFilePath,
			String stagingDocRelativePath, String stagingDocFilePath, String configRelativePath, String configFilePath, String coordinatorServerName,
			String testType, String tokenCredentialId, boolean secured, boolean trustAnySSLCertificate) {

		super(useCustomRegistry, host, port, tokenCredentialId, secured, trustAnySSLCertificate);
		this.marFilePath = marFilePath;
		this.stagingDocRelativePath = stagingDocRelativePath;
		this.stagingDocFilePath = stagingDocFilePath;
		this.configRelativePath = configRelativePath;
		this.configFilePath = configFilePath;
		this.coordinatorServerName= coordinatorServerName;
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

	public String getStagingDocRelativePath() {
		return stagingDocRelativePath;
	}

	public String getStagingDocFilePath() {
		return stagingDocFilePath;
	}

	public String getConfigRelativePath() {
		return configRelativePath;
	}

	public String getConfigFilePath() {
		return configFilePath;
	}

	public String getCoordinatorServerName() {
		return coordinatorServerName;
	}

	@Override
	public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace,
						  @Nonnull Launcher launcher, @Nonnull TaskListener listener)
			throws InterruptedException, IOException {

		Utils.checkRegistryEndpoint(this);
		this.updateCredentails(run);

		String currentHost = getCurrentHost();
		String currentPort = getCurrentPort();
		String currentProtocol = getCurrentProtocol();
		boolean trustAnySSLCertificate = getTrustAnySSLCertificate();

		FilePath workDir = new FilePath(run.getRootDir());
		validateRequiredMarFilePath();
		String resolvedMarFilePath = resolveParameter(marFilePath, run, listener);
		String resolvedStagingDocRelativePath = Utils.resolveParameter(getStagingDocRelativePath(), run, listener);
		String resolvedStagingDocFilePath = resolveParameter(getStagingDocFilePath(), run, listener);
		String resolvedConfigRelativePath = Utils.resolveParameter(getConfigRelativePath(), run, listener);
		String resolvedConfigFilePath = resolveParameter(getConfigFilePath(), run, listener);
		String resolvedCoordinatorServerName = Utils.resolveParameter(getCoordinatorServerName(), run, listener);

		listener.getLogger().println(Messages.DevTestDeployTest_deploying(resolvedMarFilePath));
		listener.getLogger().println(Messages.DevTestPlugin_devTestLocation(currentHost, currentPort));

		URLFactory urlFactory = new URLFactory(currentProtocol , currentHost, currentPort);
		HttpEntity entity = createPostEntity(workspace, listener, resolvedMarFilePath, resolvedStagingDocRelativePath, resolvedStagingDocFilePath, resolvedConfigRelativePath, resolvedConfigFilePath, resolvedCoordinatorServerName);
		String itemUrl = isTest() ? urlFactory.buildUrl(APIEndpoints.RUN_TEST_URL) : urlFactory.buildUrl(APIEndpoints.RUN_SUITE_URL);

		String currentUsername = getCurrentUsername();
		String currentPassword = getCurrentPassword();

		String runItemId = sendPostRequestAndGetItemId(itemUrl, currentUsername, currentPassword, trustAnySSLCertificate, entity, listener);
		logSuccessMessage(runItemId, listener);
		String runItemResponseBody = waitForEnd(itemUrl + "/" + runItemId, currentUsername, currentPassword, trustAnySSLCertificate);
		String runItemStatus = parseResponseForStatus(runItemResponseBody);
		logRunItemStatus(runItemStatus, listener);
		run.addAction(new ParametersAction(new StringParameterValue("deployTestResponse", runItemResponseBody)));
		downloadReportsAndSetRunResult(runItemStatus, runItemId, urlFactory, currentUsername, currentPassword, trustAnySSLCertificate, workDir, run, listener);

	}

	/**
	 * periodically is checking the test status until it's something else than running.
	 */
	private String waitForEnd(String checkUrl, String currentUsername, String currentPassword, boolean trustAnySSLCertificate)
			throws IOException, InterruptedException {
		String body ;
		String state;
		do {
			Thread.sleep(1900);
			try(CloseableHttpResponse response = RestClient.executeGet(checkUrl, currentUsername, currentPassword, trustAnySSLCertificate, null)){
				body = EntityUtils.toString(response.getEntity());
				state = parseResponseForStatus(body);
			}
		} while (!this.endStates.contains(state));
		return body;
	}

	private HttpEntity createPostEntity(FilePath workspace, TaskListener listener, String marFilePath, String resolvedStagingDocRelativePath, String resolvedStagingDocFilePath, String resolvedConfigRelativePath, String resolvedConfigFilePath, String resolvedCoordinatorServerName) throws IOException, InterruptedException {

		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		multipartEntityBuilder.setContentType(ContentType.MULTIPART_FORM_DATA);

		addBodyPartWithLogging(workspace, multipartEntityBuilder, "file", marFilePath, "MAR File", listener);

		addTextBodyIfNotBlank(multipartEntityBuilder, "stagingDoc", resolvedStagingDocRelativePath, "Staging Doc", listener);
		addBodyPartWithLogging(workspace, multipartEntityBuilder, "stagingDocFile", resolvedStagingDocFilePath, "Staging Doc File", listener);

		addTextBodyIfNotBlank(multipartEntityBuilder, "config", resolvedConfigRelativePath, "Config", listener);
		addBodyPartWithLogging(workspace, multipartEntityBuilder, "configFile", resolvedConfigFilePath, "Config File", listener);

		addTextBodyIfNotBlank(multipartEntityBuilder, "coordinatorServerName", resolvedCoordinatorServerName, "Coordinator server name", listener);

		return multipartEntityBuilder.build();
	}

	private void addTextBodyIfNotBlank(MultipartEntityBuilder multipartEntityBuilder, String name, String value, String logMessage, TaskListener listener) {
		if (StringUtils.isNotBlank(value)) {
			multipartEntityBuilder.addTextBody(name, StringUtils.trim(value));
			listener.getLogger().println(logMessage + ": " + StringUtils.trim(value));
		}
	}

	private void addBodyPartWithLogging(FilePath workspace, MultipartEntityBuilder multipartEntityBuilder, String name, String filePath, String logMessage, TaskListener listener) throws IOException, InterruptedException {
		if (StringUtils.isNotBlank(filePath)) {
			try {
				Utils.addBodyPart(workspace, multipartEntityBuilder, name, StringUtils.trim(filePath));
				listener.getLogger().println(logMessage + ": " + StringUtils.trim(filePath));
			} catch (InvalidInputException ex) {
				handleFileNotFound(listener, ex.getMessage());
			}
		}
	}


	private void handleFileNotFound(TaskListener listener, String filePath) throws FileNotFoundException {
		String errorMessage = Messages.DevTestPlugin_missingFile(filePath);
		listener.getLogger().println(errorMessage);
		throw new FileNotFoundException(Messages.DevTestPlugin_fileEx(filePath));
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
		public FormValidation doCheckHost(@AncestorInPath Item context, @QueryParameter boolean useCustomRegistry,
				@QueryParameter String host, @QueryParameter String port,
				@QueryParameter String tokenCredentialId) {
			return Utils.doCheckHost(context, useCustomRegistry, host, port, tokenCredentialId);
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
		return parseResponseForKey(resp, "id");
	}

	private String parseResponseForStatus(String resp) {
		return parseResponseForKey(resp, "testStatus");
	}

	private String parseResponseForKey(String resp, String key) {
		JsonParser parser = new JsonParser();
		JsonElement jsonTree = parser.parse(resp);

		if (jsonTree.isJsonObject()) {
			JsonObject jsonObject = jsonTree.getAsJsonObject();

			JsonElement element = jsonObject.get(key);
			if (element != null) {
				return element.getAsString();
			}
		}
		return "";
	}

	private void downloadTestOutput(URLFactory urlFactory, String currentUsername, String currentPassword, boolean trustAnySSLCertificate, FilePath storeDir,
			String testId) throws IOException, InterruptedException {
		String reportUrl = urlFactory.buildUrl(String.format(APIEndpoints.REPORT_URL, testId));
		downloadFile(reportUrl, currentUsername, currentPassword, trustAnySSLCertificate, new FilePath(storeDir, "test.json"));

		String cycleUrl = urlFactory.buildUrl(String.format(APIEndpoints.CYCLE_URL, testId));
		downloadFile(cycleUrl, currentUsername, currentPassword, trustAnySSLCertificate, new FilePath(storeDir, "cycles.json"));
	}

	private void downloadSuiteOutput(URLFactory urlFactory, String currentUsername, String currentPassword, boolean trustAnySSLCertificate, FilePath storeDir,
			String suitId, PrintStream logger) throws IOException, InterruptedException {
		String suiteUrl = urlFactory.buildUrl(String.format(APIEndpoints.SUITE_REPORT_URL, suitId));
		FilePath suiteJson = new FilePath(storeDir, "suite.json");
		downloadFile(suiteUrl, currentUsername, currentPassword, trustAnySSLCertificate, suiteJson);

		//get tests info for suite
		String testSuitUrl = urlFactory.buildUrl(String.format(APIEndpoints.SUITE_TESTS_REPORT_URL, suitId));
		FilePath testsSuiteJson = new FilePath(storeDir, "testsSuite.json");
		downloadFile(testSuitUrl, currentUsername, currentPassword, trustAnySSLCertificate, testsSuiteJson);

		//get ids for all tests in suite and get reports for them
		TestInvokeApiResultParser parser = new TestInvokeApiResultParser(logger);
		List<String> testIds = parser.getCasesIdsFromSuite(testsSuiteJson.readToString());
		downloadTestFiles(suiteUrl, testIds, currentUsername, currentPassword, trustAnySSLCertificate, storeDir);
	}
	private void downloadTestFiles(String suiteUrl, List<String> testIds, String currentUsername, String currentPassword, boolean trustAnySSLCertificate, FilePath storeDir)
			throws IOException, InterruptedException {
		int i = 1;
		for (String id : testIds) {
			downloadFile(suiteUrl + "/tests/" + id, currentUsername, currentPassword, trustAnySSLCertificate,
					new FilePath(storeDir, "tests/case" + i + "/test.json"));
			downloadFile(suiteUrl + "/tests/" + id + "/cycles", currentUsername, currentPassword, trustAnySSLCertificate,
					new FilePath(storeDir, "tests/case" + i + "/cycles.json"));
			i++;
		}
	}

	private void downloadFile(String url, String currentUsername, String currentPassword, boolean trustAnySSLCertificate, FilePath storePath)
			throws IOException, InterruptedException {
		try(CloseableHttpResponse response = RestClient.executeGet(url, currentUsername, currentPassword, trustAnySSLCertificate, null)){
			String body = EntityUtils.toString(response.getEntity());
			storePath.write(body, "UTF-8");
		}

	}

	private String getCurrentHost() {
		return isUseCustomRegistry() ? super.getHost() : DevTestPluginConfiguration.get().getHost();
	}

	private String getCurrentPort() {
		return isUseCustomRegistry() ? super.getPort() : DevTestPluginConfiguration.get().getPort();
	}

	private String getCurrentProtocol() {
		return isUseCustomRegistry() ? (isSecured() ? "https" : "http") : (DevTestPluginConfiguration.get().isSecured() ? "https" : "http");
	}

	private boolean getTrustAnySSLCertificate() {
		return isUseCustomRegistry() ? super.isTrustAnySSLCertificate() : DevTestPluginConfiguration.get().isTrustAnySSLCertificate();
	}

	private void validateRequiredMarFilePath() throws AbortException {
		if (marFilePath == null || marFilePath.isEmpty()) {
			throw new AbortException(Messages.DevTestDeployTest_missing_mar());
		}
	}

	private String getCurrentUsername() {
		return isUseCustomRegistry() ? super.getUsername() : DevTestPluginConfiguration.get().getUsername();
	}

	private String getCurrentPassword() {
		return isUseCustomRegistry() ? super.getPassword().getPlainText() : DevTestPluginConfiguration.get().getPassword().getPlainText();
	}

	private String resolveParameter(String param, Run<?, ?> run, TaskListener listener) throws IOException, InterruptedException {
		if (StringUtils.isNotEmpty(param)) {
			List<String> resParamFilePath = Utils.resolveParameters(Collections.singletonList(param), run, listener);
			return resParamFilePath.get(0);
		}
		return null;
	}

	private String sendPostRequestAndGetItemId(String itemUrl, String currentUsername, String currentPassword, boolean trustAnySSLCertificate, HttpEntity entity, TaskListener listener) throws IOException{
		try (CloseableHttpResponse response = RestClient.executePost(itemUrl, currentUsername, currentPassword, trustAnySSLCertificate, entity, null)) {
			int statusCode = response.getStatusLine().getStatusCode();
			String responseBody = EntityUtils.toString(response.getEntity());
			if (statusCode == 200) {
				listener.getLogger().println(Messages.DevTestPlugin_responseBody(responseBody));
			} else {
				listener.getLogger().println(Messages.DevTestDeployTest_error());
				throw new AbortException(Messages.DevTestPlugin_responseStatus(statusCode, responseBody));
			}
			return parseResponseForId(responseBody);
		}
	}

	private void logSuccessMessage(String runItemId, TaskListener listener) {
		listener.getLogger().println(Messages.DevTestDeployTest_success(runItemId));
	}
	private void logRunItemStatus(String runItemStatus, TaskListener listener) {
		listener.getLogger().println(Messages.DevTestDeployTest_status(runItemStatus));
	}

	private void downloadReportsAndSetRunResult(String runItemStatus, String runItemId, URLFactory urlFactory, String currentUsername, String currentPassword, boolean trustAnySSLCertificate, FilePath workDir, Run<?, ?> run, TaskListener listener) throws IOException, InterruptedException {
		if (isTest()) {
			downloadTestOutput(urlFactory, currentUsername, currentPassword, trustAnySSLCertificate,
					new FilePath(workDir, "report/" + runItemId + "/tests/case"), runItemId);
		} else {
			downloadSuiteOutput(urlFactory, currentUsername, currentPassword, trustAnySSLCertificate,
					new FilePath(workDir, "report/" + runItemId + "/suites/suite"), runItemId, listener.getLogger());
		}

		setResultBasedOnStatus(runItemStatus, run, listener);

		printStatusDetails(runItemId, run, listener);
	}

	private void setResultBasedOnStatus(String runItemStatus, Run<?, ?> run, TaskListener listener) {
		if (runItemStatus.equalsIgnoreCase("FAILED")) {
			listener.getLogger().println("Test run result: UNSTABLE");
			run.setResult(Result.UNSTABLE);
		} else if (runItemStatus.equalsIgnoreCase("ABORTED") || runItemStatus.equalsIgnoreCase("FAILED_TO_STAGE")) {
			listener.getLogger().println("Test run result: FAILURE");
			run.setResult(Result.FAILURE);
		} else {
			listener.getLogger().println("Test run result: SUCCESS");
			run.setResult(Result.SUCCESS);
		}
	}

	private void printStatusDetails(String runItemId, Run<?, ?> run, TaskListener listener) {
		Report report = new TestInvokeApiResultParser(listener.getLogger())
				.parseStep(run.getRootDir().toString() + "/report/" + runItemId);
		listener.getLogger().println(Messages.DevTestDeployTest_statusDetails(report.getTotalCount(),
				report.getSuccessfullTests().size(), report.getFailCount()));
	}

}
