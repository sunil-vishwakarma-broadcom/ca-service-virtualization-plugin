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

/**
 * Test Cycle report representation.
 *
 * @author mykdm01
 */
public class TestCycle extends AbstractReport {

	private static final long serialVersionUID = -8800849699362689033L;

	private String id;

	private int cycle;

	private Date start;

	private Date stop;

	private TestState state;

	private int elapsedTimeInMillSec;

	private List<String> messages;

	private String rawCycleReport;

	private TestCycle(TestCycleBuilder builder) {
		super(builder.id);
		this.id = builder.id;
		this.cycle = builder.cycle;
		this.start = builder.start;
		this.stop = builder.stop;
		this.state = builder.state;
		this.elapsedTimeInMillSec = builder.elapsedTimeInMillSec;
		if (builder.messages != null) {
			this.messages = builder.messages;
		}
		this.rawCycleReport = builder.rawCycleReport;
	}

	public String getTestCycleId() {
		return id;
	}

	@Override
	public String getName() {
		return (cycle + 1) + " cycle";
	}

	public int getCycle() {
		return cycle;
	}

	/**
	 * Gets testcycle start date.
	 *
	 * @return testcycle start date
	 */
	public Date getStart() {
		if (start != null) {
			return new Date(start.getTime());
		}
		return null;
	}

	/**
	 * Gets testcycle stop date.
	 *
	 * @return testcycle stop date
	 */
	public Date getStop() {
		if (stop != null) {
			return new Date(stop.getTime());
		}
		return null;
	}

	public TestState getState() {
		return state;
	}

	public int getElapsedTimeInMillSec() {
		return elapsedTimeInMillSec;
	}

	public List<String> getMessages() {
		return messages;
	}

	public String getRawCycleReport() {
		return rawCycleReport;
	}

	@Override
	public Collection<? extends TestResult> getChildren() {
		return null;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public String getUrl() {
		return '/' + this.id;
	}

	public static class TestCycleBuilder {

		private String id;

		private int cycle;

		private Date start;

		private Date stop;

		private TestState state = TestState.FAILED;

		private int elapsedTimeInMillSec;

		private List<String> messages = new ArrayList<>();

		private String rawCycleReport;

		private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSSS");

		/**
		 * Sets the id.
		 *
		 * @param id id
		 *
		 * @return a reference to this object
		 */
		public TestCycleBuilder withId(String id) {
			this.id = id;
			return this;
		}

		/**
		 * Sets cycle start moment as Date.
		 *
		 * @param start string timestamp in yyyy-MM-dd'T'HH:mm:ss-SSSS format
		 *
		 * @return a reference to this object
		 */
		public TestCycleBuilder withStart(String start) {
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
		 * Sets cycle stop moment as Date.
		 *
		 * @param stop string timestamp in yyyy-MM-dd'T'HH:mm:ss-SSSS format
		 *
		 * @return a reference to this object
		 */
		public TestCycleBuilder withStop(String stop) {
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
		 * Sets cycle number.
		 *
		 * @param cycle cycle
		 *
		 * @return a reference to this object
		 */
		public TestCycleBuilder withCycle(String cycle) {
			if (cycle != null) {
				try {
					this.cycle = Integer.parseInt(cycle);
				} catch (NumberFormatException ex) {
					this.cycle = 0;
				}
			}
			return this;
		}

		/**
		 * Sets cycle finish state.
		 *
		 * @param state state
		 *
		 * @return a reference to this object
		 */
		public TestCycleBuilder withState(String state) {
			if (state != null) {
				try {
					this.state = TestState.valueOf(state);
				} catch (IllegalArgumentException ex) {
					this.state = TestState.FAILED;
				}
			}
			return this;
		}

		/**
		 * Sets cycle duration.
		 *
		 * @param elapsedTimeInMillSec string test duration
		 *
		 * @return a reference to this object
		 */
		public TestCycleBuilder withElapsedTime(String elapsedTimeInMillSec) {
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
		 * Sets cycle response messages.
		 *
		 * @param messages messages
		 *
		 * @return a reference to this object
		 */
		public TestCycleBuilder withMessages(List<String> messages) {
			if (messages != null) {
				this.messages = messages;
			}
			return this;
		}

		/**
		 * Sets raw JSON report.
		 *
		 * @param rawReport json
		 *
		 * @return a reference to this object
		 */
		public TestCycleBuilder withRawCycleReport(String rawReport) {
			this.rawCycleReport = rawReport.toString();
			return this;
		}

		public TestCycle build() {
			return new TestCycle(this);
		}

	}


}
