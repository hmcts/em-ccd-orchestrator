package uk.gov.hmcts.reform.em.stitching.testutil;

import org.apache.commons.lang3.Validate;

import java.util.Properties;

public class Env {

    static Properties defaults = new Properties();

    static {
        defaults.setProperty("PROXY", "false");
        defaults.setProperty("TEST_URL", "http://localhost:8080");
        defaults.setProperty("S2S_TOKEN", "AAAAAAAAAAAAAAAA");
        defaults.setProperty("S2S_SERVICE_NAME", "em_gw");
        defaults.setProperty("S2S_URL", "http://localhost:4502");
        defaults.setProperty("IDAM_API_USER_ROLE", "caseworker");
        defaults.setProperty("IDAM_API_USER", "test@test.com");
        defaults.setProperty("IDAM_API_URL", "http://localhost:4501");
        defaults.setProperty("EM_STITCHING_API_URL", "http://localhost:4623");
        defaults.setProperty("DM_STORE_APP_URL", "http://localhost:4603");
    }

    public static String getUseProxy() { return require("PROXY"); }

    public static String getTestUrl() {
        return require("TEST_URL");
    }

    public static String getIdamURL() {
        return require("IDAM_API_URL");
    }

    public static String getS2SURL() {
        return require("S2S_URL");
    }

    public static String getS2SToken() {
        return require("S2S_TOKEN");
    }

    public static String getS2SServiceName() {
        return require("S2S_SERVICE_NAME");
    }

    public static String getStitchingApiUrl() {
        return require("EM_STITCHING_API_URL");
    }

    public static String getDmApiUrl() {
        return require("DM_STORE_APP_URL");
    }

    public static String require(String name) {
        return Validate.notNull(System.getenv(name) == null ? defaults.getProperty(name) : System.getenv(name), "Environment variable `%s` is required", name);
    }
}
