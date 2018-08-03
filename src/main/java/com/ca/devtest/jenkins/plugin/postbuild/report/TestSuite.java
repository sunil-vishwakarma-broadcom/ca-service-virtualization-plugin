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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test Suite.
 *
 * @author mykdm01
 */
public class TestSuite implements Serializable {

	private static final long serialVersionUID = -414743477095600405L;

	private List<TestCase> testCases;

	private String name;

	private String elapsedTime;

	private Date start;

	private Date stop;

	private int totalTestsCount;

	private int passCount;

	private int failCount;

	private int warningCount;

	private int abortCount;


	private TestSuite(TestSuiteBuilder builder) {
		this.name = builder.name;
		this.testCases = builder.testCases;
		this.elapsedTime = builder.elapsedTime;
		this.start = builder.start;
		this.stop = builder.stop;
		this.totalTestsCount = builder.totalTestsCount;
		this.passCount = builder.passCount;
		this.failCount = builder.failCount;
		this.warningCount = builder.warningCount;
		this.abortCount = builder.abortCount;
	}

	public List<TestCase> getTestCases() {
		return testCases;
	}


	public String getName() {
		return name;
	}


	public String getElapsedTime() {
		return elapsedTime;
	}

	/**
	 * Gets testsuite start date.
	 *
	 * @return testsuite start date
	 */
	public Date getStart() {
		if (start != null) {
			return new Date(start.getTime());
		}
		return null;
	}

	/**
	 * Gets testsuite stop date.
	 *
	 * @return testsuite stop date
	 */
	public Date getStop() {
		if (stop != null) {
			return new Date(stop.getTime());
		}
		return null;
	}


	public int getTotalTestsCount() {
		return totalTestsCount;
	}


	public int getPassCount() {
		return passCount;
	}


	public int getFailCount() {
		return failCount;
	}


	public int getWarningCount() {
		return warningCount;
	}


	public int getAbortCount() {
		return abortCount;
	}

	/**
	 * Returns list of failed test cases in current suite.
	 *
	 * @return list of failed test cases
	 */
	public List<TestCase> getFailedTests() {
		return this.testCases.stream().filter(testCase -> testCase.getState() != TestState.PASSED)
												 .collect(
														 Collectors.toList());
	}

	/**
	 * Returns list of successful test cases in current suite.
	 *
	 * @return list of successful test cases
	 */
	public List<TestCase> getSuccessfullTests() {
		return this.testCases.stream().filter(testCase -> testCase.getState() == TestState.PASSED)
												 .collect(
														 Collectors.toList());
	}

	public static class TestSuiteBuilder {

		private String name;

		private String elapsedTime;

		private Date start;

		private Date stop;

		private int totalTestsCount;

		private int passCount;

		private int failCount;

		private int warningCount;

		private int abortCount;

		private List<TestCase> testCases = new ArrayList<>();

		private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS");

		/**
		 * Sets test cases.
		 *
		 * @param testCases test cases
		 *
		 * @return a reference to this object
		 */
		public TestSuiteBuilder withTestCases(List<TestCase> testCases) {
			if (testCases != null) {
				this.testCases = testCases;
			}
			return this;
		}

		/**
		 * Sets the name.
		 *
		 * @param name name
		 *
		 * @return a reference to this object
		 */
		public TestSuiteBuilder withName(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Sets suite duration.
		 *
		 * @param elapsedTime string test duration
		 *
		 * @return a reference to this object
		 */
		public TestSuiteBuilder withElapsedTime(String elapsedTime) {
			this.elapsedTime = elapsedTime;
			return this;
		}

		/**
		 * Sets suite start moment as Date.
		 *
		 * @param start string timestamp in yyyy-MM-dd'T'HH:mm:ss-SSSS format
		 *
		 * @return a reference to this object
		 */
		public TestSuiteBuilder withStart(String start) {
			if (start != null) {
				try {
					this.start = format.parse(start);
				} catch (ParseException ex) {
					this.start = null;
				}
			}
			return this;
		}

		/**
		 * Sets suite stop moment as Date.
		 *
		 * @param stop string timestamp in yyyy-MM-dd'T'HH:mm:ss-SSSS format
		 *
		 * @return a reference to this object
		 */
		public TestSuiteBuilder withStop(String stop) {
			if (stop != null) {
				try {
					this.stop = format.parse(stop);
				} catch (ParseException ex) {
					this.stop = null;
				}
			}
			return this;
		}

		/**
		 * Sets total test case count in suite.
		 *
		 * @param totalTestsCount test case count
		 *
		 * @return a reference to this object
		 */
		public TestSuiteBuilder withTotalTestsCount(String totalTestsCount) {
			try {
				this.totalTestsCount = Integer.parseInt(totalTestsCount);
			} catch (NumberFormatException ex) {
				this.totalTestsCount = 0;
			}
			return this;
		}

		/**
		 * Sets passed test case count in suite.
		 *
		 * @param passCount passed test case count
		 *
		 * @return a reference to this object
		 */
		public TestSuiteBuilder withPassCount(String passCount) {
			try {
				this.passCount = Integer.parseInt(passCount);
			} catch (NumberFormatException ex) {
				this.passCount = 0;
			}
			return this;
		}

		/**
		 * Sets failed test case count in suite.
		 *
		 * @param failCount failed test case count
		 *
		 * @return a reference to this object
		 */
		public TestSuiteBuilder withFailCount(String failCount) {
			try {
				this.failCount = Integer.parseInt(failCount);
			} catch (NumberFormatException ex) {
				this.failCount = 0;
			}
			return this;
		}

		/**
		 * Sets warned test case count in suite.
		 *
		 * @param warningCount warned test case count
		 *
		 * @return a reference to this object
		 */
		public TestSuiteBuilder withWarningCount(String warningCount) {
			try {
				this.warningCount = Integer.parseInt(warningCount);
			} catch (NumberFormatException ex) {
				this.warningCount = 0;
			}
			return this;
		}

		/**
		 * Sets aborted test case count in suite.
		 *
		 * @param abortCount aborted test case count
		 *
		 * @return a reference to this object
		 */
		public TestSuiteBuilder withAbortCount(String abortCount) {
			try {
				this.abortCount = Integer.parseInt(abortCount);
			} catch (NumberFormatException ex) {
				this.abortCount = 0;
			}
			return this;
		}

		public TestSuite build() {
			return new TestSuite(this);
		}

	}

}
