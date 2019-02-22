package uk.gov.hmcts.reform.em.orchestrator.testutil;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.util.ArrayList;
import java.util.List;

public class TestUtil {

    private String s2sToken;
    private String idamToken;

    public String uploadDocument(String pdfName) {
        String url = s2sAuthRequest()
            .header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
            .multiPart("files", "test.pdf", ClassLoader.getSystemResourceAsStream(pdfName), "application/pdf")
            .multiPart("classification", "PUBLIC")
            .request("POST", Env.getDmApiUrl() + "/documents")
            .getBody()
            .jsonPath()
            .get("_embedded.documents[0]._links.self.href");

        return url.replaceAll(Env.getDmApiUrl(), Env.getDockerDmApiUrl());
    }

    public String uploadDocument() {
        return uploadDocument("annotationTemplate.pdf");
    }

    public RequestSpecification authRequest() {
        return s2sAuthRequest()
            .header("Authorization", "Bearer " + getIdamToken("test@test.com"));
    }

    public RequestSpecification s2sAuthRequest() {
        RestAssured.useRelaxedHTTPSValidation();
        return RestAssured
            .given()
            .header("ServiceAuthorization", "Bearer " + getS2sToken());
    }

    public String getIdamToken(String username) {
        if (idamToken == null) {
            createUser(username, "password");
            Integer id = findUserIdByUserEmail(username);
            String userId = id.toString();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", userId);
            jsonObject.put("role", "caseworker");

            Response response = RestAssured
                .given()
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .formParam("id", userId)
                .formParam("role", "caseworker")
                .post(Env.getIdamURL() + "/testing-support/lease");

            idamToken = response.getBody().print();
        }
        return idamToken;
    }

    private Integer findUserIdByUserEmail(String email) {
        return RestAssured
            .get(Env.getIdamURL() + "/testing-support/accounts/" + email)
            .getBody()
            .jsonPath()
            .get("id");
    }

    public void createUser(String email, String password) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", email);
        jsonObject.put("password", password);
        jsonObject.put("forename", "test");
        jsonObject.put("surname", "test");

        RestAssured
            .given()
            .header("Content-Type", "application/json")
            .body(jsonObject.toString())
            .post(Env.getIdamURL() + "/testing-support/accounts");

    }


    public String getS2sToken() {

        if (s2sToken == null) {
            String otp = String.valueOf(new GoogleAuthenticator().getTotpPassword(Env.getS2SToken()));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("microservice", Env.getS2SServiceName());
            jsonObject.put("oneTimePassword", otp);

            Response response = RestAssured
                .given()
                .header("Content-Type", "application/json")
                .body(jsonObject.toString())
                    .post(Env.getS22Url() + "/lease");
            s2sToken = response.getBody().asString();
            s2sToken = response.getBody().print();
        }

        return s2sToken;
    }

    public CcdBundleDTO getTestBundle() {
        CcdBundleDTO bundle = new CcdBundleDTO();
        bundle.setTitle("Bundle title");
        bundle.setDescription("Test bundle");
        bundle.setEligibleForStitchingAsBoolean(true);
        List<CcdValue<CcdBundleDocumentDTO>> docs = new ArrayList<>();
        docs.add(getTestBundleDocument(uploadDocument()));
        bundle.setDocuments(docs);

        return bundle;
    }

    public CcdValue<CcdBundleDocumentDTO> getTestBundleDocument(String documentUrl) {
        CcdBundleDocumentDTO document = new CcdBundleDocumentDTO("test document",
                "description", 1, new CcdDocument(documentUrl, "fn",
                documentUrl + "/binary"));
        return new CcdValue<>(document);
    }

    public CcdBundleDTO getTestBundleWithWordDoc() {
        CcdBundleDTO bundle = new CcdBundleDTO();
        bundle.setTitle("Bundle title");
        bundle.setDescription("Test bundle");
        bundle.setEligibleForStitchingAsBoolean(true);
        List<CcdValue<CcdBundleDocumentDTO>> docs = new ArrayList<>();
        docs.add(getTestBundleDocument(uploadDocX("wordDocument2.docx")));
        bundle.setDocuments(docs);

        return bundle;
    }

    public String uploadWordDocument(String docName) {
        String url = s2sAuthRequest()
            .header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
                .multiPart("files", "test.doc", ClassLoader.getSystemResourceAsStream(docName),
                        "application/msword")
            .multiPart("classification", "PUBLIC")
            .request("POST", Env.getDmApiUrl() + "/documents")
            .getBody()
            .jsonPath()
            .get("_embedded.documents[0]._links.self.href");

        return url.replaceAll(Env.getDmApiUrl(), Env.getDockerDmApiUrl());
    }

    public String uploadDocX(String docName) {
        String url = s2sAuthRequest()
            .header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
                .multiPart("files", "test.docx", ClassLoader.getSystemResourceAsStream(docName),
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            .multiPart("classification", "PUBLIC")
            .request("POST", Env.getDmApiUrl() + "/documents")
            .getBody()
            .jsonPath()
            .get("_embedded.documents[0]._links.self.href");

        return url.replaceAll(Env.getDmApiUrl(), Env.getDockerDmApiUrl());
    }
}

