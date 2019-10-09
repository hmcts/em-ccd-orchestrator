package uk.gov.hmcts.reform.em.orchestrator.testutil;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import io.restassured.RestAssured;
import org.json.JSONObject;

public class S2sHelper {

    private final String s2sUrl;
    private final String emGwTotpSecret;
    private final String emGwMicroserviceName;
    private final String ccdGwTotpSecret;
    private final String ccdGwMicroserviceName;

    public S2sHelper(String s2sUrl, String emGwTotpSecret, String emGwMicroserviceName,
                     String ccdGwTotpSecret, String ccdGwMicroserviceName) {
        this.s2sUrl = s2sUrl;
        this.emGwTotpSecret = emGwTotpSecret;
        this.emGwMicroserviceName = emGwMicroserviceName;
        this.ccdGwTotpSecret = ccdGwTotpSecret;
        this.ccdGwMicroserviceName = ccdGwMicroserviceName;
    }

    public String getEmGwS2sToken() {
        return getS2sToken(emGwMicroserviceName, emGwTotpSecret);
    }

    public String getCcdGwS2sToken() {
        return getS2sToken(ccdGwMicroserviceName, ccdGwTotpSecret);
    }

    private String getS2sToken(String microserviceName, String microserviceSecret) {
        String otp = String.valueOf(new GoogleAuthenticator().getTotpPassword(microserviceSecret));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("microservice", microserviceName);
        jsonObject.put("oneTimePassword", otp);

        return "Bearer " + RestAssured
                .given()
                .header("Content-Type", "application/json")
                .body(jsonObject.toString())
                .post(s2sUrl + "/lease")
                .getBody()
                .asString();
    }
}
