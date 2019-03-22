package uk.gov.hmcts.reform.em.orchestrator.testutil;

import io.restassured.RestAssured;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Base64;

public class IdamHelper {

    private static final String USERNAME = "testytesttest@test.net";
    private static final String PASSWORD = "4590fgvhbfgbDdffm3lk4j";

    private final String idamUrl;
    private final String client;
    private final String secret;
    private final String redirect;

    public IdamHelper(String idamUrl, String client, String secret, String redirect) {
        this.idamUrl = idamUrl;
        this.client = client;
        this.secret = secret;
        this.redirect = redirect;
    }

    public String getIdamToken() {
        createUser();

        String code = getCode();
        String token = getToken(code);

        return "Bearer " + token;
    }

    private void createUser() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", USERNAME);
        jsonObject.put("password", PASSWORD);
        jsonObject.put("forename", "test");
        jsonObject.put("surname", "test");

        RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(jsonObject.toString())
            .post(idamUrl + "/testing-support/accounts");
    }

    private String getCode() {
        String credentials = USERNAME + ":" + PASSWORD;
        String authHeader = Base64.getEncoder().encodeToString(credentials.getBytes());

        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header("Authorization", "Basic " + authHeader)
            .formParam("redirect_uri", redirect)
            .formParam("client_id", client)
            .formParam("response_type", "code")
            .post(idamUrl + "/oauth2/authorize")
            .jsonPath()
            .get("code");
    }

    private String getToken(String code) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .formParam("code", code)
            .formParam("grant_type", "authorization_code")
            .formParam("redirect_uri", redirect)
            .formParam("client_id", client)
            .formParam("client_secret", secret)
            .post(idamUrl + "/oauth2/token")
            .jsonPath()
            .getString("access_token");
    }
}
