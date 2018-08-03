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

import hudson.tasks.test.TestResult;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test Case report representation.
 *
 * @author mykdm01
 */
public class TestCase extends AbstractReport {

	private static final long serialVersionUID = -2305165264170674540L;

	private String id;

	private String name;

	private List<TestCycle> cycles;

	private TestState state;

	private Date start;

	private Date stop;

	private int elapsedTimeInMillSec;

	private String suiteName;

	private List<TestCycle> failedCycles;

	private List<TestCycle> succesfullCycles;


	private TestCase(TestCaseBuilder builder) {
		super(builder.id);
		this.id = builder.id;
		this.name = builder.name;
		this.cycles = builder.cycles;
		this.start = builder.start;
		this.stop = builder.stop;
		this.elapsedTimeInMillSec = builder.elapsedTimeInMillSec;
		this.state = builder.state;
		this.suiteName = builder.suiteName;
		this.failedCycles = findFailedCycles();
		this.succesfullCycles = findSuccessfulCycles();
	}

	@Override
	public TestCase getParent() {
		return null;
	}

	public String getTestCaseId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	public List<TestCycle> getCycles() {
		return cycles;
	}

	public TestState getState() {
		return state;
	}

	/**
	 * Gets testcase start date.
	 *
	 * @return testcase start date
	 */
	public Date getStart() {
		if (start != null) {
			return new Date(start.getTime());
		}
		return null;
	}

	/**
	 * Gets testcase stop date.
	 *
	 * @return testcase stop date
	 */
	public Date getStop() {
		if (stop != null) {
			return new Date(stop.getTime());
		}
		return null;
	}

	public int getElapsedTimeInMillSec() {
		return elapsedTimeInMillSec;
	}

	public String getSuiteName() {
		return suiteName == null ? "" : suiteName;
	}

	public void setSuiteName(String suiteName) {
		this.suiteName = suiteName;
	}

	@Override
	public Collection<? extends TestResult> getChildren() {
		return cycles;
	}

	@Override
	public boolean hasChildren() {
		return !cycles.isEmpty();
	}

	@Override
	public String getDisplayName() {
		return name;
	}

	@Override
	public String getUrl() {
		return '/' + this.id;
	}

	/**
	 * Returns the class name depending on {@link TestCase#state}.
	 *
	 * @return the class name
	 */
	public String getCssClass() {
		if (this.state != TestState.PASSED) {
			return "result-failed";
		}
		return "result-passed";
	}

	private List<TestCycle> findFailedCycles() {
		return cycles.stream().filter(testCase -> testCase.getState() != TestState.PASSED).collect(
				Collectors.toList());
	}

	private List<TestCycle> findSuccessfulCycles() {
		return cycles.stream().filter(testCase -> testCase.getState() == TestState.PASSED).collect(
				Collectors.toList());
	}

	public static class TestCaseBuilder {

		private String id;

		private String name;

		private List<TestCycle> cycles = new ArrayList<>();

		private TestState state = TestState.FAILED;

		private Date start;

		private Date stop;

		private int elapsedTimeInMillSec;

		private String suiteName;

		private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS");

		/**
		 * Sets the name.
		 *
		 * @param name name
		 *
		 * @return a reference to this object
		 */
		public TestCaseBuilder withName(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Sets the id.
		 *
		 * @param id id
		 *
		 * @return a reference to this object
		 */
		public TestCaseBuilder withId(String id) {
			this.id = id;
			return this;
		}

		/**
		 * Sets parent suite name.
		 *
		 * @param suiteName suite name
		 *
		 * @return a reference to this object
		 */
		public TestCaseBuilder withSuiteName(String suiteName) {
			this.suiteName = suiteName;
			return this;
		}

		/**
		 * Sets cycles.
		 *
		 * @param cycles cycles
		 *
		 * @return a reference to this object
		 */
		public TestCaseBuilder withCycles(List<TestCycle> cycles) {
			if (cycles != null) {
				this.cycles = cycles;
			}
			return this;
		}

		/**
		 * Sets test start moment as Date.
		 *
		 * @param start string timestamp in yyyy-MM-dd'T'HH:mm:ss-SSSS format
		 *
		 * @return a reference to this object
		 */
		public TestCaseBuilder withStart(String start) {
			if (start != null) {
				try {
					this.start = format.parse(start);
				} catch (ParseException ex) {
					this.stop = null;
				}
			}
			return this;
		}

		/**
		 * Sets test stop moment as Date.
		 *
		 * @param stop string timestamp in yyyy-MM-dd'T'HH:mm:ss-SSSS format
		 *
		 * @return a reference to this object
		 */
		public TestCaseBuilder withStop(String stop) {
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
		 * Sets test duration.
		 *
		 * @param elapsedTimeInMillSec string test duration
		 *
		 * @return a reference to this object
		 */
		public TestCaseBuilder withElapsedTime(String elapsedTimeInMillSec) {
			if (elapsedTimeInMillSec != null) {
				try {
					this.elapsedTimeInMillSec = Integer.parseInt(elapsedTimeInMillSec);
				} catch (NumberFormatException ex) {
					this.elapsedTimeInMillSec = 0;
				}
			}
			return this;
		}

		/**
		 * Sets test finish state.
		 *
		 * @param state state
		 *
		 * @return a reference to this object
		 */
		public TestCaseBuilder withState(String state) {
			if (state != null) {
				try {
					this.state = TestState.valueOf(state);
				} catch (IllegalArgumentException ex) {
					this.state = TestState.FAILED;
				}
			}
			return this;
		}

		public TestCase build() {
			return new TestCase(this);
		}


	}

}

