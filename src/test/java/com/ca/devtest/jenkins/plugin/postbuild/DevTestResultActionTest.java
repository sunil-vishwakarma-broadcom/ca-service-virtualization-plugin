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

package com.ca.devtest.jenkins.plugin.postbuild;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ca.devtest.jenkins.plugin.Messages;
import com.ca.devtest.jenkins.plugin.postbuild.parser.Parser;
import com.ca.devtest.jenkins.plugin.postbuild.parser.TestInvokeApiResultParser;
import com.ca.devtest.jenkins.plugin.postbuild.report.Report;
import com.ca.devtest.jenkins.plugin.postbuild.report.TestCase;
import hudson.model.Build;
import hudson.model.FreeStyleBuild;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class DevTestResultActionTest {

	private static Report report;

	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

	@BeforeClass
	public static void beforeClass() {
		Parser parser = new TestInvokeApiResultParser(new PrintStream(new ByteArrayOutputStream()));
		report = parser.parse("src/test/resources/report");
	}

	@Test
	public void actionResultsTest() throws IOException {
		Build build = new FreeStyleBuild(jenkinsRule.createFreeStyleProject("test"));
		DevTestResultAction action = new DevTestResultAction(build, report);
		assertEquals(action.getFailCount(), 1);
		assertEquals(action.getTotalCount(), 11);

		Report fromAction = action.getReport();
		TestCase testCase = fromAction.getFailedTests().get(0);
		assertEquals(testCase.getTestCaseId(), "EAC6BDD9E5AF11E7BADA020027E349EE");
		assertEquals(action.getDisplayName(), Messages.DevTestReport_Title());

		assertTrue(fromAction.getFailedTests().stream().map(failedTest -> failedTest.getRun())
												 .filter(run -> run.equals(build)).findAny().isPresent());

		assertTrue(!fromAction.getPassedTests().stream().map(failedTest -> failedTest.getRun())
												 .filter(run -> run.equals(build)).findAny().isPresent());
	}

}
