package uk.gov.hmcts.reform.em.orchestrator.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import jakarta.annotation.PostConstruct;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.ImageRendering;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.ImageRenderingLocation;
import uk.gov.hmcts.reform.em.orchestrator.functional.dto.CcdTestBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.functional.dto.CcdTestDocument;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBoolean;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentImage;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.TaskState;
import uk.gov.hmcts.reform.em.test.ccddata.CcdDataHelper;
import uk.gov.hmcts.reform.em.test.cdam.CdamHelper;
import uk.gov.hmcts.reform.em.test.dm.DmHelper;
import uk.gov.hmcts.reform.em.test.idam.IdamHelper;
import uk.gov.hmcts.reform.em.test.s2s.S2sHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.touk.throwing.ThrowingFunction.unchecked;

@Service
public class TestUtil {

    private final int retryCount = 6;
    private final int sleepTime = 2000;
    private String idamAuth;
    private String s2sAuth;

    @Autowired
    private IdamHelper idamHelper;
    @Autowired
    private S2sHelper s2sHelper;
    @Autowired
    private DmHelper dmHelper;
    @Autowired
    private CcdDataHelper ccdDataHelper;

    @Autowired
    private CdamHelper cdamHelper;

    @Autowired
    @Qualifier("xuiS2sHelper")
    private S2sHelper cdamS2sHelper;

    @Value("${test.url}")
    private String testUrl;
    @Value("${document_management.url}")
    private String dmApiUrl;
    @Value("${document_management.docker_url}")
    private String dmDocumentApiUrl;

    @Value("${em-rpa-stitching-api.base-url}")
    private String stitchingBaseUrl;

    @Value("${em-rpa-stitching-api.resource}")
    private String stitchingResource;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public final String createAutomatedBundlingCaseTemplate = "{\n"
        + "    \"caseTitle\": null,\n"
        + "    \"caseOwner\": null,\n"
        + "    \"caseCreationDate\": null,\n"
        + "    \"caseDescription\": null,\n"
        + "    \"caseComments\": null,\n"
        + "    \"caseDocuments\": [%s],\n"
        + "    \"bundleConfiguration\": \"f-tests-1-flat-docs.yaml\"\n"
        + "  }";

    public final String createCdamAutomatedBundlingCaseTemplate = "{\n"
        + "    \"caseTitle\": null,\n"
        + "    \"caseOwner\": null,\n"
        + "    \"caseCreationDate\": null,\n"
        + "    \"caseDescription\": null,\n"
        + "    \"caseComments\": null,\n"
        + "    \"caseDocuments\": %s,\n"
        + "    \"bundleConfiguration\": \"f-tests-1-flat-docs.yaml\"\n"
        + "  }";

    @PostConstruct
    public void init() {
        idamHelper.createUser(getUsername(), Stream.of("caseworker", "caseworker-publiclaw")
            .collect(Collectors.toList()));
        SerenityRest.useRelaxedHTTPSValidation();
        idamAuth = idamHelper.authenticateUser(getUsername());
        s2sAuth = s2sHelper.getS2sToken();
    }

    public String uploadDocument(String fileName, String mimeType) {
        try {
            String url = dmHelper.getDocumentMetadata(
                    dmHelper.uploadAndGetId(
                            ClassLoader.getSystemResourceAsStream(fileName), mimeType, fileName))
                    .links.self.href;

            return getDmApiUrl().equals("http://localhost:4603")
                    ? url.replaceAll(getDmApiUrl(), getDmDocumentApiUrl())
                    : url;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String uploadDocument() {
        return uploadDocument("annotationTemplate.pdf", "application/pdf");
    }

    public RequestSpecification s2sAuthRequest() {
        return SerenityRest
                .given()
                .header("ServiceAuthorization", s2sAuth);
    }

    public RequestSpecification authRequest() {
        return s2sAuthRequest()
                .header("Authorization", idamAuth);
    }

    public RequestSpecification unauthenticatedRequest() {
        return SerenityRest.given();
    }

    public CcdBundleDTO getTestBundle() {
        CcdBundleDTO bundle = new CcdBundleDTO();
        bundle.setId(UUID.randomUUID().toString());
        bundle.setTitle("Bundle title");
        bundle.setDescription("Test bundle");
        bundle.setEligibleForStitchingAsBoolean(true);
        bundle.setEligibleForCloningAsBoolean(false);

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

    public String uploadDocX(String docName) {
        return uploadDocument(docName, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    public static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    public String getUsername() {
        return "testytesttest" + getTestUrl().hashCode() + "@test.net";
    }

    public String getTestUrl() {
        return testUrl;
    }

    public String getDmApiUrl() {
        return dmApiUrl;
    }

    public String getDmDocumentApiUrl() {
        return dmDocumentApiUrl;
    }

    public CcdBundleDTO getTestBundleWithImageRendered() {
        DocumentImage documentImage = new DocumentImage();
        documentImage.setImageRendering(ImageRendering.translucent);
        documentImage.setImageRenderingLocation(ImageRenderingLocation.firstPage);
        documentImage.setCoordinateX(50);
        documentImage.setCoordinateY(50);
        documentImage.setDocmosisAssetId("schmcts.png");
        CcdBundleDTO bundle = new CcdBundleDTO();
        bundle.setId(UUID.randomUUID().toString());
        bundle.setTitle("Bundle title");
        bundle.setDescription("Test bundle");
        bundle.setEligibleForStitchingAsBoolean(true);
        bundle.setEligibleForCloningAsBoolean(false);
        bundle.setDocumentImage(documentImage);

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
        bundle.setDocumentImage(documentImage);
        return bundle;
    }

    public RequestSpecification emptyIdamAuthRequest() {
        return s2sAuthRequest()
                .header("Authorization", null);
    }

    public RequestSpecification emptyIdamAuthAndEmptyS2SAuth() {
        return SerenityRest
                .given()
                .header("ServiceAuthorization", null)
                .header("Authorization", null);
    }

    public RequestSpecification validAuthRequestWithEmptyS2SAuth() {
        return emptyS2sAuthRequest().header("Authorization", idamAuth);
    }

    public RequestSpecification validS2SAuthWithEmptyIdamAuth() {
        return s2sAuthRequest().header("Authorization", null);
    }

    private RequestSpecification emptyS2sAuthRequest() {
        return SerenityRest.given().header("ServiceAuthorization", null);
    }

    public RequestSpecification invalidIdamAuthrequest() {
        return s2sAuthRequest().header("Authorization", "invalidIDAMAuthRequest");
    }

    public RequestSpecification invalidS2SAuth() {
        return invalidS2sAuthRequest().header("Authorization", idamAuth);
    }

    private RequestSpecification invalidS2sAuthRequest() {
        return SerenityRest.given().header("ServiceAuthorization", "invalidS2SAuthorization");
    }

    //////////// CDAM //////////////////

    public RequestSpecification cdamAuthRequest() {
        return cdamS2sAuthRequest()
            .header("Authorization", idamAuth);
    }

    public RequestSpecification cdamS2sAuthRequest() {
        return SerenityRest
            .given()
            .log().all()
            .header("ServiceAuthorization", cdamS2sHelper.getS2sToken());
    }

    public List<CcdValue<CcdBundleDocumentDTO>> uploadCdamBundleDocuments(
                    List<Pair<String, String>> fileDetails, String userName) throws Exception {

        List<MultipartFile> multipartFiles = fileDetails.stream()
            .map(unchecked(pair -> createMultipartFile(pair.getFirst(), pair.getSecond())))
            .collect(Collectors.toList());

        DocumentUploadRequest uploadRequest = new DocumentUploadRequest(
            Classification.PUBLIC.toString(), getEnvCcdCaseTypeId(), "PUBLICLAW", multipartFiles);

        UploadResponse uploadResponse =  cdamHelper.uploadDocuments(getUsername(), uploadRequest);

        createCaseAndUploadDocuments(uploadResponse, userName);

        List<CcdValue<CcdBundleDocumentDTO>> bundleDocuments = uploadResponse.getDocuments().stream()
            .map(this::createBundleDocument)
            .collect(Collectors.toList());
        return bundleDocuments;
    }

    /*
    Uploads Documents through CDAM and attachs the response DocUrl & Hash against the case. And creates/submits the
    case.
     */
    public List<String> createCaseAndUploadDocuments(UploadResponse uploadResponse, String userName) throws Exception {
        List<CcdValue<CcdTestBundleDocumentDTO>> bundleDocuments = uploadResponse.getDocuments().stream()
            .map(this::createTestBundleDocument)
            .collect(Collectors.toList());
        String documentsString = objectMapper.writeValueAsString(bundleDocuments);
        CaseDetails caseDetails = createBundleCase(documentsString, userName);


        return uploadResponse.getDocuments().stream()
            .map(document -> document.links.self.href)
            .collect(Collectors.toList());
    }

    public CcdValue<CcdBundleDocumentDTO> createBundleDocument(Document document) {
        CcdDocument ccdDocument = CcdDocument.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .hash(document.hashToken)
            .fileName(document.originalDocumentName)
            .build();
        CcdBundleDocumentDTO ccdBundleDocumentDTO = CcdBundleDocumentDTO.builder()
            .sourceDocument(ccdDocument)
            .name(document.originalDocumentName)
            .build();
        return new CcdValue<>(ccdBundleDocumentDTO);
    }

    public CaseDetails createBundleCase(String documents, String userName) throws Exception {
        return ccdDataHelper.createCase(userName, "PUBLICLAW", getEnvCcdCaseTypeId(), "createCase",
            objectMapper.readTree(String.format(createCdamAutomatedBundlingCaseTemplate, documents)));
    }

    public Document.Links uploadCdamDocument() throws Exception {
        List<MultipartFile> multipartFiles = Stream.of(Pair.of("annotationTemplate.pdf", "application/pdf"))
                .map(unchecked(pair -> createMultipartFile(pair.getFirst(), pair.getSecond())))
                .collect(Collectors.toList());

        DocumentUploadRequest uploadRequest = new DocumentUploadRequest(
            Classification.PUBLIC.toString(), getEnvCcdCaseTypeId(), "PUBLICLAW", multipartFiles);

        UploadResponse uploadResponse =  cdamHelper.uploadDocuments(getUsername(), uploadRequest);
        return uploadResponse.getDocuments().get(0).links;
    }

    public String uploadCdamDocuments(List<Pair<String, String>> fileDetails) throws Exception {

        List<MultipartFile> multipartFiles = fileDetails.stream()
            .map(unchecked(pair -> createMultipartFile(pair.getFirst(), pair.getSecond())))
            .collect(Collectors.toList());

        DocumentUploadRequest uploadRequest = new DocumentUploadRequest(
            Classification.PUBLIC.toString(), getEnvCcdCaseTypeId(), "PUBLICLAW", multipartFiles);

        UploadResponse uploadResponse =  cdamHelper.uploadDocuments(getUsername(), uploadRequest);

        List<CcdValue<CcdTestBundleDocumentDTO>> bundleDocuments = uploadResponse.getDocuments().stream()
            .map(this::createTestBundleDocument)
            .collect(Collectors.toList());
        return objectMapper.writeValueAsString(bundleDocuments);
    }

    private MultipartFile createMultipartFile(String fileName, String contentType) throws IOException {
        return new MockMultipartFile(fileName, fileName, contentType,
            ClassLoader.getSystemResourceAsStream(fileName));
    }


    public CcdValue<CcdTestBundleDocumentDTO> createTestBundleDocument(Document document) {
        CcdTestDocument ccdTestDocument = CcdTestDocument.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .hash(document.hashToken)
            .fileName(document.originalDocumentName)
            .build();
        CcdTestBundleDocumentDTO ccdTestBundleDocumentDTO = CcdTestBundleDocumentDTO.builder()
            .documentLink(ccdTestDocument)
            .documentName(document.originalDocumentName)
            .build();
        return new CcdValue<>(ccdTestBundleDocumentDTO);
    }

    public String getEnvCcdCaseTypeId() {
        return ExtendedCcdHelper.CCD_BUNDLE_MVP_TYPE_ASYNC;
    }

    public CcdBundleDTO getCdamTestBundle(String userName) throws Exception {
        CcdBundleDTO bundle = new CcdBundleDTO();
        bundle.setId(UUID.randomUUID().toString());
        bundle.setTitle("Bundle title");
        bundle.setDescription("Test bundle");
        bundle.setEligibleForStitchingAsBoolean(true);
        bundle.setEligibleForCloningAsBoolean(false);

        List<Pair<String, String>> fileDetails = new ArrayList<>();
        fileDetails.add(Pair.of("annotationTemplate.pdf", "application/pdf"));
        fileDetails.add(Pair.of("hundred-page.pdf", "application/pdf"));

        List<CcdValue<CcdBundleDocumentDTO>> docs = uploadCdamBundleDocuments(fileDetails, userName);
        bundle.setDocuments(docs);

        bundle.setFileName("fileName");
        bundle.setHasTableOfContents(CcdBoolean.Yes);
        bundle.setHasCoversheets(CcdBoolean.Yes);
        bundle.setStitchStatus("");
        return bundle;
    }

    public CcdBundleDTO getCdamTestBundleWithWordDoc(String userName) throws Exception {
        CcdBundleDTO bundle = new CcdBundleDTO();
        bundle.setTitle("Bundle title");
        bundle.setDescription("Test bundle");
        bundle.setEligibleForStitchingAsBoolean(true);

        List<Pair<String, String>> fileDetails = new ArrayList<>();
        fileDetails.add(Pair.of("annotationTemplate.pdf", "application/pdf"));
        fileDetails.add(Pair.of("wordDocument2.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));

        List<CcdValue<CcdBundleDocumentDTO>> docs = uploadCdamBundleDocuments(fileDetails, userName);
        bundle.setDocuments(docs);

        return bundle;
    }

    public CcdBundleDTO getCdamTestBundleWithImageRendered(String userName) throws Exception {
        DocumentImage documentImage = new DocumentImage();
        documentImage.setImageRendering(ImageRendering.translucent);
        documentImage.setImageRenderingLocation(ImageRenderingLocation.firstPage);
        documentImage.setCoordinateX(50);
        documentImage.setCoordinateY(50);
        documentImage.setDocmosisAssetId("schmcts.png");
        CcdBundleDTO bundle = new CcdBundleDTO();
        bundle.setId(UUID.randomUUID().toString());
        bundle.setTitle("Bundle title");
        bundle.setDescription("Test bundle");
        bundle.setEligibleForStitchingAsBoolean(true);
        bundle.setEligibleForCloningAsBoolean(false);
        bundle.setDocumentImage(documentImage);

        List<Pair<String, String>> fileDetails = new ArrayList<>();
        fileDetails.add(Pair.of("annotationTemplate.pdf", "application/pdf"));
        fileDetails.add(Pair.of("hundred-page.pdf", "application/pdf"));

        List<CcdValue<CcdBundleDocumentDTO>> docs = uploadCdamBundleDocuments(fileDetails, userName);
        bundle.setDocuments(docs);

        bundle.setFileName("fileName");
        bundle.setHasTableOfContents(CcdBoolean.Yes);
        bundle.setHasCoversheets(CcdBoolean.Yes);
        bundle.setStitchStatus("");
        bundle.setDocumentImage(documentImage);
        return bundle;
    }

    public ValidatableResponse poll(long documentTaskId) throws InterruptedException, IOException {
        final RequestSpecification requestSpecification = authRequest()
                .baseUri(getTestUrl())
                .contentType(APPLICATION_JSON_VALUE);

        for (int i = 0; i < retryCount; i++) {
            final Response response = requestSpecification.get(stitchingBaseUrl
                    + stitchingResource + "/" + documentTaskId);
            final JsonPath jsonPath = response.body().jsonPath();
            final String taskState = jsonPath.getString("taskState");
            if (!taskState.equals(TaskState.NEW.toString())) {
                return response.then();
            } else {
                Thread.sleep(sleepTime);
            }
        }
        throw new IOException("Task not complete after maximum number of retries");
    }

    public String addCdamProperties(Object json) {
        String cdamJson =  "{ \"caseTypeId\":\"CCD_BUNDLE_MVP_TYPE\", "
                + "\"jurisdictionId\":\"BENEFIT\",%s } }";
        String string = json.toString();
        return String.format(cdamJson, string.substring(1, string.length() - 1));
    }

}
