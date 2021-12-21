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

package com.ca.devtest.jenkins.plugin.util;

import com.ca.devtest.jenkins.plugin.DevTestPluginConfiguration;
import com.ca.devtest.jenkins.plugin.Messages;
import com.ca.devtest.jenkins.plugin.buildstep.DefaultBuildStep;
import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Util;
import hudson.model.Item;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.Secret;
import hudson.util.VariableResolver;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import jenkins.model.Jenkins;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.AntPathMatcher;
import org.kohsuke.stapler.AncestorInPath;

/**
 * Utils class providing helping methods.
 *
 * @author jakro01
 */
public class Utils {

	private Utils(){}
	/**
	 * Creates authorization header for basic security.
	 *
	 * @param username username
	 * @param password password
	 *
	 * @return header value
	 *
	 * @throws UnsupportedEncodingException when encoding failed
	 */
	public static String createBasicAuthHeader(String username, String password)
			throws UnsupportedEncodingException {
		String header = username + ":" + password;

		return "Basic " + Base64.encodeBase64String(header.getBytes("UTF-8"));
	}

	/**
	 * Helping method for checking custom Registry endpoint input.
	 *
	 * @param useCustomRegistry if custom Registry endpoint should be used
	 * @param host              host to Registry
	 * @param port              port to Registry
	 * @param tokenCredentialId credentials token id
	 *
	 * @return form validation
	 */
	public static FormValidation doCheckHost(@AncestorInPath Item context, boolean useCustomRegistry, String host, String port,
			String tokenCredentialId) {
		StandardUsernamePasswordCredentials jenkinsCredentials = Utils
				.lookupCredentials(context, tokenCredentialId);
		String username = "";
		String password = "";
		if (jenkinsCredentials != null) {
			username = jenkinsCredentials.getUsername();
			password = Secret.toString(jenkinsCredentials.getPassword());
		}
		if (useCustomRegistry) {
			if (host.length() == 0 || port.length() == 0 || username.length() == 0
					|| password.length() == 0) {
				return FormValidation
						.error(Messages.DevTestPlugin_DescriptorImpl_MissingCustomRegistry());
			}
		}

		return FormValidation.ok();
	}

	/**
	 * Checks Registry endpoint values for empty or null values.
	 *
	 * @param buildstep build step that extend DefaultBuildStep
	 *
	 * @throws AbortException if some required fields are null
	 */
	public static void checkRegistryEndpoint(DefaultBuildStep buildstep) throws AbortException {
		if (buildstep.isUseCustomRegistry()) {
			if (!ObjectUtils.allNotNull(buildstep.getHost(), buildstep.getPort(),
					buildstep.getUsername(), buildstep.getPassword())) {
				throw new AbortException(Messages.Utils_EmptyRegistry());
			}
		} else {
			if (!checkDefaultConfigurationNotNull() || checkDefaultConfigurationIsEmpty()) {
				throw new AbortException(Messages.Utils_IncorrectRegistry());
			}
		}
	}


	/**
	 * Checks if there some fields in global configuration are not null.
	 *
	 * @return true if all fields are not null otherwise false
	 */
	private static boolean checkDefaultConfigurationNotNull() {
		return ObjectUtils.allNotNull(DevTestPluginConfiguration.get().getHost(),
				DevTestPluginConfiguration.get().getPort(),
				DevTestPluginConfiguration.get().getUsername(),
				DevTestPluginConfiguration.get().getPassword());
	}

	/**
	 * Checks if there some fields in global configuration are empty.
	 *
	 * @return true if some field is a empty string otherwise false
	 */
	private static boolean checkDefaultConfigurationIsEmpty() {
		return DevTestPluginConfiguration.get().getHost().isEmpty()
				|| DevTestPluginConfiguration.get().getPort().isEmpty()
				|| DevTestPluginConfiguration.get().getUsername().isEmpty()
				|| DevTestPluginConfiguration.get().getPassword().getPlainText().isEmpty();
	}

	/**
	 * Resolves Jenkins parameters from build based on job environment and returns them as List
	 * containing resolved parameters or original value. Pattern for parameters in Jenkins is either
	 * $parameter or ${parameter}.
	 *
	 * @param parameters list of parameters
	 * @param run        specific jenkins job run
	 * @param listener   listener of the same job run
	 *
	 * @return list containing resolved parameters
	 *
	 * @throws IOException          when there is a problem of getting build environment
	 * @throws InterruptedException interrupted exception
	 */
	public static List<String> resolveParameters(List<String> parameters, Run<?, ?> run,
			TaskListener listener) throws IOException, InterruptedException {
		EnvVars envVars = new EnvVars();
		if (run instanceof AbstractBuild) {
			final AbstractBuild build = (AbstractBuild) run;
			envVars = build.getEnvironment(listener);
		}

		List<String> resolvedParamaters = new ArrayList<>();
		final VariableResolver<String> resolver = new VariableResolver.ByMap<>(envVars);

		for (String parameter : parameters) {
			String resolvedParameter = Util.replaceMacro(envVars.expand(parameter), resolver);
			resolvedParamaters.addAll(Arrays
					.asList(resolvedParameter.split("\\s*\n\\s*")));
		}

		return resolvedParamaters;
	}

	/**
	 * Resolves single Jenkins parameter from build based on job environment.
	 * Pattern for parameters in Jenkins is either
	 * $parameter or ${parameter}.
	 *
	 * @param parameter parameter
	 * @param run       specific jenkins job run
	 * @param listener  listener of the same job run
	 *
	 * @return String resolved parameters
	 *
	 * @throws IOException          when there is a problem of getting build environment
	 * @throws InterruptedException interrupted exception
	 */
	public static String resolveParameter(String parameter, Run<?, ?> run,
			TaskListener listener) throws IOException, InterruptedException {
		EnvVars envVars = new EnvVars();
		if (run instanceof AbstractBuild) {
			final AbstractBuild build = (AbstractBuild) run;
			envVars = build.getEnvironment(listener);
		}

		final VariableResolver<String> resolver = new VariableResolver.ByMap<>(envVars);
		return Util.replaceMacro(envVars.expand(parameter), resolver);
	}

	/**
	 * Resolves jenkins credentials by id.
	 *
	 * @param credentialId id
	 *
	 * @return credentials object else if credentialId is null then it return null
	 */
	public static StandardUsernamePasswordCredentials lookupCredentials(@AncestorInPath Item context,
																		String credentialId) {
		if (credentialId == null) {
			return null;
		}
		List<StandardUsernamePasswordCredentials> credentials;
		if(context == null) {
			credentials = CredentialsProvider
					.lookupCredentials(StandardUsernamePasswordCredentials.class, Jenkins.getInstance(),
							ACL.SYSTEM, Collections.<DomainRequirement>emptyList());
		}else{
			credentials = CredentialsProvider
					.lookupCredentials(StandardUsernamePasswordCredentials.class, context,
							ACL.SYSTEM, Collections.<DomainRequirement>emptyList());
			CredentialsMatcher matcher = CredentialsMatchers.withId(credentialId);
		}
		CredentialsMatcher matcher = CredentialsMatchers.withId(credentialId);
		return CredentialsMatchers.firstOrNull(credentials, matcher);
	}

	/**
	 * Retrieves list of file paths matching the list of wildcards.
	 *
	 * @param marFilesWildcards wildcards
	 * @param workspace         project workspace path
	 *
	 * @return list of file paths matching list of wildcards
	 *
	 * @throws AbortException if there are no files matching wildcard
	 */
	public static List<String> getFilesMatchingWildcardList(List<String> marFilesWildcards,
			String workspace)
			throws AbortException {
		Set<String> result = new LinkedHashSet<>();
		for (String path : marFilesWildcards) {
			if (path.contains("*") || path.contains("?")) {
				result.addAll(Utils.getFilesMatchingWildcard(path, workspace));
			} else {
				result.add(path);
			}
		}
		return new ArrayList<>(result);
	}

	/**
	 * Retrieves list of file paths matching the wildcard.
	 *
	 * @param wildcard wildcard
	 * @param startDir search root folder path
	 *
	 * @return list of file paths
	 *
	 * @throws AbortException if there are no files matching wildcard
	 */
	public static List<String> getFilesMatchingWildcard(String wildcard, final String startDir)
			throws AbortException {
		if (wildcard == null || startDir == null) {
			throw new AbortException(Messages.Utils_NullWildcardOrDir(wildcard, startDir));
		}
		final AntPathMatcher pathMatcher = new AntPathMatcher();
		List<String> files = new ArrayList<>();
		FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) {
				if (file != null) {
					String matcher = startDir + wildcard;
					if ((!wildcard.startsWith("\\") && !wildcard.startsWith("/"))
							&&
							(!startDir.endsWith("\\") && !startDir.endsWith("/"))) {
						matcher = startDir + "/" + wildcard;
					}
					matcher = (matcher).replace('\\', '/');
					// sanitizing paths before matching (making matcher absolute)
					// pattern and a path must both be absolute or must both be relative
					// in order for the two to match in Linux
					if (!matcher.startsWith("/")) {
						if (matcher.length() >= 3) {
							if (!matcher.substring(1).startsWith(":/")) {
								matcher = "/" + matcher;
							}
						} else {
							matcher = "/" + matcher;
						}
					}
					String path = file.toAbsolutePath().toString().replace('\\', '/');
					if (pathMatcher.match(matcher, path)) {
						files.add(new File(startDir).toURI().relativize(new File(path).toURI()).getPath());
					}
				}
				return FileVisitResult.CONTINUE;
			}
		};
		try {
			Files.walkFileTree(Paths.get(startDir), matcherVisitor);
		} catch (Exception ex) {
			throw new AbortException(Messages.Utils_FolderNoAccess(startDir));
		}
		if (files.size() == 0) {
			throw new AbortException(Messages.Utils_NoFilesForWildcard(wildcard));
		}
		return files;
	}
}
