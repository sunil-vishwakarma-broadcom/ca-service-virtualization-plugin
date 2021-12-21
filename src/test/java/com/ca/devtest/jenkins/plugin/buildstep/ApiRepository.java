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

package com.ca.devtest.jenkins.plugin.buildstep;

import static com.ca.codesv.protocols.http.fluent.HttpFluentInterface.aMessage;
import static com.ca.codesv.protocols.http.fluent.HttpFluentInterface.contains;
import static com.ca.codesv.protocols.http.fluent.HttpFluentInterface.forDelete;
import static com.ca.codesv.protocols.http.fluent.HttpFluentInterface.forGet;
import static com.ca.codesv.protocols.http.fluent.HttpFluentInterface.forPost;
import static com.ca.codesv.protocols.http.fluent.HttpFluentInterface.notFoundMessage;
import static com.ca.codesv.protocols.http.fluent.HttpFluentInterface.okMessage;
import static com.ca.codesv.protocols.http.fluent.HttpFluentInterface.serverErrorMessage;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.CombinableMatcher.both;
import static org.hamcrest.core.CombinableMatcher.either;

import com.ca.codesv.sdk.annotation.TransactionDefinition;
import com.ca.codesv.sdk.annotation.VirtualServiceRepository;

/**
 * Class repository for virtualizing DevTest API for JUnit tests using CodeSV
 *
 * @author jakro01
 */
@VirtualServiceRepository(virtualServiceName = "Qa service")
public class ApiRepository {

	private static final String host;

	static {
		host = System.getProperty("testHost", "localhost") + ":1505";
	}

	/*
	Deploy VS virtualization
	 */
	@TransactionDefinition(name = "deployVs")
	public void virtualizedSuccessfulUploadVs() {
		forPost("http://" + host + "/api/Dcm/VSEs/VSE/actions/deployMar/")
				.matchesBasicAuthorization("admin", "admin")
				.matchesHeader("Accept", "application/vnd.ca.lisaInvoke.virtualService+json")
				.matchesBody(contains("fileURI"))
				.doReturn(
						aMessage(201)
								.withJsonBody("{\n"
										+ "    \"modelName\": \"webservices-vs\",\n"
										+ "    \"endTime\": \"1969-12-31 19:00:00\",\n"
										+ "    \"capacity\": 1,\n"
										+ "    \"upTime\": \"0\",\n"
										+ "    \"transactionsPerSecond\": 0,\n"
										+ "    \"transactionCount\": 0,\n"
										+ "    \"peakTransactionsPerSecond\": 0,\n"
										+ "    \"thinkScale\": 100,\n"
										+ "    \"errorCount\": 0,\n"
										+ "    \"autoRestartEnabled\": true,\n"
										+ "    \"status\": \"2\",\n"
										+ "    \"lastStartTime\": \"2018-04-18 09:17:00\",\n"
										+ "    \"executionMode\": \"Most Efficient\",\n"
										+ "    \"configName\": \"C:\\\\PROGRA~1\\\\CA\\\\DevTest\\\\lisatmp_10.3.0\\\\lads\\\\C6C47E37430A11E8B48A00505686642E\\\\examples\\\\Configs\\\\project.config\",\n"
										+ "    \"name\": \"webservices-vs\",\n"
										+ "    \"groupTag\": \"\",\n"
										+ "    \"resourceName\": \"8888 : http :  : /itkoExamples/EJB3UserControlBean\",\n"
										+ "    \"links\": [\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions\",\n"
										+ "            \"rel\": \"down\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions/start\",\n"
										+ "            \"rel\": \"start\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions/stop\",\n"
										+ "            \"rel\": \"stop\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions/resetCounts\",\n"
										+ "            \"rel\": \"resetCounts\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/sessions\",\n"
										+ "            \"rel\": \"sessions\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/events\",\n"
										+ "            \"rel\": \"events\"\n"
										+ "        }\n"
										+ "    ]\n"
										+ "}")
				);
	}


	@TransactionDefinition(name = "uploadVs")
	public void virtualizedSuccessfulDeployVsFromFileSystem() {
		forPost("http://" + host + "/api/Dcm/VSEs/VSE/actions/deployMar/")
				.matchesBasicAuthorization("admin", "admin")
				.matchesHeader("Accept", "application/vnd.ca.lisaInvoke.virtualService+json")
				.matchesBody(both(contains("file")).and(not(contains("fileURI"))))
				.doReturn(
						aMessage(201)
								.withJsonBody("{\n"
										+ "    \"modelName\": \"webservices-vs\",\n"
										+ "    \"endTime\": \"1969-12-31 19:00:00\",\n"
										+ "    \"capacity\": 1,\n"
										+ "    \"upTime\": \"0\",\n"
										+ "    \"transactionsPerSecond\": 0,\n"
										+ "    \"transactionCount\": 0,\n"
										+ "    \"peakTransactionsPerSecond\": 0,\n"
										+ "    \"thinkScale\": 100,\n"
										+ "    \"errorCount\": 0,\n"
										+ "    \"autoRestartEnabled\": true,\n"
										+ "    \"status\": \"2\",\n"
										+ "    \"lastStartTime\": \"2018-04-18 09:17:00\",\n"
										+ "    \"executionMode\": \"Most Efficient\",\n"
										+ "    \"configName\": \"C:\\\\PROGRA~1\\\\CA\\\\DevTest\\\\lisatmp_10.3.0\\\\lads\\\\C6C47E37430A11E8B48A00505686642E\\\\examples\\\\Configs\\\\project.config\",\n"
										+ "    \"name\": \"webservices-vs\",\n"
										+ "    \"groupTag\": \"\",\n"
										+ "    \"resourceName\": \"8888 : http :  : /itkoExamples/EJB3UserControlBean\",\n"
										+ "    \"links\": [\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions\",\n"
										+ "            \"rel\": \"down\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions/start\",\n"
										+ "            \"rel\": \"start\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions/stop\",\n"
										+ "            \"rel\": \"stop\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions/resetCounts\",\n"
										+ "            \"rel\": \"resetCounts\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/sessions\",\n"
										+ "            \"rel\": \"sessions\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/events\",\n"
										+ "            \"rel\": \"events\"\n"
										+ "        }\n"
										+ "    ]\n"
										+ "}")
				);
	}

	@TransactionDefinition(name = "deployVsFileFileNotFound")
	public void virtualizedMarFileDoesntExist() {
		forPost("http://" + host + "/api/Dcm/VSEs/VSE/actions/deployMar/")
				.matchesBasicAuthorization("admin", "admin")
				.matchesHeader("Accept", "application/vnd.ca.lisaInvoke.virtualService+json")
				.matchesBody(contains("fileURI"))
				.matchesBody(contains("C:/doesntExist.mar"))
				.doReturn(
						serverErrorMessage()
								.withJsonBody("{\n"
										+ "    \"id\": 1003,\n"
										+ "    \"message\": \"Can't open file:///C:/doesntExist.mar please check again.\",\n"
										+ "    \"addInfo\": \"\\r\\ncom.itko.lisa.invoke.api.exception.BadRequestException: Can't open file:///C:/webservices-vs2.mar please check again.\\r\\n\\tat com.itko.lisa.invoke.ParamterUtils.getInputStream(ParamterUtils.java:90)\\r\\n\\tat com.itko.lisa.invoke.resource.VseResource.deployService(VseResource.java:1034)\\r\\n\\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\\r\\n\\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\\r\\n\\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\\r\\n\\tat java.lang.reflect.Method.invoke(Method.java:498)\\r\\n\\tat com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)\\r\\n\\tat com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)\\r\\n\\tat com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)\\r\\n\\tat com.codahale.metrics.jersey.InstrumentedResourceMethodDispatchProvider$TimedRequestDispatcher.dispatch(InstrumentedResourceMethodDispatchProvider.java:30)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1511)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1442)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1391)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1381)\\r\\n\\tat com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:416)\\r\\n\\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:538)\\r\\n\\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:716)\\r\\n\\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:668)\\r\\n\\tat com.google.inject.servlet.ServletDefinition.doService(ServletDefinition.java:263)\\r\\n\\tat com.google.inject.servlet.ServletDefinition.service(ServletDefinition.java:178)\\r\\n\\tat com.google.inject.servlet.ManagedServletPipeline.service(ManagedServletPipeline.java:91)\\r\\n\\tat com.google.inject.servlet.FilterChainInvocation.doFilter(FilterChainInvocation.java:62)\\r\\n\\tat com.google.inject.servlet.ManagedFilterPipeline.dispatch(ManagedFilterPipeline.java:118)\\r\\n\\tat com.google.inject.servlet.GuiceFilter.doFilter(GuiceFilter.java:113)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1676)\\r\\n\\tat com.itko.lisa.invoke.AuthenFilter.doFilter(AuthenFilter.java:251)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1668)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:581)\\r\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:143)\\r\\n\\tat org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:548)\\r\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:226)\\r\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1180)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:511)\\r\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:185)\\r\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1112)\\r\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141)\\r\\n\\tat org.eclipse.jetty.server.handler.ContextHandlerCollection.handle(ContextHandlerCollection.java:213)\\r\\n\\tat org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:134)\\r\\n\\tat org.eclipse.jetty.server.Server.handle(Server.java:524)\\r\\n\\tat org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:319)\\r\\n\\tat org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:253)\\r\\n\\tat org.eclipse.jetty.io.AbstractConnection$ReadCallback.succeeded(AbstractConnection.java:273)\\r\\n\\tat org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:95)\\r\\n\\tat org.eclipse.jetty.io.SelectChannelEndPoint$2.run(SelectChannelEndPoint.java:93)\\r\\n\\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.executeProduceConsume(ExecuteProduceConsume.java:303)\\r\\n\\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.produceConsume(ExecuteProduceConsume.java:148)\\r\\n\\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.run(ExecuteProduceConsume.java:136)\\r\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:671)\\r\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool$2.run(QueuedThreadPool.java:589)\\r\\n\\tat java.lang.Thread.run(Thread.java:745)\\r\\n\\r\\n\"\n"
										+ "}")
				);
	}

	@TransactionDefinition(name = "deployVsHttpFileNotFound")
	public void virtualizedHttpMarFileDoesntExist() {
		forPost("http://" + host + "/api/Dcm/VSEs/VSE/actions/deployMar/")
				.matchesBasicAuthorization("admin", "admin")
				.matchesHeader("Accept", "application/vnd.ca.lisaInvoke.virtualService+json")
				.matchesBody(contains("fileURI"))
				.matchesBody(contains("http://test.com/webservices-vs.mar"))
				.doReturn(
						serverErrorMessage()
								.withJsonBody("{\n"
										+ "    \"id\": 1003,\n"
										+ "    \"message\": \"Our audit file is either missing or we are not a model archive or are corrupt.\",\n"
										+ "    \"addInfo\": \"\\r\\njava.lang.IllegalArgumentException: Our audit file is either missing or we are not a model archive or are corrupt.\\r\\n\\tat com.itko.lisa.model.mar.ModelArchive.init(ModelArchive.java:259)\\r\\n\\tat com.itko.lisa.model.mar.ModelArchive.<init>(ModelArchive.java:243)\\r\\n\\tat com.itko.lisa.invoke.internal.VseRetriever.deployService(VseRetriever.java:1163)\\r\\n\\tat com.itko.lisa.invoke.resource.VseResource.deployService(VseResource.java:1033)\\r\\n\\tat sun.reflect.GeneratedMethodAccessor691.invoke(Unknown Source)\\r\\n\\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\\r\\n\\tat java.lang.reflect.Method.invoke(Method.java:498)\\r\\n\\tat com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)\\r\\n\\tat com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)\\r\\n\\tat com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)\\r\\n\\tat com.codahale.metrics.jersey.InstrumentedResourceMethodDispatchProvider$TimedRequestDispatcher.dispatch(InstrumentedResourceMethodDispatchProvider.java:30)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1511)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1442)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1391)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1381)\\r\\n\\tat com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:416)\\r\\n\\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:538)\\r\\n\\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:716)\\r\\n\\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:668)\\r\\n\\tat com.google.inject.servlet.ServletDefinition.doService(ServletDefinition.java:263)\\r\\n\\tat com.google.inject.servlet.ServletDefinition.service(ServletDefinition.java:178)\\r\\n\\tat com.google.inject.servlet.ManagedServletPipeline.service(ManagedServletPipeline.java:91)\\r\\n\\tat com.google.inject.servlet.FilterChainInvocation.doFilter(FilterChainInvocation.java:62)\\r\\n\\tat com.google.inject.servlet.ManagedFilterPipeline.dispatch(ManagedFilterPipeline.java:118)\\r\\n\\tat com.google.inject.servlet.GuiceFilter.doFilter(GuiceFilter.java:113)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1676)\\r\\n\\tat com.itko.lisa.invoke.AuthenFilter.doFilter(AuthenFilter.java:251)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1668)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:581)\\r\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:143)\\r\\n\\tat org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:548)\\r\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:226)\\r\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1180)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:511)\\r\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:185)\\r\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1112)\\r\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141)\\r\\n\\tat org.eclipse.jetty.server.handler.ContextHandlerCollection.handle(ContextHandlerCollection.java:213)\\r\\n\\tat org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:134)\\r\\n\\tat org.eclipse.jetty.server.Server.handle(Server.java:524)\\r\\n\\tat org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:319)\\r\\n\\tat org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:253)\\r\\n\\tat org.eclipse.jetty.io.AbstractConnection$ReadCallback.succeeded(AbstractConnection.java:273)\\r\\n\\tat org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:95)\\r\\n\\tat org.eclipse.jetty.io.SelectChannelEndPoint$2.run(SelectChannelEndPoint.java:93)\\r\\n\\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.executeProduceConsume(ExecuteProduceConsume.java:303)\\r\\n\\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.produceConsume(ExecuteProduceConsume.java:148)\\r\\n\\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.run(ExecuteProduceConsume.java:136)\\r\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:671)\\r\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool$2.run(QueuedThreadPool.java:589)\\r\\n\\tat java.lang.Thread.run(Thread.java:745)\\r\\n\\r\\n\"\n"
										+ "}")
				);
	}

	@TransactionDefinition(name = "deployVsAndVseDoesntExist")
	public void virtualizedDeployAndVseDoesntExist() {
		forPost("http://" + host + "/api/Dcm/VSEs/vse-doesnt-exist/actions/deployMar/")
				.matchesBasicAuthorization("admin", "admin")
				.matchesHeader("Accept", "application/vnd.ca.lisaInvoke.virtualService+json")
				.matchesBody(contains("fileURI"))
				.matchesBody(contains("C:/webservices-vs.mar"))
				.doReturn(
						notFoundMessage()
								.withJsonBody("{\n"
										+ "    \"id\": 1709,\n"
										+ "    \"message\": \"vse-doesnt-exist not exist\",\n"
										+ "    \"addInfo\": \"\\r\\ncom.itko.lisa.invoke.api.exception.NotFoundException: vse-doesnt-exist not exist\\r\\n\\tat com.itko.lisa.invoke.resource.ThrowException.throwException(ThrowException.java:21)\\r\\n\\tat com.itko.lisa.invoke.resource.VseResource.deployService(VseResource.java:1045)\\r\\n\\tat sun.reflect.GeneratedMethodAccessor691.invoke(Unknown Source)\\r\\n\\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\\r\\n\\tat java.lang.reflect.Method.invoke(Method.java:498)\\r\\n\\tat com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)\\r\\n\\tat com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)\\r\\n\\tat com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)\\r\\n\\tat com.codahale.metrics.jersey.InstrumentedResourceMethodDispatchProvider$TimedRequestDispatcher.dispatch(InstrumentedResourceMethodDispatchProvider.java:30)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1511)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1442)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1391)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1381)\\r\\n\\tat com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:416)\\r\\n\\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:538)\\r\\n\\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:716)\\r\\n\\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:668)\\r\\n\\tat com.google.inject.servlet.ServletDefinition.doService(ServletDefinition.java:263)\\r\\n\\tat com.google.inject.servlet.ServletDefinition.service(ServletDefinition.java:178)\\r\\n\\tat com.google.inject.servlet.ManagedServletPipeline.service(ManagedServletPipeline.java:91)\\r\\n\\tat com.google.inject.servlet.FilterChainInvocation.doFilter(FilterChainInvocation.java:62)\\r\\n\\tat com.google.inject.servlet.ManagedFilterPipeline.dispatch(ManagedFilterPipeline.java:118)\\r\\n\\tat com.google.inject.servlet.GuiceFilter.doFilter(GuiceFilter.java:113)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1676)\\r\\n\\tat com.itko.lisa.invoke.AuthenFilter.doFilter(AuthenFilter.java:251)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1668)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:581)\\r\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:143)\\r\\n\\tat org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:548)\\r\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:226)\\r\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1180)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:511)\\r\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:185)\\r\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1112)\\r\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141)\\r\\n\\tat org.eclipse.jetty.server.handler.ContextHandlerCollection.handle(ContextHandlerCollection.java:213)\\r\\n\\tat org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:134)\\r\\n\\tat org.eclipse.jetty.server.Server.handle(Server.java:524)\\r\\n\\tat org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:319)\\r\\n\\tat org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:253)\\r\\n\\tat org.eclipse.jetty.io.AbstractConnection$ReadCallback.succeeded(AbstractConnection.java:273)\\r\\n\\tat org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:95)\\r\\n\\tat org.eclipse.jetty.io.SelectChannelEndPoint$2.run(SelectChannelEndPoint.java:93)\\r\\n\\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.executeProduceConsume(ExecuteProduceConsume.java:303)\\r\\n\\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.produceConsume(ExecuteProduceConsume.java:148)\\r\\n\\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.run(ExecuteProduceConsume.java:136)\\r\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:671)\\r\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool$2.run(QueuedThreadPool.java:589)\\r\\n\\tat java.lang.Thread.run(Thread.java:745)\\r\\n\\r\\n\"\n"
										+ "}")
				);
	}

	/*
	Undeploy VS virtualization
	 */

	@TransactionDefinition(name = "undeployVs")
	public void virtualizedSuccessfulUndeployVs() {
		forDelete("http://" + host + "/api/Dcm/VSEs/VSE/{vsName}/")
				.matchesBasicAuthorization("admin", "admin")
				.doReturn(
						aMessage(204)
				);
	}

	@TransactionDefinition(name = "undeployVsAndVsDoesntExist")
	public void virtualizedUndeployAndVsDoesntExist() {
		forDelete("http://" + host + "/api/Dcm/VSEs/VSE/vs-doesnt-exist/")
				.matchesBasicAuthorization("admin", "admin")
				.doReturn(
						notFoundMessage()
								.withXmlBody("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
										+ "<Error xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.ca.com/lisa/invoke/v2.0 Error.xsd\" type=\"application/vnd.ca.lisaInvoke.error+xml\" xmlns=\"http://www.ca.com/lisa/invoke/v2.0\">\n"
										+ "    <Id>1702</Id>\n"
										+ "    <Message>Virtual service name is invalid. Virtual service name:vs-doesnt-exist</Message>\n"
										+ "    <AdditionalInformation>\n"
										+ "com.itko.lisa.invoke.api.exception.NotFoundException: Virtual service name is invalid. Virtual service name:vs-doesnt-exist\n"
										+ "\tat com.itko.lisa.invoke.resource.ThrowException.throwException(ThrowException.java:21)\n"
										+ "\tat com.itko.lisa.invoke.resource.VseResource.deleteService(VseResource.java:429)\n"
										+ "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
										+ "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n"
										+ "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n"
										+ "\tat java.lang.reflect.Method.invoke(Method.java:498)\n"
										+ "\tat com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)\n"
										+ "\tat com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)\n"
										+ "\tat com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)\n"
										+ "\tat com.codahale.metrics.jersey.InstrumentedResourceMethodDispatchProvider$TimedRequestDispatcher.dispatch(InstrumentedResourceMethodDispatchProvider.java:30)\n"
										+ "\tat com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)\n"
										+ "\tat com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)\n"
										+ "\tat com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)\n"
										+ "\tat com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)\n"
										+ "\tat com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)\n"
										+ "\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1511)\n"
										+ "\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1442)\n"
										+ "\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1391)\n"
										+ "\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1381)\n"
										+ "\tat com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:416)\n"
										+ "\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:538)\n"
										+ "\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:716)\n"
										+ "\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:668)\n"
										+ "\tat com.google.inject.servlet.ServletDefinition.doService(ServletDefinition.java:263)\n"
										+ "\tat com.google.inject.servlet.ServletDefinition.service(ServletDefinition.java:178)\n"
										+ "\tat com.google.inject.servlet.ManagedServletPipeline.service(ManagedServletPipeline.java:91)\n"
										+ "\tat com.google.inject.servlet.FilterChainInvocation.doFilter(FilterChainInvocation.java:62)\n"
										+ "\tat com.google.inject.servlet.ManagedFilterPipeline.dispatch(ManagedFilterPipeline.java:118)\n"
										+ "\tat com.google.inject.servlet.GuiceFilter.doFilter(GuiceFilter.java:113)\n"
										+ "\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1676)\n"
										+ "\tat com.itko.lisa.invoke.AuthenFilter.doFilter(AuthenFilter.java:251)\n"
										+ "\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1668)\n"
										+ "\tat org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:581)\n"
										+ "\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:143)\n"
										+ "\tat org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:548)\n"
										+ "\tat org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:226)\n"
										+ "\tat org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1180)\n"
										+ "\tat org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:511)\n"
										+ "\tat org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:185)\n"
										+ "\tat org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1112)\n"
										+ "\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141)\n"
										+ "\tat org.eclipse.jetty.server.handler.ContextHandlerCollection.handle(ContextHandlerCollection.java:213)\n"
										+ "\tat org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:134)\n"
										+ "\tat org.eclipse.jetty.server.Server.handle(Server.java:524)\n"
										+ "\tat org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:319)\n"
										+ "\tat org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:253)\n"
										+ "\tat org.eclipse.jetty.io.AbstractConnection$ReadCallback.succeeded(AbstractConnection.java:273)\n"
										+ "\tat org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:95)\n"
										+ "\tat org.eclipse.jetty.io.SelectChannelEndPoint$2.run(SelectChannelEndPoint.java:93)\n"
										+ "\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.executeProduceConsume(ExecuteProduceConsume.java:303)\n"
										+ "\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.produceConsume(ExecuteProduceConsume.java:148)\n"
										+ "\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.run(ExecuteProduceConsume.java:136)\n"
										+ "\tat org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:671)\n"
										+ "\tat org.eclipse.jetty.util.thread.QueuedThreadPool$2.run(QueuedThreadPool.java:589)\n"
										+ "\tat java.lang.Thread.run(Thread.java:745)\n"
										+ "\n"
										+ "</AdditionalInformation>\n"
										+ "</Error>")
				);
	}

	@TransactionDefinition(name = "undeployVsAndVseDoesntExist")
	public void virtualizedUndeployAndVseDoesntExist() {
		forDelete("http://" + host + "/api/Dcm/VSEs/vse-doesnt-exist/webservices-vs/")
				.matchesBasicAuthorization("admin", "admin")
				.doReturn(
						aMessage(404)
								.withXmlBody("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
										+ "<Error xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.ca.com/lisa/invoke/v2.0 Error.xsd\" type=\"application/vnd.ca.lisaInvoke.error+xml\" xmlns=\"http://www.ca.com/lisa/invoke/v2.0\">\n"
										+ "    <Id>1709</Id>\n"
										+ "    <Message>Vse name is invalid. vse name:vse-doesnt-exist</Message>\n"
										+ "    <AdditionalInformation>\n"
										+ "com.itko.lisa.invoke.api.exception.NotFoundException: Vse name is invalid. vse name:vse-doesnt-exist\n"
										+ "\tat com.itko.lisa.invoke.resource.ThrowException.throwException(ThrowException.java:21)\n"
										+ "\tat com.itko.lisa.invoke.resource.VseResource.deleteService(VseResource.java:429)\n"
										+ "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
										+ "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n"
										+ "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n"
										+ "\tat java.lang.reflect.Method.invoke(Method.java:498)\n"
										+ "\tat com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)\n"
										+ "\tat com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)\n"
										+ "\tat com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)\n"
										+ "\tat com.codahale.metrics.jersey.InstrumentedResourceMethodDispatchProvider$TimedRequestDispatcher.dispatch(InstrumentedResourceMethodDispatchProvider.java:30)\n"
										+ "\tat com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)\n"
										+ "\tat com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)\n"
										+ "\tat com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)\n"
										+ "\tat com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)\n"
										+ "\tat com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)\n"
										+ "\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1511)\n"
										+ "\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1442)\n"
										+ "\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1391)\n"
										+ "\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1381)\n"
										+ "\tat com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:416)\n"
										+ "\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:538)\n"
										+ "\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:716)\n"
										+ "\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:668)\n"
										+ "\tat com.google.inject.servlet.ServletDefinition.doService(ServletDefinition.java:263)\n"
										+ "\tat com.google.inject.servlet.ServletDefinition.service(ServletDefinition.java:178)\n"
										+ "\tat com.google.inject.servlet.ManagedServletPipeline.service(ManagedServletPipeline.java:91)\n"
										+ "\tat com.google.inject.servlet.FilterChainInvocation.doFilter(FilterChainInvocation.java:62)\n"
										+ "\tat com.google.inject.servlet.ManagedFilterPipeline.dispatch(ManagedFilterPipeline.java:118)\n"
										+ "\tat com.google.inject.servlet.GuiceFilter.doFilter(GuiceFilter.java:113)\n"
										+ "\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1676)\n"
										+ "\tat com.itko.lisa.invoke.AuthenFilter.doFilter(AuthenFilter.java:251)\n"
										+ "\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1668)\n"
										+ "\tat org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:581)\n"
										+ "\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:143)\n"
										+ "\tat org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:548)\n"
										+ "\tat org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:226)\n"
										+ "\tat org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1180)\n"
										+ "\tat org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:511)\n"
										+ "\tat org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:185)\n"
										+ "\tat org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1112)\n"
										+ "\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141)\n"
										+ "\tat org.eclipse.jetty.server.handler.ContextHandlerCollection.handle(ContextHandlerCollection.java:213)\n"
										+ "\tat org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:134)\n"
										+ "\tat org.eclipse.jetty.server.Server.handle(Server.java:524)\n"
										+ "\tat org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:319)\n"
										+ "\tat org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:253)\n"
										+ "\tat org.eclipse.jetty.io.AbstractConnection$ReadCallback.succeeded(AbstractConnection.java:273)\n"
										+ "\tat org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:95)\n"
										+ "\tat org.eclipse.jetty.io.SelectChannelEndPoint$2.run(SelectChannelEndPoint.java:93)\n"
										+ "\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.executeProduceConsume(ExecuteProduceConsume.java:303)\n"
										+ "\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.produceConsume(ExecuteProduceConsume.java:148)\n"
										+ "\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.run(ExecuteProduceConsume.java:136)\n"
										+ "\tat org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:671)\n"
										+ "\tat org.eclipse.jetty.util.thread.QueuedThreadPool$2.run(QueuedThreadPool.java:589)\n"
										+ "\tat java.lang.Thread.run(Thread.java:745)\n"
										+ "\n"
										+ "</AdditionalInformation>\n"
										+ "</Error>")
				);
	}

	/*
	Start VS virtualization
	 */

	@TransactionDefinition(name = "startVs")
	public void virtualizedSuccessfulStartVs() {
		forPost("http://" + host + "/api/Dcm/VSEs/VSE/{vsName}/actions/start/")
				.matchesBasicAuthorization("admin", "admin")
				.matchesHeader("Accept", "application/vnd.ca.lisaInvoke.virtualService+json")
				.doReturn(
						okMessage()
								.withJsonBody("{\n"
										+ "    \"modelName\": \"${attribute.vsName}\",\n"
										+ "    \"endTime\": \"1969-12-31 19:00:00\",\n"
										+ "    \"capacity\": 1,\n"
										+ "    \"upTime\": \"0\",\n"
										+ "    \"transactionsPerSecond\": 0,\n"
										+ "    \"transactionCount\": 0,\n"
										+ "    \"peakTransactionsPerSecond\": 0,\n"
										+ "    \"thinkScale\": 100,\n"
										+ "    \"errorCount\": 0,\n"
										+ "    \"autoRestartEnabled\": true,\n"
										+ "    \"status\": \"2\",\n"
										+ "    \"lastStartTime\": \"2018-04-20 03:22:35\",\n"
										+ "    \"executionMode\": \"Most Efficient\",\n"
										+ "    \"configName\": \"C:\\\\PROGRA~1\\\\CA\\\\DevTest\\\\lisatmp_10.3.0\\\\lads\\\\9935A89F446B11E8B48A00505686642E\\\\examples\\\\Configs\\\\project.config\",\n"
										+ "    \"name\": \"${attribute.vsName}\",\n"
										+ "    \"groupTag\": \"\",\n"
										+ "    \"resourceName\": \"8888 : http :  : /itkoExamples/EJB3UserControlBean\",\n"
										+ "    \"links\": [\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions\",\n"
										+ "            \"rel\": \"down\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions/start\",\n"
										+ "            \"rel\": \"start\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions/stop\",\n"
										+ "            \"rel\": \"stop\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions/resetCounts\",\n"
										+ "            \"rel\": \"resetCounts\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/sessions\",\n"
										+ "            \"rel\": \"sessions\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/events\",\n"
										+ "            \"rel\": \"events\"\n"
										+ "        }\n"
										+ "    ]\n"
										+ "}")
				);
	}

	@TransactionDefinition(name = "startVsAndVseDoesntExist")
	public void virtualizedStartAndVseDoesntExist() {
		forPost("http://" + host + "/api/Dcm/VSEs/vse-doesnt-exist/webservices-vs/actions/start/")
				.matchesBasicAuthorization("admin", "admin")
				.matchesHeader("Accept", "application/vnd.ca.lisaInvoke.virtualService+json")
				.doReturn(
						notFoundMessage()
								.withJsonBody("{\n"
										+ "    \"id\": 1709,\n"
										+ "    \"message\": \"VSE name is invalid, either you type a wrong name, or the VSE is not started. VSE name:vse-doesnt-exist\",\n"
										+ "    \"addInfo\": \"\\r\\ncom.itko.lisa.invoke.api.exception.NotFoundException: VSE name is invalid, either you type a wrong name, or the VSE is not started. VSE name:vse-doesnt-exist\\r\\n\\tat com.itko.lisa.invoke.resource.ThrowException.throwException(ThrowException.java:21)\\r\\n\\tat com.itko.lisa.invoke.resource.VseResource.startVirtualService(VseResource.java:792)\\r\\n\\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\\r\\n\\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\\r\\n\\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\\r\\n\\tat java.lang.reflect.Method.invoke(Method.java:498)\\r\\n\\tat com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)\\r\\n\\tat com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)\\r\\n\\tat com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)\\r\\n\\tat com.codahale.metrics.jersey.InstrumentedResourceMethodDispatchProvider$TimedRequestDispatcher.dispatch(InstrumentedResourceMethodDispatchProvider.java:30)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1511)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1442)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1391)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1381)\\r\\n\\tat com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:416)\\r\\n\\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:538)\\r\\n\\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:716)\\r\\n\\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:668)\\r\\n\\tat com.google.inject.servlet.ServletDefinition.doService(ServletDefinition.java:263)\\r\\n\\tat com.google.inject.servlet.ServletDefinition.service(ServletDefinition.java:178)\\r\\n\\tat com.google.inject.servlet.ManagedServletPipeline.service(ManagedServletPipeline.java:91)\\r\\n\\tat com.google.inject.servlet.FilterChainInvocation.doFilter(FilterChainInvocation.java:62)\\r\\n\\tat com.google.inject.servlet.ManagedFilterPipeline.dispatch(ManagedFilterPipeline.java:118)\\r\\n\\tat com.google.inject.servlet.GuiceFilter.doFilter(GuiceFilter.java:113)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1676)\\r\\n\\tat com.itko.lisa.invoke.AuthenFilter.doFilter(AuthenFilter.java:251)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1668)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:581)\\r\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:143)\\r\\n\\tat org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:548)\\r\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:226)\\r\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1180)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:511)\\r\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:185)\\r\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1112)\\r\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141)\\r\\n\\tat org.eclipse.jetty.server.handler.ContextHandlerCollection.handle(ContextHandlerCollection.java:213)\\r\\n\\tat org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:134)\\r\\n\\tat org.eclipse.jetty.server.Server.handle(Server.java:524)\\r\\n\\tat org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:319)\\r\\n\\tat org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:253)\\r\\n\\tat org.eclipse.jetty.io.AbstractConnection$ReadCallback.succeeded(AbstractConnection.java:273)\\r\\n\\tat org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:95)\\r\\n\\tat org.eclipse.jetty.io.SelectChannelEndPoint$2.run(SelectChannelEndPoint.java:93)\\r\\n\\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.executeProduceConsume(ExecuteProduceConsume.java:303)\\r\\n\\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.produceConsume(ExecuteProduceConsume.java:148)\\r\\n\\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.run(ExecuteProduceConsume.java:136)\\r\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:671)\\r\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool$2.run(QueuedThreadPool.java:589)\\r\\n\\tat java.lang.Thread.run(Thread.java:745)\\r\\n\\r\\n\"\n"
										+ "}")
				);
	}

	@TransactionDefinition(name = "startVsAndVsDoesntExist")
	public void virtualizedStartAndVsDoesntExist() {
		forPost("http://" + host + "/api/Dcm/VSEs/VSE/vs-doesnt-exist/actions/start/")
				.matchesBasicAuthorization("admin", "admin")
				.matchesHeader("Accept", "application/vnd.ca.lisaInvoke.virtualService+json")
				.doReturn(
						serverErrorMessage()
								.withJsonBody("{\n"
										+ "    \"id\": 1007,\n"
										+ "    \"message\": \"No such virtual service: vs-doesnt-exist\",\n"
										+ "    \"addInfo\": \"\\r\\njava.lang.IllegalArgumentException: No such virtual service: vs-doesnt-exist\\r\\n\\tat com.itko.lisa.coordinator.VirtualServiceEnvironmentImpl.startTheService(VirtualServiceEnvironmentImpl.java:1444)\\r\\n\\tat com.itko.lisa.coordinator.VirtualServiceEnvironmentImpl.startService(VirtualServiceEnvironmentImpl.java:1429)\\r\\n\\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\\r\\n\\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\\r\\n\\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\\r\\n\\tat java.lang.reflect.Method.invoke(Method.java:498)\\r\\n\\tat com.itko.lisa.net.RemoteMethodExec.execute(RemoteMethodExec.java:57)\\r\\n\\tat com.itko.lisa.net.ServerRequestHandler.processRMIMessage(ServerRequestHandler.java:533)\\r\\n\\tat com.itko.lisa.net.ServerRequestHandler.access$600(ServerRequestHandler.java:70)\\r\\n\\tat com.itko.lisa.net.ServerRequestHandler$5.run(ServerRequestHandler.java:451)\\r\\n\\tat java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)\\r\\n\\tat java.util.concurrent.FutureTask.run(FutureTask.java:266)\\r\\n\\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)\\r\\n\\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)\\r\\n\\tat java.lang.Thread.run(Thread.java:745)\\r\\n\\r\\n\"\n"
										+ "}")
				);
	}


	/*
	Stop VS virtualization
	 */

	@TransactionDefinition(name = "stopVs")
	public void virtualizedSuccessfulStopVs() {
		forPost("http://" + host + "/api/Dcm/VSEs/VSE/{vsName}/actions/stop/")
				.matchesBasicAuthorization("admin", "admin")
				.matchesHeader("Accept", "application/vnd.ca.lisaInvoke.virtualService+json")
				.doReturn(
						okMessage()
								.withJsonBody("{\n"
										+ "    \"modelName\": \"${attribute.vsName}\",\n"
										+ "    \"endTime\": \"1969-12-31 19:00:00\",\n"
										+ "    \"capacity\": 1,\n"
										+ "    \"upTime\": \"0:22:34\",\n"
										+ "    \"transactionsPerSecond\": 0,\n"
										+ "    \"transactionCount\": 0,\n"
										+ "    \"peakTransactionsPerSecond\": 0,\n"
										+ "    \"thinkScale\": 100,\n"
										+ "    \"errorCount\": 0,\n"
										+ "    \"autoRestartEnabled\": true,\n"
										+ "    \"status\": \"3\",\n"
										+ "    \"lastStartTime\": \"2018-04-20 03:22:35\",\n"
										+ "    \"executionMode\": \"Most Efficient\",\n"
										+ "    \"configName\": \"C:\\\\PROGRA~1\\\\CA\\\\DevTest\\\\lisatmp_10.3.0\\\\lads\\\\9935A89F446B11E8B48A00505686642E\\\\examples\\\\Configs\\\\project.config\",\n"
										+ "    \"name\": \"${attribute.vsName}\",\n"
										+ "    \"groupTag\": \"\",\n"
										+ "    \"resourceName\": \"8888 : http :  : /itkoExamples/EJB3UserControlBean\",\n"
										+ "    \"links\": [\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions\",\n"
										+ "            \"rel\": \"down\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions/start\",\n"
										+ "            \"rel\": \"start\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions/stop\",\n"
										+ "            \"rel\": \"stop\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/actions/resetCounts\",\n"
										+ "            \"rel\": \"resetCounts\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/sessions\",\n"
										+ "            \"rel\": \"sessions\"\n"
										+ "        },\n"
										+ "        {\n"
										+ "            \"href\": \"http://" + host
										+ "/api/Dcm/VSEs/VSE/webservices-vs/events\",\n"
										+ "            \"rel\": \"events\"\n"
										+ "        }\n"
										+ "    ]\n"
										+ "}")
				);
	}

	@TransactionDefinition(name = "stopVsAndVseDoesntExist")
	public void virtualizedStopAndVseDoesntExist() {
		forPost("http://" + host + "/api/Dcm/VSEs/vse-doesnt-exist/webservices-vs/actions/stop/")
				.matchesBasicAuthorization("admin", "admin")
				.matchesHeader("Accept", "application/vnd.ca.lisaInvoke.virtualService+json")
				.doReturn(
						notFoundMessage()
								.withJsonBody("{\n"
										+ "    \"id\": 1709,\n"
										+ "    \"message\": \"VSE name is invalid, either you type a wrong name, or the VSE is not started. VSE name:vse-doesnt-exist\",\n"
										+ "    \"addInfo\": \"\\r\\ncom.itko.lisa.invoke.api.exception.NotFoundException: VSE name is invalid, either you type a wrong name, or the VSE is not started. VSE name:vse-doesnt-exist\\r\\n\\tat com.itko.lisa.invoke.resource.ThrowException.throwException(ThrowException.java:21)\\r\\n\\tat com.itko.lisa.invoke.resource.VseResource.stopVirtualService(VseResource.java:895)\\r\\n\\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\\r\\n\\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\\r\\n\\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\\r\\n\\tat java.lang.reflect.Method.invoke(Method.java:498)\\r\\n\\tat com.sun.jersey.spi.container.JavaMethodInvokerFactory$1.invoke(JavaMethodInvokerFactory.java:60)\\r\\n\\tat com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$ResponseOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:205)\\r\\n\\tat com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:75)\\r\\n\\tat com.codahale.metrics.jersey.InstrumentedResourceMethodDispatchProvider$TimedRequestDispatcher.dispatch(InstrumentedResourceMethodDispatchProvider.java:30)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:302)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)\\r\\n\\tat com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1511)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1442)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1391)\\r\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1381)\\r\\n\\tat com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:416)\\r\\n\\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:538)\\r\\n\\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:716)\\r\\n\\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:668)\\r\\n\\tat com.google.inject.servlet.ServletDefinition.doService(ServletDefinition.java:263)\\r\\n\\tat com.google.inject.servlet.ServletDefinition.service(ServletDefinition.java:178)\\r\\n\\tat com.google.inject.servlet.ManagedServletPipeline.service(ManagedServletPipeline.java:91)\\r\\n\\tat com.google.inject.servlet.FilterChainInvocation.doFilter(FilterChainInvocation.java:62)\\r\\n\\tat com.google.inject.servlet.ManagedFilterPipeline.dispatch(ManagedFilterPipeline.java:118)\\r\\n\\tat com.google.inject.servlet.GuiceFilter.doFilter(GuiceFilter.java:113)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1676)\\r\\n\\tat com.itko.lisa.invoke.AuthenFilter.doFilter(AuthenFilter.java:251)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1668)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:581)\\r\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:143)\\r\\n\\tat org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:548)\\r\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:226)\\r\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1180)\\r\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:511)\\r\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:185)\\r\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1112)\\r\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141)\\r\\n\\tat org.eclipse.jetty.server.handler.ContextHandlerCollection.handle(ContextHandlerCollection.java:213)\\r\\n\\tat org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:134)\\r\\n\\tat org.eclipse.jetty.server.Server.handle(Server.java:524)\\r\\n\\tat org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:319)\\r\\n\\tat org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:253)\\r\\n\\tat org.eclipse.jetty.io.AbstractConnection$ReadCallback.succeeded(AbstractConnection.java:273)\\r\\n\\tat org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:95)\\r\\n\\tat org.eclipse.jetty.io.SelectChannelEndPoint$2.run(SelectChannelEndPoint.java:93)\\r\\n\\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.executeProduceConsume(ExecuteProduceConsume.java:303)\\r\\n\\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.produceConsume(ExecuteProduceConsume.java:148)\\r\\n\\tat org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.run(ExecuteProduceConsume.java:136)\\r\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:671)\\r\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool$2.run(QueuedThreadPool.java:589)\\r\\n\\tat java.lang.Thread.run(Thread.java:745)\\r\\n\\r\\n\"\n"
										+ "}")
				);
	}

	@TransactionDefinition(name = "stopVsAndVsDoesntExist")
	public void virtualizedStopAndVsDoesntExist() {
		forPost("http://" + host + "/api/Dcm/VSEs/VSE/vs-doesnt-exist/actions/stop/")
				.matchesBasicAuthorization("admin", "admin")
				.matchesHeader("Accept", "application/vnd.ca.lisaInvoke.virtualService+json")
				.doReturn(
						serverErrorMessage()
								.withJsonBody("{\n"
										+ "    \"id\": 1007,\n"
										+ "    \"message\": \"No such virtual service: vs-doesnt-exist\",\n"
										+ "    \"addInfo\": \"\\r\\njava.lang.IllegalArgumentException: No such virtual service: vs-doesnt-exist\\r\\n\\tat com.itko.lisa.coordinator.VirtualServiceEnvironmentImpl.stopTheService(VirtualServiceEnvironmentImpl.java:1548)\\r\\n\\tat com.itko.lisa.coordinator.VirtualServiceEnvironmentImpl.stopService(VirtualServiceEnvironmentImpl.java:1514)\\r\\n\\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\\r\\n\\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\\r\\n\\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\\r\\n\\tat java.lang.reflect.Method.invoke(Method.java:498)\\r\\n\\tat com.itko.lisa.net.RemoteMethodExec.execute(RemoteMethodExec.java:57)\\r\\n\\tat com.itko.lisa.net.ServerRequestHandler.processRMIMessage(ServerRequestHandler.java:533)\\r\\n\\tat com.itko.lisa.net.ServerRequestHandler.access$600(ServerRequestHandler.java:70)\\r\\n\\tat com.itko.lisa.net.ServerRequestHandler$5.run(ServerRequestHandler.java:451)\\r\\n\\tat java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)\\r\\n\\tat java.util.concurrent.FutureTask.run(FutureTask.java:266)\\r\\n\\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)\\r\\n\\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)\\r\\n\\tat java.lang.Thread.run(Thread.java:745)\\r\\n\\r\\n\"\n"
										+ "}")
				);
	}

	/*
	Deploy VS virtualization
  */

	@TransactionDefinition(name = "startTestWrongMarFile")
	public void virtualizedStartTestWrongMarFileTest() {
		forPost("http://" + host + "/lisa-test-invoke/api/v1/tests/run")
				.doReturn(
						aMessage(400)
								.withJsonBody("{\n"
										+ "  \"error\": {\n"
										+ "    \"type\": \"IllegalArgumentException\",\n"
										+ "    \"message\": \"Our audit file is either missing or we are not a model archive or are corrupt.\"\n"
										+ "  }\n"
										+ "}"
								)
				);
	}

	@TransactionDefinition(name = "startTestMarFileTest")
	public void virtualizedStartTestMarFileTest() {
		forPost("http://" + host + "/lisa-test-invoke/api/v1/tests/run")
				.doReturn(
						okMessage()
								.withJsonBody(
										"{\n"
												+ "  \"_links\": {\n"
												+ "    \"self\": {\n"
												+ "      \"href\": \"http://marpa24n223060:1505/lisa-test-invoke/api/v1/tests/run/828D1E9D5CE611E8A5A80050569C3565\"\n"
												+ "    }\n"
												+ "  },\n"
												+ "  \"id\": \"828D1E9D5CE611E8A5A80050569C3565\",\n"
												+ "  \"name\": \"REST Example (Run1User1Cycle0Think)\",\n"
												+ "  \"runType\": \"TEST\",\n"
												+ "  \"runBy\": \"admin\",\n"
												+ "  \"testStatus\": \"INITIATED\",\n"
												+ "  \"manuallyTerminated\": false\n"
												+ "}"
								)
				);
	}

	@TransactionDefinition(name = "startTestMarFileSuite")
	public void virtualizedStartTestMarFileSuite() {
		forPost("http://" + host + "/lisa-test-invoke/api/v1/suites/run")
				.doReturn(
						okMessage()
								.withJsonBody(
										"{\n"
												+ "  \"_links\": {\n"
												+ "    \"self\": {\n"
												+ "      \"href\": \"http://marpa24n223060:1505/lisa-test-invoke/api/v1/suites/run/4A9797CA5DC111E8A5A80050569C3565\"\n"
												+ "    }\n"
												+ "  },\n"
												+ "  \"id\": \"4A9797CA5DC111E8A5A80050569C3565\",\n"
												+ "  \"name\": \"testsuite\",\n"
												+ "  \"runType\": \"SUITE\",\n"
												+ "  \"runBy\": \"admin\",\n"
												+ "  \"testStatus\": \"RUNNING\",\n"
												+ "  \"manuallyTerminated\": false\n"
												+ "}"
								)
				);
	}

	@TransactionDefinition(name = "virtualizeStartTestGetTestStatus")
	public void virtualizeStartTestGetTestStatus() {
		forGet("http://" + host + "/lisa-test-invoke/api/v1/tests/run/828D1E9D5CE611E8A5A80050569C3565")
				.doReturn(
						okMessage()
								.withJsonBody(
										"{\n"
												+ "  \"_links\": {\n"
												+ "    \"self\": {\n"
												+ "      \"href\": \"http://marpa24n223060:1505/lisa-test-invoke/api/v1/tests/run/D85F86C25C7811E8A5A80050569C3565\"\n"
												+ "    }\n"
												+ "  },\n"
												+ "  \"id\": \"D85F86C25C7811E8A5A80050569C3565\",\n"
												+ "  \"name\": \"REST Example (Run1User1Cycle0Think)\",\n"
												+ "  \"runType\": \"TEST\",\n"
												+ "  \"runBy\": \"admin\",\n"
												+ "  \"testStatus\": \"PASSED\",\n"
												+ "  \"manuallyTerminated\": false\n"
												+ "}")
				);

	}

	@TransactionDefinition(name = "virtualizeStartTestGetSuiteStatus")
	public void virtualizeStartTestGetSuiteStatus() {
		forGet(
				"http://" + host + "/lisa-test-invoke/api/v1/suites/run/4A9797CA5DC111E8A5A80050569C3565")
				.doReturn(
						okMessage()
								.withJsonBody(
										"{\n"
												+ "  \"_links\": {\n"
												+ "    \"self\": {\n"
												+ "      \"href\": \"http://marpa24n223060:1505/lisa-test-invoke/api/v1/suites/run/4A9797CA5DC111E8A5A80050569C3565\"\n"
												+ "    }\n"
												+ "  },\n"
												+ "  \"id\": \"D85F86C25C7811E8A5A80050569C3565\",\n"
												+ "  \"name\": \"REST Example (Run1User1Cycle0Think)\",\n"
												+ "  \"runType\": \"SUITE\",\n"
												+ "  \"runBy\": \"admin\",\n"
												+ "  \"testStatus\": \"PASSED\",\n"
												+ "  \"manuallyTerminated\": false\n"
												+ "}")
				);

	}


	/**
	 *Transaction Definition created for DevTestCreateAndDeployVsTest class - START
	 *
	 * @author sv673714
	 */
	//Create And Deploy VS virtualization
	@TransactionDefinition(name = "createdeployVsif")
	public void createdAndDeployedSuccessfulVsFromInputFile() {
		forPost("http://" + host + "/lisa-virtualize-invoke/api/v3/vses/VSE/services")
				.matchesBasicAuthorization("admin", "admin")
				.matchesHeader("Accept", "application/json")
				.matchesBody(contains("inputFile1"))
				.doReturn(
						aMessage(200)
								.withJsonBody("{\"_links\":{\"self\":"
										+"{\"href\":\""+host+"/lisa-virtualize-invoke/api/v2/vses/vse-id/services/vs-id\"}},"
										+"\"serviceId\":\"vs-id\",\"serviceName\":\"API_Test_1\",\"modelName\":\"test-create-vs\","
										+"\"capacity\":1,\"txnPerSecond\":0,\"txnCount\":0,\"peakTxnPerSecond\":0,"
										+"\"thinkScale\":100,\"errorCount\":0,\"autoRestartEnabled\":true,\"status\":2,"
										+"\"statusDescription\":\"running\",\"startTime\":\"3/18/21 8:46:35 PM\","
										+"\"startTimeLong\":1616080595673,\"endTime\":\"\",\"endTimeLong\":0,\"upTime\":123,"
										+"\"executionMode\":\"Most Efficient\",\"executionModeValue\":\"EFFICIENT\","
										+"\"configurationName\":\"project.config\",\"resourceName\":\"8002 : http :  : /\",\"groupTag\":\"\"}")
				);
	}

	/*
	Create And Deploy VS virtualization
	 */
	@TransactionDefinition(name = "createdeployVs")
	public void createdAndDeployedVsSuccessfulUploadVs() {
		forPost("http://" + host + "/lisa-virtualize-invoke/api/v3/vses/VSE/services")
				.matchesBasicAuthorization("admin", "admin")
				.matchesHeader("Accept", "application/json")
				.matchesBody(contains("inputFile1"))
				.doReturn(
						aMessage(200)
								.withJsonBody("{\"_links\":{\"self\":"
										+"{\"href\":\""+host+"/lisa-virtualize-invoke/api/v2/vses/vse-id/services/vs-id\"}},"
										+"\"serviceId\":\"vs-id\",\"serviceName\":\"API_Test_1\",\"modelName\":\"test-create-vs\","
										+"\"capacity\":1,\"txnPerSecond\":0,\"txnCount\":0,\"peakTxnPerSecond\":0,"
										+"\"thinkScale\":100,\"errorCount\":0,\"autoRestartEnabled\":true,\"status\":2,"
										+"\"statusDescription\":\"running\",\"startTime\":\"3/18/21 8:46:35 PM\","
										+"\"startTimeLong\":1616080595673,\"endTime\":\"\",\"endTimeLong\":0,\"upTime\":123,"
										+"\"executionMode\":\"Most Efficient\",\"executionModeValue\":\"EFFICIENT\","
										+"\"configurationName\":\"project.config\",\"resourceName\":\"8002 : http :  : /\",\"groupTag\":\"\"}")
				);
	}

	/*
	Create And Deploy VS virtualization
	 */
	@TransactionDefinition(name = "createdeployVsURI")
	public void createdAndDeployedVsSuccessWithURI() {
		forPost("http://" + host + "/lisa-virtualize-invoke/api/v3/vses/VSE/services")
				.matchesBasicAuthorization("admin", "admin")
				.matchesHeader("Accept", "application/json")
				.matchesBody( either(contains("swaggerurl")).or(contains("ramlurl")).or(contains("wadlurl")))
				.doReturn(
						aMessage(200)
								.withJsonBody("{\"_links\":{\"self\":"
										+"{\"href\":\""+host+"/lisa-virtualize-invoke/api/v2/vses/vse-id/services/vs-id\"}},"
										+"\"serviceId\":\"vs-id\",\"serviceName\":\"API_Test_1\",\"modelName\":\"test-create-vs\","
										+"\"capacity\":1,\"txnPerSecond\":0,\"txnCount\":0,\"peakTxnPerSecond\":0,"
										+"\"thinkScale\":100,\"errorCount\":0,\"autoRestartEnabled\":true,\"status\":2,"
										+"\"statusDescription\":\"running\",\"startTime\":\"3/18/21 8:46:35 PM\","
										+"\"startTimeLong\":1616080595673,\"endTime\":\"\",\"endTimeLong\":0,\"upTime\":123,"
										+"\"executionMode\":\"Most Efficient\",\"executionModeValue\":\"EFFICIENT\","
										+"\"configurationName\":\"project.config\",\"resourceName\":\"8002 : http :  : /\",\"groupTag\":\"\"}")
				);
	}

	//Transaction Definition created for DevTestCreateAndDeployVsTest - END
}
