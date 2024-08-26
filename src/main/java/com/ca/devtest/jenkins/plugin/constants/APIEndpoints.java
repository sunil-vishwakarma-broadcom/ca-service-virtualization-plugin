package com.ca.devtest.jenkins.plugin.constants;

/**
 * Constant class containing APIEndpoints.
 *
 */
public final class APIEndpoints {

    private APIEndpoints(){ }

    public static final String TEST_CONNECTION_API = "/api/Dcm/";
    public static final String UNDEPLOY_VS = "/api/Dcm/VSEs/%s/%s/";

    public static final String SERVICES = "/lisa-virtualize-invoke/api/v3/vses/%s/services";

    public static final String REPORTS_SUITE_URL = "/lisa-test-invoke/api/v1/suites/reports/";
    public static final String SUITE_REPORT_URL = "/lisa-test-invoke/api/v1/suites/reports/%s";
    public static final String SUITE_TESTS_REPORT_URL = "/lisa-test-invoke/api/v1/suites/reports/%s/tests";

    public static final String REPORTS_TEST_URL = "/lisa-test-invoke/api/v1/tests/reports/";
    public static final String REPORT_URL = "/lisa-test-invoke/api/v1/tests/reports/%s";
    public static final String CYCLE_URL = "/lisa-test-invoke/api/v1/tests/reports/%s/cycles";

    public static final String RUN_TEST_URL = "/lisa-test-invoke/api/v1/tests/run";
    public static final String RUN_SUITE_URL = "/lisa-test-invoke/api/v1/suites/run";

    public static final String DEPLOY_MAR = "/api/Dcm/VSEs/%s/actions/deployMar/";
    public static final String START_VS = "/api/Dcm/VSEs/%s/%s/actions/start/";
    public static final String STOP_VS = "/api/Dcm/VSEs/%s/%s/actions/stop/";

}
