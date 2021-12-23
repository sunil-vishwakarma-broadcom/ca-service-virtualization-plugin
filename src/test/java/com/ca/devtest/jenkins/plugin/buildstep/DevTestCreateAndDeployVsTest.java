package com.ca.devtest.jenkins.plugin.buildstep;

import com.ca.codesv.engine.junit4.VirtualServerRule;
import com.ca.codesv.sdk.annotation.TransactionClassRepository;
import com.ca.devtest.jenkins.plugin.util.MultiFileSCM;
import hudson.model.Cause;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.SingleFileSCM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;

public class DevTestCreateAndDeployVsTest extends AbstractDevTestBuildStepTest {

    public static final String configObject = "{ \"virtualService\": { \"version\": \"2\", \"name\": \"API_Test_1\", "
            + "\"description\": \"Invoke API V2\", \"status\": \"\" }, \"transportProtocol\": { \"typeId\": \"HTTP\", "
            + "\"basePath\": \"/\", \"useGateway\": true, \"duptxns\": true, \"hostHeaderPassThrough\": false }, "
            + "\"dataProtocol\": { \"forRequest\": true, \"typeId\": \"RESTDPH\" } }";

    @Rule
    @TransactionClassRepository(repoClasses = {ApiRepository.class})
    public VirtualServerRule vs = new VirtualServerRule(this);

    @Test
    public void testEnvironmentPropertiesInConfig() throws Exception {
        Jenkins.getInstance().getGlobalNodeProperties().replaceBy(
                Collections.singleton(new EnvironmentVariablesNodeProperty(
                        new Entry("host", "1.1.1.1"), new Entry("port", "6666"),
                        new Entry("vse", "test"), new Entry("config", configObject),
                        new Entry("deploy", "true"),new Entry("undeploy", "false"),
                        new Entry("inputFile1", "test-create-vs.vsi"),
                        new Entry("inputFile2", "test-create-vs.vsm"), new Entry("activeConfig", "test-create-vs.config"),
                        new Entry("dataFiles", "test-create-vs.zip"), new Entry("swaggerurl", "http://test.yaml"),
                        new Entry("ramlurl", "http://test.raml"), new Entry("wadlurl", "http://test.wsdl"))));

        FreeStyleProject project = jenkins.createFreeStyleProject();
        DevTestCreateAndDeployVs devTestCreateAndDeployVs = createPlugin(true, "${host}", "${port}",
                "${vse}", "${config}", "${deploy}","${undeploy}", "${inputFile1}", "${inputFile2}", "${activeConfig}",
                "${dataFiles}", "${swaggerurl}", "${ramlurl}","${wadlurl}","id");
        project.getBuildersList().add(devTestCreateAndDeployVs);
        FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0,new Cause.UserIdCause()));
        jenkins.assertLogContains("Creating and deploying virtual service To DevTest API located on 1.1.1.1:6666", build);
        jenkins.assertLogContains("Config Object: "+configObject, build);
        jenkins.assertLogContains("Deploy: true", build);
        jenkins.assertLogContains("Undeploy: false", build);
        jenkins.assertLogContains("Input file 1 path is  - test-create-vs.vsi", build);
        jenkins.assertLogContains("Input file 2 path is  - test-create-vs.vsm", build);
        jenkins.assertLogContains("Active Config path is  - test-create-vs.config", build);
        jenkins.assertLogContains("Data File path is  - test-create-vs.zip", build);

        FreeStyleProject project1 = jenkins.createFreeStyleProject();
        DevTestCreateAndDeployVs devTestCreateAndDeployVsFromUrl = createPlugin(true, "${host}", "${port}",
                "${vse}", "${config}", "${deploy}","${undeploy}", null, null, null,
                null, "${swaggerurl}", "${ramlurl}","${wadlurl}","id");
        project1.getBuildersList().add(devTestCreateAndDeployVsFromUrl);
        FreeStyleBuild build1 = jenkins.assertBuildStatus(Result.FAILURE, project1.scheduleBuild2(0));
        jenkins.assertLogContains("Swagger url: http://test.yaml", build1);
        jenkins.assertLogContains("Raml url: http://test.raml", build1);
        jenkins.assertLogContains("Wadl url: http://test.wsdl", build1);
    }

    @Test
    public void testVseNameEmpty() throws Exception {
        vs.useTransaction("deployVsAndVseDoesntExist");

        FreeStyleProject project = jenkins.createFreeStyleProject("test1");
        DevTestCreateAndDeployVs builder = createPlugin(true, getHost(), "1505", "",
                configObject, "true", "true",null, null, null, null, null, null, null, "id");
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
        jenkins.assertLogContains("ERROR: VSE name cannot be empty!", build);
    }

    @Test
    public void testVseNameNull() throws Exception {
        vs.useTransaction("deployVsAndVseDoesntExist");

        FreeStyleProject project = jenkins.createFreeStyleProject("test2");
        DevTestCreateAndDeployVs builder = createPlugin(true, getHost(), "1505", null,
                configObject, "true", "true",null, null, null, null, null, null, null, "id");
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
        jenkins.assertLogContains("ERROR: VSE name cannot be empty!", build);
    }

    @Test
    public void testConfigObjectEmpty() throws Exception {
        vs.useTransaction("deployVsAndVseDoesntExist");

        FreeStyleProject project = jenkins.createFreeStyleProject("test3");
        DevTestCreateAndDeployVs builder = createPlugin(true, getHost(), "1505", "VSE",
                "", "true","true", null, null, null, null, null, null, null, "id");
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
        jenkins.assertLogContains("ERROR: Configuration JSON cannot be empty", build);
    }

    @Test
    public void testConfigObjectNull() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("test4");
        DevTestCreateAndDeployVs builder = createPlugin(true, getHost(), "1505", "VSE",
                null, "true", "true",null, null, null, null, null, null, null, "id");
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
        jenkins.assertLogContains("ERROR: Configuration JSON cannot be empty", build);
    }

    @Test
    public void testInvalidCustomRegAttr() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("test5");
        DevTestCreateAndDeployVs builder = createPlugin(true, "", "", "VSE",
                configObject, "true", "true",null, null, null, null, null, null, null, "id");
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
        jenkins.assertLogContains("ERROR: URI does not specify a valid host name", build);
    }

    @Test
    public void testBuildWithFile() throws Exception {
        vs.useTransaction("createdeployVsif");

        FreeStyleProject project = jenkins.createFreeStyleProject("test6");
        DevTestCreateAndDeployVs builder = createPluginForIF1IF2(configObject, "true", "true","file://"+getClass().getResource("/test-create-vs.vsi").getPath(), "file://"+getClass().getResource("/test-create-vs.vsm").getPath());
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("\"serviceName\":\"API_Test_1\"", build);
        jenkins.assertLogContains("\"modelName\":\"test-create-vs\"", build);
    }

    @Test
    public void testBuildWithFileOnJenkins() throws Exception {
        vs.useTransaction("createdeployVs");
        FreeStyleProject project = jenkins.createFreeStyleProject("test7");

        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(4);
        files.add(new SingleFileSCM("test-create-vs.vsi", getClass().getResource("/test-create-vs.vsi")));
        files.add(new SingleFileSCM("test-create-vs.vsm", getClass().getResource("/test-create-vs.vsm")));
        files.add(new SingleFileSCM("test-create-vs.config", getClass().getResource("/test-create-vs.config")));
        files.add(new SingleFileSCM("test-create-vs.zip", getClass().getResource("/test-create-vs.zip")));
        MultiFileSCM multiFileSCM = new MultiFileSCM(files);
        project.setScm(multiFileSCM);

        DevTestCreateAndDeployVs builder = createPluginForIf1If2AcDf(configObject, "true","true", "test-create-vs.vsi", "test-create-vs.vsm", "test-create-vs.config", "test-create-vs.zip");
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("\"serviceName\":\"API_Test_1\"", build);
        jenkins.assertLogContains("\"modelName\":\"test-create-vs\"", build);
    }

    @Test
    public void testBuildMissingFile() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("test8");
        DevTestCreateAndDeployVs builder = createPluginForIF1IF2(configObject, "true","false", "file:///C:/test-create-vsqqqqq.vsi", "file:///C:/test-create-vs.vsm");
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
        jenkins.assertLogContains("ERROR: /C:/test-create-vsqqqqq.vsi (No such file or directory)", build);
    }

    @Test
    public void testBuildWithSwagger() throws Exception {

        vs.useTransaction("createdeployVsURI");
        FreeStyleProject project = jenkins.createFreeStyleProject("test9");

        DevTestCreateAndDeployVs builder = createPluginForS(configObject, "true", "true", "http://test.com/test-create-vs.yaml");
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("\"serviceName\":\"API_Test_1\"", build);
        jenkins.assertLogContains("\"modelName\":\"test-create-vs\"", build);
    }

    @Test
    public void testBuildWithRaml() throws Exception {

        vs.useTransaction("createdeployVsURI");
        FreeStyleProject project = jenkins.createFreeStyleProject("test10");

        DevTestCreateAndDeployVs builder = createPluginForR(configObject, "true", "true", "http://test.com/test-create-vs.raml");
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("\"serviceName\":\"API_Test_1\"", build);
        jenkins.assertLogContains("\"modelName\":\"test-create-vs\"", build);
    }

    @Test
    public void testBuildWithWadl() throws Exception {

        vs.useTransaction("createdeployVsURI");
        FreeStyleProject project = jenkins.createFreeStyleProject("test11");

        DevTestCreateAndDeployVs builder = createPluginForW(configObject, "true", "true", "http://test.com/test-create-vs.wsdl");
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("\"serviceName\":\"API_Test_1\"", build);
        jenkins.assertLogContains("\"modelName\":\"test-create-vs\"", build);
    }

    //Create and deploy VS from inputFile1 and inputFile2
    private DevTestCreateAndDeployVs createPluginForIF1IF2(String config, String deploy, String undeploy, String inputFile1, String inputFile2) {
        return createPlugin(true, getHost(), "1505", "VSE", config, deploy, undeploy, inputFile1, inputFile2,
                null, null, null, null, null, "id");
    }

    private DevTestCreateAndDeployVs createPluginForIf1If2AcDf(String config, String deploy, String undeploy, String inputFile1, String inputFile2, String activeConfig, String dataFiles) {
        return createPlugin(true, getHost(), "1505", "VSE", config, deploy, undeploy, inputFile1, inputFile2,
                activeConfig, dataFiles, null, null, null, "id");
    }

    //Create and deploy VS from swaggerurl
    private DevTestCreateAndDeployVs createPluginForS(String config, String deploy,  String undeploy,String swaggerurl) {
        return createPlugin(true, getHost(), "1505", "VSE", config, deploy, undeploy, null, null,
                null, null, swaggerurl, null, null, "id");
    }

    //Create and deploy VS from ramlurl
    private DevTestCreateAndDeployVs createPluginForR(String config, String deploy, String undeploy, String ramlurl) {
        return createPlugin(true, getHost(), "1505", "VSE", config, deploy, undeploy, null, null,
                null, null, null, ramlurl, null, "id");
    }

    //Create and deploy VS from wadlurl
    private DevTestCreateAndDeployVs createPluginForW(String config, String deploy, String undeploy, String wadlurl) {
        return createPlugin(true, getHost(), "1505", "VSE", config, deploy, undeploy, null, null,
                null, null, null, null, wadlurl, "id");
    }

    private DevTestCreateAndDeployVs createPlugin(boolean useCustomRegistry, String host, String port, String vseName, String config,
                                                  String deploy, String undeploy, String inputFile1, String inputFile2, String activeConfig, String dataFiles,
                                                  String swaggerurl, String ramlurl, String wadlurl, String tokenId) {
        return new DevTestCreateAndDeployVs(useCustomRegistry, host, port, vseName, config, deploy, undeploy, inputFile1, inputFile2,
                activeConfig, dataFiles, swaggerurl, ramlurl, wadlurl, tokenId, false);
    }
}