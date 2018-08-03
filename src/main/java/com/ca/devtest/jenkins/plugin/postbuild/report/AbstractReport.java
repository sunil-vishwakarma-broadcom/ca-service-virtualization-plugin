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

import com.ca.devtest.jenkins.plugin.postbuild.DevTestResultAction;
import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestResult;
import java.io.Serializable;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Base class for all results.
 */
@SuppressWarnings("serial")
@ExportedBean
public abstract class AbstractReport extends TabulatedResult implements ModelObject, Serializable {

	protected Run<?, ?> run;

	protected final String name;

	protected AbstractReport parent;

	public AbstractReport(String name) {
		this.name = name;
	}

	@Exported(visibility = 999)
	@Override
	public String getName() {
		return name;
	}

	@Override
	public AbstractReport getParent() {
		return parent;
	}

	public void setParent(AbstractReport parent) {
		this.parent = parent;
	}

	@Override
	public Run<?, ?> getRun() {
		return run;
	}

	public void setRun(Run<?, ?> run) {
		this.run = run;
	}

	@Override
	public String getTitle() {
		return getName();
	}

	public String getDisplayName() {
		return getName();
	}

	public String getUpUrl() {
		Jenkins j = Jenkins.getInstance();
		return j.getRootUrl() + run.getUrl() + getId();
	}

	/**
	 * Retrieving dynamic report object by request uri token.
	 *
	 * @return test report object
	 */
	@Override
	public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
		for (TestResult result : this.getChildren()) {
			//for TestCase uri identifier is id
			if (result instanceof TestCase) {
				if (token.equals(((TestCase) result).getTestCaseId())) {
					return result;
				}
			} else if (result instanceof TestCycle) {
				//for TestCycle uri identifier is id
				if (token.equals(((TestCycle) result).getTestCycleId())) {
					return result;
				}
			} else {
				//otherwise use getId() from parent, this would use name as identifier
				if (token.equals(result.getId())) {
					return result;
				}

			}

		}
		return null;
	}

	/**
	 * Explicit override here to ensure that when we are building DevTest reports,
	 * we are only working with DevTest results.
	 */
	@Override
	public AbstractTestResultAction getTestResultAction() {
		Run<?, ?> run = getRun();
		if (run != null) {
			return run.getAction(DevTestResultAction.class);
		}
		return null;
	}

	/**
	 * {@link AbstractReport#getTestResultAction()}.
	 */
	@Override
	public AbstractTestResultAction getParentAction() {
		return getTestResultAction();
	}

	@Override
	public TestResult findCorrespondingResult(String id) {
		if (getId().equals(id) || id == null) {
			return this;
		}

		int sepIdx = id.indexOf('/');
		if (sepIdx < 0) {
			if (getSafeName().equals(id)) {
				return this;
			}
		} else {
			String currId = id.substring(0, sepIdx);
			if (!getSafeName().equals(currId)) {
				return null;
			}

			String childId = id.substring(sepIdx + 1);
			sepIdx = childId.indexOf('/');

			for (TestResult result : this.getChildren()) {
				if (sepIdx < 0 && childId.equals(result.getSafeName())) {
					return result;
				} else if (sepIdx > 0 && result.getSafeName().equals(childId.substring(0, sepIdx))) {
					return result.findCorrespondingResult(childId);
				}
			}
		}
		return null;
	}


	/**
	 * Gets the age of a result.
	 *
	 * @return the number of consecutive builds for which we have a result for this package
	 */
	public long getAge() {
		AbstractReport result = (AbstractReport) getPreviousResult();
		if (result == null) {
			return 1;
		} else {
			return 1 + result.getAge();
		}
	}

}
