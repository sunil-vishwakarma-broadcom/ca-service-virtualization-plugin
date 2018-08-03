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
 *
 ******************************************************************************/

package com.ca.devtest.jenkins.plugin.buildstep;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

/**
 * Abstract descriptor, parent for buildstep descriptors.
 *
 * @author mykdm01
 */
public abstract class DefaultDescriptor extends BuildStepDescriptor<Builder> {

	/**
	 * Method used to fill dropdown with credential tokens.
	 *
	 * @param project       project
	 * @param credentialsId credentials Id
	 *
	 * @return listbox model
	 */
	public ListBoxModel doFillTokenCredentialIdItems(@AncestorInPath Item project,
			@QueryParameter String credentialsId) {
		if (project == null && !Jenkins.getActiveInstance().hasPermission(Jenkins.ADMINISTER)
				|| project != null && !project.hasPermission(Item.EXTENDED_READ)) {
			return new StandardListBoxModel().includeCurrentValue(credentialsId);
		}

		return new StandardListBoxModel()
				.withEmptySelection()
				.withAll(CredentialsProvider.lookupCredentials(
						StandardUsernamePasswordCredentials.class,
						Jenkins.getInstance(),
						ACL.SYSTEM)
				);
	}

}
