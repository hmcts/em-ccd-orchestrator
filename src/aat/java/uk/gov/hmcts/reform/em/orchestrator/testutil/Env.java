package uk.gov.hmcts.reform.em.orchestrator.testutil;

import org.apache.commons.lang3.Validate;

import java.util.Properties;

public final class Env {

    static Properties defaults = new Properties();

    private Env() {
    }

    static {
        defaults.setProperty("PROXY", "false");
        defaults.setProperty("TEST_URL", "http://localhost:8080");
        defaults.setProperty("DM_STORE_APP_URL", "http://localhost:4603");
        defaults.setProperty("DOCKER_DM_STORE_APP_URL", "http://dm-store:8080");
        defaults.setProperty("IDAM_API_BASE_URI", "http://localhost:4501");
        defaults.setProperty("OAUTH_CLIENT", "webshow");
        defaults.setProperty("IDAM_WEBSHOW_WHITELIST", "http://localhost:8080/oauth2redirect");
        defaults.setProperty("FUNCTIONAL_TEST_CLIENT_OAUTH_SECRET", "AAAAAAAAAAAAAAAA");
        defaults.setProperty("S2S_BASE_URI", "http://localhost:4502");
        defaults.setProperty("FUNCTIONAL_TEST_CLIENT_S2S_TOKEN", "AAAAAAAAAAAAAAAA");
        defaults.setProperty("S2S_SERVICE_NAME", "em_gw");
        defaults.setProperty("CCD_GW_SERVICE_NAME", "ccd_gw");
        defaults.setProperty("FUNCTIONAL_TEST_CCD_GW_SERVICE_SECRET", "AAAAAAAAAAAAAAAA");
        defaults.setProperty("CCD_DEF_API", "http://localhost:4451");
        defaults.setProperty("CCD_DATA_API", "http://localhost:4452");
        defaults.setProperty("CCD_CASE_DEF_FILE", "adv_bundling_functional_tests_ccd_def.xlsx");


    }

    public static String getIdamUrl() {
        return require("IDAM_API_BASE_URI");
    }

    public static String getOAuthClient() {
        return require("OAUTH_CLIENT");
    }

    public static String getOAuthRedirect() {
        return require("IDAM_WEBSHOW_WHITELIST");
    }

    public static String getOAuthSecret() {
        return require("FUNCTIONAL_TEST_CLIENT_OAUTH_SECRET");
    }

    public static String getS2sUrl() {
        return require("S2S_BASE_URI");
    }

    public static String getEmGwS2sSecret() {
        return require("FUNCTIONAL_TEST_CLIENT_S2S_TOKEN");
    }

    public static String getEmGwS2sMicroservice() {
        return require("S2S_SERVICE_NAME");
    }

    public static String getCcdGwS2sSecret() {
        return require("FUNCTIONAL_TEST_CCD_GW_SERVICE_SECRET");
    }

    public static String getCcdGwS2sMicroservice() {
        return require("CCD_GW_SERVICE_NAME");
    }

    public static String getUseProxy() {
        return require("PROXY");
    }

    public static String getTestUrl() {
        return require("TEST_URL");
    }

    public static String getDmApiUrl() {
        return require("DM_STORE_APP_URL");
    }

    public static String getDockerDmApiUrl() {
        return require("DOCKER_DM_STORE_APP_URL");
    }

    public static String getCcdDefApiUrl() {
        return require("CCD_DEF_API");
    }

    public static String getCcdDataApiUrl() {
        return require("CCD_DATA_API");
    }

    public static String getCcdDefFileName() {
        return require("CCD_CASE_DEF_FILE");
    }

    public static String require(String name) {
        return Validate.notNull(System.getenv(name) == null ? defaults.getProperty(name) : System.getenv(name), "Environment variable `%s` is required", name);
    }
}
