package uk.gov.hmcts.reform.em.orchestrator.testutil;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.*;

import java.util.ArrayList;
import java.util.List;

public class TestUtil {

    private final String idamAuth;
    private final String s2sAuth;

    public TestUtil() {
        IdamHelper idamHelper = new IdamHelper(
            Env.getIdamUrl(),
            Env.getOAuthClient(),
            Env.getOAuthSecret(),
            Env.getOAuthRedirect()
        );

        S2sHelper s2sHelper = new S2sHelper(
            Env.getS2sUrl(),
            Env.getS2sSecret(),
            Env.getS2sMicroservice()
        );

        RestAssured.useRelaxedHTTPSValidation();

        idamAuth = idamHelper.getIdamToken();
        s2sAuth = s2sHelper.getS2sToken();
    }

    public String uploadDocument(String pdfName) {
        String url = s2sAuthRequest()
            .header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
            .multiPart("files", "test.pdf", ClassLoader.getSystemResourceAsStream(pdfName), "application/pdf")
            .multiPart("classification", "PUBLIC")
            .request("POST", Env.getDmApiUrl() + "/documents")
            .getBody()
            .jsonPath()
            .get("_embedded.documents[0]._links.self.href");

        return Env.getDmApiUrl().equals("http://localhost:4603")
            ? url.replaceAll(Env.getDmApiUrl(), Env.getDockerDmApiUrl())
            : url;
    }

    public String uploadDocument() {
        return uploadDocument("annotationTemplate.pdf");
    }

    public RequestSpecification s2sAuthRequest() {
        return RestAssured
            .given()
            .header("ServiceAuthorization", s2sAuth);
    }

    public RequestSpecification authRequest() {
        return s2sAuthRequest()
            .header("Authorization", idamAuth);
    }

    public CcdBundleDTO getTestBundle() {
        CcdBundleDTO bundle = new CcdBundleDTO();
        bundle.setId(2L);
        bundle.setTitle("Bundle title");
        bundle.setDescription("Test bundle");
        bundle.setEligibleForStitchingAsBoolean(true);
        bundle.setEligibleForCloningAsBoolean(false);

        CcdDocument doc = new CcdDocument();
        doc.setBinaryUrl("www.exampleurl.com/binary");
        doc.setFileName("doc filename");
        doc.setUrl("www.exampleurl.com");
        bundle.setStitchedDocument(doc);

        List<CcdValue<CcdBundleDocumentDTO>> docs = new ArrayList<>();
        docs.add(getTestBundleDocument(uploadDocument()));
        bundle.setDocuments(docs);

        bundle.setFileName("fileName");
        bundle.setHasTableOfContents(CcdBoolean.Yes);
        bundle.setHasCoversheets(CcdBoolean.Yes);
        bundle.setStitchStatus("");
        return bundle;
    }

    private CcdValue<CcdBundleDocumentDTO> getTestBundleDocument(String documentUrl) {
        CcdBundleDocumentDTO document = new CcdBundleDocumentDTO("test document",
                "description", 1, new CcdDocument(documentUrl, "fn",
                uriWithBinarySuffix(documentUrl)));
        return new CcdValue<>(document);
    }

    public static String uriWithBinarySuffix(String s) {
        return s.endsWith("/binary") ? s : s + "/binary";
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

        return Env.getDmApiUrl().equals("http://localhost:4603")
            ? url.replaceAll(Env.getDmApiUrl(), Env.getDockerDmApiUrl())
            : url;
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

        return Env.getDmApiUrl().equals("http://localhost:4603")
            ? url.replaceAll(Env.getDmApiUrl(), Env.getDockerDmApiUrl())
            : url;
    }
}

