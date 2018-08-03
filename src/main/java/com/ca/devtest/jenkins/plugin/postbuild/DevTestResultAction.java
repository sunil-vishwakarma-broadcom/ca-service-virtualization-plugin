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

import com.ca.devtest.jenkins.plugin.Messages;
import com.ca.devtest.jenkins.plugin.postbuild.report.Report;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Api;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Action for DevTest reporting post build step.
 *
 * @author mykdm01
 */
public class DevTestResultAction extends AbstractTestResultAction implements Serializable,
		SimpleBuildStep.LastBuildAction {


	private static final long serialVersionUID = 1094502348958367308L;

	private Report report;

	public Report getReport() {
		return report;
	}

	//this is default jenkins icon
	@Override
	public String getIconFileName() {
		return "clipboard.png";
	}

	@Override
	public Report getResult() {
		return report;
	}

	@Override
	public int getFailCount() {
		return report.getFailCount();
	}

	@Override
	public int getTotalCount() {
		return report.getTotalCount();
	}

	@Override
	public String getDisplayName() {
		return Messages.DevTestReport_Title();
	}

	@Override
	public String getUrlName() {
		return Messages.DevTestReport_Url();
	}

	/**
	 * Constructor.
	 *
	 * @param run  current run
	 * @param report results report
	 */
	public DevTestResultAction(final Run<?, ?> run, Report report) {
		this.report = report;
		report.setRun(run);
	}

	public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
		return getResult().getDynamic(token, req, rsp);
	}


	@Override
	public Api getApi() {
		return new Api(getResult());
	}

	@Override
	public Collection<? extends Action> getProjectActions() {
		//no sub actions return empty list
		return Collections.emptyList();
	}

}
