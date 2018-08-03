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
import com.ca.devtest.jenkins.plugin.postbuild.parser.TestInvokeApiResultParser;
import com.ca.devtest.jenkins.plugin.postbuild.report.Report;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import javax.annotation.Nonnull;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Post build step for gathering test results.
 *
 * @author jakro01
 */
public class DevTestTestPublisher extends Recorder implements SimpleBuildStep {

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@DataBoundConstructor
	public DevTestTestPublisher() {
	}

	@Override
	public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace,
			@Nonnull Launcher launcher, @Nonnull TaskListener listener) {
		Report report = new TestInvokeApiResultParser(listener.getLogger())
				.parse(run.getRootDir().toString() + "/report");
		DevTestResultAction devTestResultAction = new DevTestResultAction(run, report);
		run.addAction(devTestResultAction);
	}


	@Symbol("svPublishTestReport")
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> abstractClass) {
			return true;
		}

		/**
		 * Method used for setting name of the component in Jenkins GUI. In this case it is name of
		 * post build step.
		 *
		 * @return name of post build step
		 */
		@Override
		public String getDisplayName() {
			return Messages.DevTestTestPublisher_DescriptorImpl_DisplayName();
		}

	}
}
