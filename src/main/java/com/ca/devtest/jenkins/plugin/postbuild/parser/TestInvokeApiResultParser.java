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

import com.ca.devtest.jenkins.plugin.Messages;
import com.ca.devtest.jenkins.plugin.postbuild.report.Report;
import com.ca.devtest.jenkins.plugin.postbuild.report.TestCase;
import com.ca.devtest.jenkins.plugin.postbuild.report.TestCycle;
import com.ca.devtest.jenkins.plugin.postbuild.report.TestSuite;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Result parser implementation for Test invoke API.
 *
 * @author mykdm01
 */
public class TestInvokeApiResultParser implements Parser {

	private PrintStream logger;

	private JsonParser jparser = new JsonParser();

	private Gson prettyParser = new GsonBuilder().setPrettyPrinting().create();


	public TestInvokeApiResultParser(PrintStream logger) {
		this.logger = logger;
	}

	@Override
	public Report parse(String rootDir) {
		logger.println(Messages.DevTestParser_Start());
		return new Report(getSuites(rootDir), getStandAloneTests(rootDir));
	}

	public Report parseStep(String stepDir) {
		return new Report(getSuiteForStep(stepDir), getStandAloneTestForStep(stepDir));
	}

	@Override
	public List<String> getCasesIdsFromSuite(String suiteCasesJson) {
		if (suiteCasesJson == null) {
			return Collections.emptyList();
		}
		JsonElement jsonTree = null;
		try {
			jsonTree = jparser.parse(suiteCasesJson);
		} catch (Exception ex) {
			logger.println(Messages.DevTestParser_SuiteIDNotParsed());
		}
		List<TestCase> casesList = parseCases(jsonTree, Collections.emptyList());
		return casesList.stream().map(testCase -> testCase.getTestCaseId()).collect(
				Collectors.toList());
	}

	TestSuite parseSuite(JsonElement report, List<TestCase> cases) {
		if (report != null && report.isJsonObject()) {
			JsonObject suiteObject = report.getAsJsonObject();
			TestSuite suite = new TestSuite.TestSuiteBuilder()
					.withName(safelyGetStringValue(suiteObject, "suiteName"))
					.withElapsedTime(safelyGetStringValue(suiteObject, "elapsedTimeInMillSec"))
					.withStart(safelyGetStringValue(suiteObject, "startTime"))
					.withStop(safelyGetStringValue(suiteObject, "endTime"))
					.withTotalTestsCount(safelyGetStringValue(suiteObject, "totalTestsExecuted"))
					.withPassCount(safelyGetStringValue(suiteObject, "passCount"))
					.withFailCount(safelyGetStringValue(suiteObject, "failCount"))
					.withWarningCount(safelyGetStringValue(suiteObject, "warningCount"))
					.withAbortCount(safelyGetStringValue(suiteObject, "abortCount"))
					.withTestCases(cases)
					.build();
			suite.getTestCases().stream().forEach(testCase -> testCase.setSuiteName(suite.getName()));
			return suite;
		}
		return null;
	}


	TestCase parseSimpleCase(JsonElement report, List<TestCycle> cycles) {
		if (report != null && report.isJsonObject()) {
			JsonObject caseObject = report.getAsJsonObject();

			TestCase testCase = new TestCase.TestCaseBuilder()
					.withId(safelyGetStringValue(caseObject, "testRunUniqueId"))
					.withName(safelyGetStringValue(caseObject, "testcaseName"))
					.withState(safelyGetStringValue(caseObject, "endedState"))
					.withElapsedTime(safelyGetStringValue(caseObject, "elapsedTimeInMillSec"))
					.withStart(safelyGetStringValue(caseObject, "startTime"))
					.withStop(safelyGetStringValue(caseObject, "endTime"))
					.withCycles(cycles)
					.build();
			return testCase;
		}
		return null;
	}

	List<TestCase> parseCases(JsonElement report, List<TestCycle> cycles) {
		if (report != null && report.isJsonObject()) {
			JsonElement casesElement = report.getAsJsonObject().get("_embedded");
			if (casesElement != null && casesElement.isJsonObject()) {
				casesElement = casesElement.getAsJsonObject().get("Tests");
				if (casesElement != null && casesElement.isJsonArray()) {
					JsonArray casesArray = casesElement.getAsJsonArray();
					List<TestCase> cases = new ArrayList<>();
					for (JsonElement caseItem : casesArray) {
						TestCase testCase = parseCase(caseItem, cycles);
						if (testCase != null) {
							cases.add(testCase);
						}
					}
					return cases;
				}
			}
		}
		return new ArrayList<>();
	}

	TestCase parseCase(JsonElement caseElement, List<TestCycle> cycles) {
		if (caseElement == null || !caseElement.isJsonObject()) {
			return null;
		}
		JsonObject caseObject = caseElement.getAsJsonObject();
		TestCase testCase = new TestCase.TestCaseBuilder()
				.withId(safelyGetStringValue(caseObject, "testRunUniqueId"))
				.withName(safelyGetStringValue(caseObject, "testcaseName"))
				.withCycles(cycles)
				.build();
		return testCase;
	}


	List<TestCycle> parseCycles(JsonElement report) {
		if (report != null && report.isJsonObject()) {
			JsonElement cyclesElement = report.getAsJsonObject().get("_embedded");
			if (cyclesElement != null && cyclesElement.isJsonObject()) {
				cyclesElement = cyclesElement.getAsJsonObject().get("CycleHistory");
				if (cyclesElement != null && cyclesElement.isJsonArray()) {
					JsonArray cyclesArray = cyclesElement.getAsJsonArray();
					List<TestCycle> cycles = new ArrayList<>();
					for (JsonElement cycle : cyclesArray) {
						TestCycle testCycle = parseCycle(cycle);
						if (testCycle != null) {
							cycles.add(testCycle);
						}
					}
					return cycles;
				}
			}
		}
		return new ArrayList<>();
	}

	TestCycle parseCycle(JsonElement cycleElement) {
		if (cycleElement == null || !cycleElement.isJsonObject()) {
			return null;
		}
		JsonObject cycle = cycleElement.getAsJsonObject();
		TestCycle testCycle = new TestCycle.TestCycleBuilder()
				.withId(safelyGetStringValue(cycle, "cycleUniqueId"))
				.withCycle(safelyGetStringValue(cycle, "cycle"))
				.withElapsedTime(safelyGetStringValue(cycle, "elapsedTimeInMillSec"))
				.withStart(safelyGetStringValue(cycle, "startTime"))
				.withStop(safelyGetStringValue(cycle, "endTime"))
				.withState(safelyGetStringValue(cycle, "endedState"))
				.withMessages(safelyGetStringList(cycle.get("_embedded"), "messages"))
				.withRawCycleReport(prettyParser.toJson(cycle))
				.build();
		return testCycle;
	}

	private String safelyGetStringValue(JsonElement element, String field) {
		if (element != null && field != null && element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.get(field) != null) {
				return object.get(field).getAsString();
			}
		}
		logger.println(Messages.DevTestParser_FieldNotExists(field));
		return null;
	}

	private List<String> safelyGetStringList(JsonElement element, String field) {
		if (element != null && field != null && element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object != null && object.get(field) != null && object.get(field).isJsonArray()) {
				JsonArray array = object.get(field).getAsJsonArray();
				List<String> result = new ArrayList<>();
				for (JsonElement item : array) {
					if (item.getAsString() != null) {
						result.add(item.getAsString());
					}
				}
				return result;
			}
		}
		return null;
	}


	List<TestCycle> parseCyclesForTest(String path) {
		String jsonString = readReport(path);
		JsonElement report = null;
		try {
			report = jparser.parse(jsonString);
		} catch (Exception ex) {
			logger.println(Messages.DevTestParser_CycleNotParsed());
		}
		return parseCycles(report);

	}

	TestCase parseCaseForTest(String caseDir) {
		String jsonString = readReport(caseDir + "/test.json");
		JsonElement report = null;
		try {
			report = jparser.parse(jsonString);
		} catch (Exception ex) {
			logger.println(Messages.DevTestParser_CaseNotParsed());
		}
		List<TestCycle> cycles = parseCyclesForTest(caseDir + "/cycles.json");
		return parseSimpleCase(report, cycles);
	}

	List<TestSuite> getSuites(String rootDir) {
		logger.println(Messages.DevTestParser_StartSuite());
		List<String> stepDirs = getDirs(rootDir);
		List<TestSuite> suites = new ArrayList<>();
		for (String stepDir : stepDirs) {
			suites.addAll(getSuiteForStep(stepDir));
		}
		return suites;
	}

	private List<TestSuite> getSuiteForStep(String stepDir) {
		List<String> suiteDirs = getDirs(stepDir + "/suites");
		List<TestSuite> resSuites = new ArrayList<>();
		for (String suiteDir : suiteDirs) {
			List<String> testDirs = getDirs(suiteDir + "/tests");
			List<TestCase> testCases = new ArrayList<>();
			for (String dirPath : testDirs) {
				TestCase testCase = parseCaseForTest(dirPath);
				if (testCase != null) {
					testCases.add(testCase);
				} else {
					logger.println(Messages.DevTestParser_CaseNotParsedIn(dirPath));
				}
			}
			String jsonString = readReport(suiteDir + "/suite.json");
			JsonElement report = null;
			try {
				report = jparser.parse(jsonString);
			} catch (Exception ex) {
				logger.println(Messages.DevTestParser_SuiteNotParsed());
			}
			TestSuite suite = parseSuite(report, testCases);
			if (suite != null) {
				resSuites.add(suite);
			} else {
				logger.println(Messages.DevTestParser_SuiteNotParsedIn(suiteDir));
			}
		}
		return resSuites;
	}


	private List<TestCase> getStandAloneTests(String rootDir) {
		logger.println(Messages.DevTestParser_StartCases());
		List<String> stepDirs = getDirs(rootDir);
		List<TestCase> testCases = new ArrayList<>();
		for (String stepDir : stepDirs) {
			testCases.addAll(getStandAloneTestForStep(stepDir));
		}
		return testCases;
	}

	private List<TestCase> getStandAloneTestForStep(String stepDir) {
		List<TestCase> resTests = new ArrayList<>();
		List<String> testDirs = getDirs(stepDir + "/tests");
		for (String dirPath : testDirs) {
			TestCase testCase = parseCaseForTest(dirPath);
			if (testCase != null) {
				resTests.add(testCase);
			} else {
				logger.println(Messages.DevTestParser_CaseNotParsedIn(dirPath));
			}
		}
		return resTests;
	}

	private List<String> getDirs(String dir) {
		List<String> dirs = new ArrayList<>();
		File[] files = new File(dir).listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					dirs.add(file.getPath());
				}
			}
		}
		return dirs;
	}


	private synchronized String readReport(String reportPath) {
		String content = "";
		try {
			content = new String(Files.readAllBytes(Paths.get(reportPath)), Charset.forName("UTF-8"));
		} catch (IOException e) {
			logger.println(Messages.DevTestParser_FailedReadFile(reportPath));
		}
		return content;
	}

}
