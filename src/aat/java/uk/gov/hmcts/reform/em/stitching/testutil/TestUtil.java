package uk.gov.hmcts.reform.em.stitching.testutil;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.stitching.service.dto.BundleDTO;
import uk.gov.hmcts.reform.em.stitching.service.dto.BundleDocumentDTO;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class TestUtil {

    private String s2sToken;
    private String idamToken;

    public File getDocumentBinary(String documentId) throws Exception {
        Response response = s2sAuthRequest()
            .header("user-roles", "caseworker")
            .request("GET", Env.getDmApiUrl() + "/documents/" + documentId + "/binary");

        Path tempPath = Paths.get(System.getProperty("java.io.tmpdir") + "/" + documentId + "-test.pdf");

        Files.copy(response.getBody().asInputStream(), tempPath, StandardCopyOption.REPLACE_EXISTING);

        return tempPath.toFile();
    }

    public String uploadDocument(String pdfName) {
        String newDocUrl = s2sAuthRequest()
            .header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
            .multiPart("files", "test.pdf", ClassLoader.getSystemResourceAsStream(pdfName), "application/pdf")
            .multiPart("classification", "PUBLIC")
            .request("POST", Env.getDmApiUrl() + "/documents")
            .getBody()
            .jsonPath()
            .get("_embedded.documents[0]._links.self.href");

        return newDocUrl;
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

    public String getIdamToken() {
        return getIdamToken("test@test.com");
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
                .post(Env.getS2SURL() + "/lease");
            s2sToken = response.getBody().asString();
            s2sToken = response.getBody().print();
        }

        return s2sToken;
    }

    public BundleDTO getTestBundle() {
        BundleDTO bundle = new BundleDTO();
        bundle.setDescription("Test bundle");
        List<BundleDocumentDTO> docs = new ArrayList<>();
        docs.add(getTestBundleDocument(uploadDocument()));
        docs.add(getTestBundleDocument(uploadDocument()));
        bundle.setDocuments(docs);

        return bundle;
    }

    public BundleDocumentDTO getTestBundleDocument(String documentUrl) {
        String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
        BundleDocumentDTO document = new BundleDocumentDTO();

        document.setDocumentId(documentId);
        document.setDocumentURI(documentUrl);

        return document;
    }

    public BundleDTO getTestBundleWithWordDoc() {
        BundleDTO bundle = new BundleDTO();
        bundle.setDescription("Test bundle");
        List<BundleDocumentDTO> docs = new ArrayList<>();
        docs.add(getTestBundleDocument(uploadWordDocument("wordDocument.doc")));
        docs.add(getTestBundleDocument(uploadDocX("wordDocument2.docx")));
        bundle.setDocuments(docs);

        return bundle;
    }

    public String uploadWordDocument(String docName) {
        String newDocUrl = s2sAuthRequest()
            .header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
            .multiPart("files", "test.doc", ClassLoader.getSystemResourceAsStream(docName), "application/msword")
            .multiPart("classification", "PUBLIC")
            .request("POST", Env.getDmApiUrl() + "/documents")
            .getBody()
            .jsonPath()
            .get("_embedded.documents[0]._links.self.href");

        return newDocUrl;
    }

    public String uploadDocX(String docName) {
        String newDocUrl = s2sAuthRequest()
            .header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
            .multiPart("files", "test.docx", ClassLoader.getSystemResourceAsStream(docName), "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            .multiPart("classification", "PUBLIC")
            .request("POST", Env.getDmApiUrl() + "/documents")
            .getBody()
            .jsonPath()
            .get("_embedded.documents[0]._links.self.href");

        return newDocUrl;
    }
}

