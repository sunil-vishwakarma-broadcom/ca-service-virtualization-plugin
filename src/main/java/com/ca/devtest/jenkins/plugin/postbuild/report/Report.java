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

package com.ca.devtest.jenkins.plugin.postbuild.report;

import com.ca.devtest.jenkins.plugin.Messages;
import hudson.model.Run;
import hudson.tasks.test.TestResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core DevTest report class, contains suite and standalone tests reports.
 *
 * @author mykdm01
 */
public class Report extends AbstractReport {

	private static final long serialVersionUID = 8006349377695561300L;

	private List<TestSuite> suites = Collections.emptyList();

	private List<TestCase> standAloneCases = Collections.emptyList();

	private List<TestCase> failedTests;

	private List<TestCase> successfullTests;

	private List<TestCase> allTests;

	/**
	 * Constructor.
	 *
	 * @param suites          list of parsed test suites
	 * @param standAloneCases test cases without suite
	 */
	public Report(List<TestSuite> suites, List<TestCase> standAloneCases) {
		super(Messages.DevTestReport_Url());
		if (suites != null) {
			this.suites = suites;
		}
		if (standAloneCases != null) {
			this.standAloneCases = standAloneCases;
		}

		this.failedTests = findFailedTests();
		this.successfullTests = findSuccessfullTests();
		this.allTests = new ArrayList<>();
		this.allTests.addAll(failedTests);
		this.allTests.addAll(successfullTests);
	}

	public List<TestSuite> getSuites() {
		return suites;
	}

	public List<TestCase> getStandAloneCases() {
		return standAloneCases;
	}

	public List<TestCase> getSuccessfullTests() {
		return successfullTests;
	}

	@Override
	public List<TestCase> getFailedTests() {
		return failedTests;
	}

	@Override
	public int getFailCount() {
		return failedTests.size();
	}

	@Override
	public AbstractReport getParent() {
		return null;
	}

	/**
	 * Total tests count.
	 */
	@Override
	public int getTotalCount() {
		int count = standAloneCases.size();
		for (TestSuite suite : suites) {
			count += suite.getTotalTestsCount();
		}
		return count;
	}

	/**
	 * Subreport items for main report.
	 * This makes available to access subreport on a new page.
	 */
	@Override
	public Collection<? extends TestResult> getChildren() {
		return allTests;
	}

	@Override
	public boolean hasChildren() {
		return !failedTests.isEmpty();
	}

	@Override
	public String getDisplayName() {
		return name;
	}

	/**
	 * Sets build in subreport items to correctly display sidebar.
	 */
	@Override
	public void setRun(Run<?, ?> run) {
		this.run = run;
		for (TestCase testCase : allTests) {
			testCase.setRun(run);
			for (TestCycle cycle : testCase.getCycles()) {
				cycle.setRun(run);
			}
		}
	}

	private List<TestCase> findFailedTests() {
		List<TestCase> failedTests = standAloneCases.stream().filter(
				testCase -> testCase.getState() != TestState.PASSED).collect(
				Collectors.toList());
		for (TestSuite suite : suites) {
			failedTests.addAll(suite.getFailedTests());
		}
		return failedTests;
	}

	private List<TestCase> findSuccessfullTests() {
		List<TestCase> failedTests = standAloneCases.stream().filter(
				testCase -> testCase.getState() == TestState.PASSED).collect(
				Collectors.toList());
		for (TestSuite suite : suites) {
			failedTests.addAll(suite.getSuccessfullTests());
		}
		return failedTests;
	}
}

