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

package com.ca.devtest.jenkins.plugin.postbuild.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.ca.devtest.jenkins.plugin.postbuild.report.Report;
import com.ca.devtest.jenkins.plugin.postbuild.report.TestCase;
import com.ca.devtest.jenkins.plugin.postbuild.report.TestCycle;
import com.ca.devtest.jenkins.plugin.postbuild.report.TestState;
import com.ca.devtest.jenkins.plugin.postbuild.report.TestSuite;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Invoke API parser test.
 *
 * @author mykdm01
 */
public class TestInvokeApiResultParserTest {

	private JsonParser jparser = new JsonParser();
	private TestInvokeApiResultParser parser = new TestInvokeApiResultParser(
			new PrintStream(new ByteArrayOutputStream()));

	/**
	 * Cycles parser tests
	 */
	@Test
	public void testParseCycles()
			throws ParseException {
		String jsonString = readFile(
				"src/test/resources/report/buildstep/suites/suite/tests/case/cycles.json");
		JsonElement jsonTree = jparser.parse(jsonString);

		List<TestCycle> cycleList = parser.parseCycles(jsonTree);
		assertEquals(10, cycleList.size());

		TestCycle cycle = cycleList.get(0);
		assertEquals(cycle.getCycle(), 0);
		assertEquals(cycle.getTestCycleId(), "442F2822E5B211E7B46E020027E349EE");
		assertEquals(cycle.getElapsedTimeInMillSec(), 4443);
		assertEquals(cycle.getMessages(), Collections.emptyList());
		assertEquals(cycle.getStart(),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS").parse("2017-12-20T13:19:06-0500"));
		assertEquals(cycle.getStop(),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS").parse("2017-12-20T13:19:11-0500"));
		assertEquals(cycle.getState(), TestState.FAILED);
	}

	@Test
	public void testParseCyclesEmptyReport() {
		JsonElement jsonTree = jparser.parse("{}");
		List<TestCycle> cycleList = parser.parseCycles(jsonTree);
		assertEquals(cycleList, Collections.emptyList());
	}

	@Test
	public void testParseCyclesNullReport() {
		List<TestCycle> cycleList = parser.parseCycles(null);
		assertEquals(cycleList, Collections.emptyList());
	}

	@Test
	public void testParseCyclesInvalidReport() {
		JsonElement jsonTree = jparser.parse("{_embedded :{" +
				"        \"CycleHistory\": [\n" +
				"            {}]}}");
		List<TestCycle> cycleList = parser.parseCycles(jsonTree);
		assertEquals(1, cycleList.size());

		TestCycle cycle = cycleList.get(0);
		assertEquals(cycle.getCycle(), 0);
		assertNull(null);
		assertEquals(cycle.getElapsedTimeInMillSec(), 0);
		assertEquals(cycle.getMessages(), Collections.emptyList());
		assertNull(cycle.getStart());
		assertNull(cycle.getStop());
		assertEquals(cycle.getState(), TestState.FAILED);
	}

	@Test
	public void testParseCyclesBadJsonSyntax() {
		List<TestCycle> cycleList = parser.parseCyclesForTest(
				"src/test/resources/report/buildstep/suites/failsuite/failcase/cycle.json");
		assertEquals(0, cycleList.size());
	}

	/**
	 * Cases parser tests
	 */
	@Test
	public void testParseCases() {
		String jsonString = readFile("src/test/resources/report/cases.json");
		JsonElement jsonTree = jparser.parse(jsonString);
		List<TestCase> casesList = parser.parseCases(jsonTree, Collections.emptyList());
		assertEquals(2, casesList.size());

		TestCase testCase = casesList.get(0);
		assertEquals(testCase.getTestCaseId(), "4414E9C0E5B211E7BADA020027E349EE");
		assertEquals(testCase.getName(), "Run1User10Cycle");
	}

	@Test
	public void testParseCasesEmptyReport() {
		JsonElement jsonTree = jparser.parse("{}");
		List<TestCase> casesList = parser.parseCases(jsonTree, Collections.emptyList());
		assertEquals(casesList, Collections.emptyList());
	}

	@Test
	public void testParseCasesNullReport() {
		List<TestCase> casesList = parser.parseCases(null, Collections.emptyList());
		assertEquals(casesList, Collections.emptyList());
	}

	@Test
	public void testParseCasesInvalidReport() {
		JsonElement jsonTree = jparser.parse("{_embedded :{" +
				"        \"Tests\": [\n" +
				"            {}]}}");

		List<TestCase> casesList = parser.parseCases(jsonTree, Collections.emptyList());
		assertEquals(1, casesList.size());

		TestCase testCase = casesList.get(0);
		Assert.assertNull(testCase.getName());
	}

	@Test
	public void testParseCaseNullJsonElement() {
		TestCase testCase = parser.parseCase(null, null);
		assertNull(testCase);
	}


	/**
	 * Simple case parser tests
	 */
	@Test
	public void testParseSimpleCase() throws ParseException {
		String jsonString = readFile(
				"src/test/resources/report/buildstep/suites/suite/tests/case/test.json");
		JsonElement jsonTree = jparser.parse(jsonString);
		TestCase testCase = parser.parseSimpleCase(jsonTree, Collections.emptyList());

		assertEquals(testCase.getTestCaseId(), "EAC6BDD9E5AF11E7BADA020027E349EE");
		assertEquals(testCase.getElapsedTimeInMillSec(), 55050);
		assertEquals(testCase.getStart(),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS").parse("2017-12-20T13:02:20-0500"));
		assertEquals(testCase.getStop(),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS").parse("2017-12-20T13:02:25-0500"));
		assertEquals(testCase.getState(), TestState.FAILED);

		assertEquals(testCase.getName(), "Run1User1Cycle");
		assertEquals(0, testCase.getCycles().size());
	}

	@Test
	public void testParseSimpleCaseNullReport() {
		TestCase testCase = parser.parseSimpleCase(null, Collections.emptyList());
		Assert.assertNull(testCase);
	}

	@Test
	public void testParseCasesBadJsonSyntax() {
		TestCase testCase = parser
				.parseCaseForTest("src/test/resources/report/buildstep/suites/failsuite/failcase/");
		assertNull(testCase);
	}


	@Test
	public void testParseSuite() throws ParseException {
		String jsonString = readFile("src/test/resources/report/buildstep/suites/suite/suite.json");
		JsonElement jsonTree = jparser.parse(jsonString);
		TestSuite suite = parser.parseSuite(jsonTree, new ArrayList<>());

		assertEquals(suite.getAbortCount(), 1);
		assertEquals(suite.getElapsedTime(), "9887");
		assertEquals(suite.getFailCount(), 0);
		assertEquals(suite.getStart(),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS").parse("2018-05-22T10:05:25-0400"));
		assertEquals(suite.getStop(),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS").parse("2018-05-22T10:05:35-0400"));
		assertEquals(suite.getPassCount(), 0);
		assertEquals(suite.getName(), "AllTestsSuite");
		assertEquals(suite.getTestCases(), Collections.emptyList());
		assertEquals(suite.getTotalTestsCount(), 10);
		assertEquals(suite.getWarningCount(), 0);

	}

	@Test
	public void testGetCasesIdsFromSuite() {
		String jsonString = readFile("src/test/resources/report/suitecases.json");
		List<String> ids = parser.getCasesIdsFromSuite(jsonString);
		assertEquals(ids.size(), 2);
		assertEquals(ids.get(0), "4414E9C0E5B211E7BADA020027E349EE");
		assertEquals(ids.get(1), "60447D97E5B211E7BADA020027E349EE");
	}

	@Test
	public void testGetCasesIdsFromInvalidSuiteJson() {
		List<String> cycleList = parser.getCasesIdsFromSuite("{,,,//}");
		assertEquals(cycleList.size(), 0);
	}

	@Test
	public void testParseSuiteBadJsonSyntax()  {
		List<TestSuite> suites = parser.getSuites("src/test/resources/report/buildstep/suites");
		assertEquals(suites.size(), 0);
	}

	@Test
	public void testParseWholeSuite() throws ParseException {
		Report report = parser.parse("src/test/resources/report");

		assertEquals(report.getSuites().size(), 1);
		TestSuite suite = report.getSuites().get(0);
		assertEquals(suite.getAbortCount(), 1);
		assertEquals(suite.getElapsedTime(), "9887");
		assertEquals(suite.getFailCount(), 0);
		assertEquals(suite.getStart(),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS").parse("2018-05-22T10:05:25-0400"));
		assertEquals(suite.getStop(),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS").parse("2018-05-22T10:05:35-0400"));
		assertEquals(suite.getPassCount(), 0);
		assertEquals(suite.getName(), "AllTestsSuite");
		Assert.assertNotNull(suite.getTestCases());

		TestSuite testSuite = report.getSuites().get(0);
		assertEquals(testSuite.getAbortCount(), 1);
		assertEquals(testSuite.getElapsedTime(), "9887");

		assertEquals(suite.getTestCases().size(), 1);

		TestCase testCase = suite.getTestCases().get(0);

		assertEquals(testCase.getTestCaseId(), "EAC6BDD9E5AF11E7BADA020027E349EE");
		assertEquals(testCase.getElapsedTimeInMillSec(), 55050);
		assertEquals(testCase.getStart(),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS").parse("2017-12-20T13:02:20-0500"));
		assertEquals(testCase.getStop(),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS").parse("2017-12-20T13:02:25-0500"));
		assertEquals(testCase.getState(), TestState.FAILED);

		TestCycle cycle = suite.getTestCases().get(0).getCycles().get(0);

		assertEquals(cycle.getTestCycleId(), "442F2822E5B211E7B46E020027E349EE");
		assertEquals(cycle.getCycle(), 0);
		assertEquals(cycle.getElapsedTimeInMillSec(), 4443);
		assertEquals(cycle.getMessages(), Collections.emptyList());
		assertEquals(cycle.getStart(),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS").parse("2017-12-20T13:19:06-0500"));
		assertEquals(cycle.getStop(),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS").parse("2017-12-20T13:19:11-0500"));
		assertEquals(cycle.getState(), TestState.FAILED);

		assertEquals(suite.getTotalTestsCount(), 10);
		assertEquals(suite.getWarningCount(), 0);

		testCase = report.getStandAloneCases().get(0);

		assertEquals(testCase.getTestCaseId(), "0000BDD9E5AF11E7BADA020027E349EE");
		assertEquals(testCase.getElapsedTimeInMillSec(), 5505);
		assertEquals(testCase.getStart(),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS").parse("2017-12-20T13:02:20-0500"));
		assertEquals(testCase.getStop(),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS").parse("2017-12-20T13:02:25-0500"));
		assertEquals(testCase.getState(), TestState.PASSED);
		assertEquals(suite.getTotalTestsCount(), 10);
		assertEquals(suite.getWarningCount(), 0);
	}

	private String readFile(String filePath) {
		StringBuilder contentBuilder = new StringBuilder();
		try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contentBuilder.toString();
	}

}
