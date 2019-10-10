package uk.gov.hmcts.reform.em.orchestrator.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.junit.Assert;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IdamHelper {

    private static final String USERNAME = "testytesttest" + new Random().nextLong() + "@test.net";
    private static final String PASSWORD = "4590fgvhbfgbDdffm3lk4j";

    private final String idamUrl;
    private final String client;
    private final String secret;
    private final String redirect;

    private final Map<String, String> idamTokens = new HashMap<>();

    ObjectMapper mapper = new ObjectMapper();

    public IdamHelper(String idamUrl, String client, String secret, String redirect) {
        this.idamUrl = idamUrl;
        this.client = client;
        this.secret = secret;
        this.redirect = redirect;
    }

    public String getIdamToken() {
        return getIdamToken(USERNAME, Stream.of("caseworker").collect(Collectors.toList()));
    }

    public String getIdamToken(String username, List<String> roles) {

        if (!idamTokens.containsKey(username)) {
            deleteUser(username);
            createUser(username, roles);

            String code = getCode(username);
            String token = getToken(code);

            idamTokens.put(username, "Bearer " + token);
        }
        return idamTokens.get(username);
    }

    public String getUserId(String username) {
        String userId = RestAssured
            .given().log().all()
            .header("Authorization", idamTokens.get(username))
            .get(idamUrl + "/details").andReturn().jsonPath().get("id").toString();

        return userId;
    }

    public void createUser(String username, List<String> roles) {
        try {
            System.out.println(RestAssured
                    .given().log().all()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(mapper.writeValueAsString(CreateUserDto.builder()
                            .email(username)
                            .password(PASSWORD)
                            .surname("x")
                            .forename("x")
                            .roles(roles.stream().map(role -> new CreateUserRolesDto(role)).collect(Collectors.toList()))
                            .build()))
                    .post(idamUrl + "/testing-support/accounts").andReturn().print());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUser(String username) {
        int statusCode = RestAssured.given().log().all()
                .delete(idamUrl + "/testing-support/accounts/" + username).andReturn().getStatusCode();
        Assert.assertTrue(HttpHelper.isSuccessful(statusCode) || statusCode == 404);
        idamTokens.remove(username);
    }

    private String getCode(String username) {
        String credentials = username + ":" + PASSWORD;
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
            .given().log().all()
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




