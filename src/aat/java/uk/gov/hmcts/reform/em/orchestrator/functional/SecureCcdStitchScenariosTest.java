package uk.gov.hmcts.reform.em.orchestrator.functional;

import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.em.orchestrator.config.Constants;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBoolean;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class SecureCcdStitchScenariosTest extends BaseTest {

    // This has the caseTypeId, jurisdictionId populated by default(Hard coded) as these are required fields for CDAM.
    private static final String SYNC_CASE_JSON =  "{ \"caseTypeId\":\"CCD_BUNDLE_MVP_TYPE\", "
        + "\"jurisdictionId\":\"BENEFIT\",\"case_details\":{ "
        + "\"case_data\":{ \"caseBundles\":[%s ] } } }";

    @BeforeEach
    public void setUp() {
        assumeTrue(enableCdamValidation);
    }

    @Test
    void testPostBundleStitch() throws Exception {
        CcdBundleDTO bundle = testUtil.getCdamTestBundle(extendedCcdHelper.getBundleTesterUser());
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(SYNC_CASE_JSON, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[0].value.stitchedDocument.document_url", notNullValue())
                .body("data.caseBundles[0].value.stitchedDocument.document_hash", notNullValue());
    }

    @Test
    void testPostBundleStitchWithCaseId() throws Exception {
        CcdBundleDTO bundle = testUtil.getCdamTestBundle(extendedCcdHelper.getBundleTesterUser());
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String caseJson = String.format(SYNC_CASE_JSON.substring(1), json);
        String wrappedJson = String.format("{ \"id\": \"123\", %s", caseJson);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[0].value.stitchedDocument.document_url", notNullValue())
                .body("data.caseBundles[0].value.stitchedDocument.document_hash", notNullValue());
    }

    @Test
    void testPostBundleStitchWithWordDoc() throws Exception {
        CcdBundleDTO bundle = testUtil.getCdamTestBundleWithWordDoc(extendedCcdHelper.getBundleTesterUser());
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(SYNC_CASE_JSON, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[0].value.stitchedDocument.document_url", notNullValue())
                .body("data.caseBundles[0].value.stitchedDocument.document_hash", notNullValue());
    }

    @Test
    void testSpecificFilename() throws Exception {
        CcdBundleDTO bundle = testUtil.getCdamTestBundle(extendedCcdHelper.getBundleTesterUser());
        bundle.setFileName("my-file-name.pdf");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(SYNC_CASE_JSON, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[0].value.fileName", equalTo("my-file-name.pdf"))
                .body("data.caseBundles[0].value.stitchedDocument.document_url", notNullValue())
                .body("data.caseBundles[0].value.stitchedDocument.document_hash", notNullValue());
    }

    @Test
    void testFilenameWithoutExtension() throws Exception {
        CcdBundleDTO bundle = testUtil.getCdamTestBundle(extendedCcdHelper.getBundleTesterUser());
        bundle.setFileName("doc-file-name");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(SYNC_CASE_JSON, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.stitchedDocument.document_filename", equalTo("doc-file-name.pdf"))
                .body("data.caseBundles[0].value.fileName", equalTo("doc-file-name"));
    }

    @Test
    void testPostBundleStitchFileNameOneChar() throws Exception {
        CcdBundleDTO bundle = testUtil.getCdamTestBundle(extendedCcdHelper.getBundleTesterUser());
        bundle.setFileName("a");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(SYNC_CASE_JSON, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", equalTo(Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG));
    }

    @Test
    void testPostBundleStitchFileName51Char() throws Exception {
        CcdBundleDTO bundle = testUtil.getCdamTestBundle(extendedCcdHelper.getBundleTesterUser());
        bundle.setFileName(Constants.FILE_NAME_WITH_51_CHARS_LENGTH);

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(SYNC_CASE_JSON, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", equalTo(Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG));
    }

    @Test
    void testNoFileNameButBundleTitleOnly() throws Exception {
        CcdBundleDTO bundle = testUtil.getCdamTestBundleWithWordDoc(extendedCcdHelper.getBundleTesterUser());

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(SYNC_CASE_JSON, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[0].value.stitchedDocument.document_filename", equalTo("Bundle title.pdf"));
    }

    @Test
    void testFilenameErrors() throws Exception {
        CcdBundleDTO bundle = testUtil.getCdamTestBundle(extendedCcdHelper.getBundleTesterUser());
        bundle.setFileName("1234567890123456789012345678901%.pdf");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(SYNC_CASE_JSON, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(400)
                .body("data.caseBundles[0].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[0].value.fileName", equalTo("1234567890123456789012345678901%.pdf"))
                .body("errors[0]", notNullValue());
    }

    @Test
    void testLongBundleDescriptionErrors() throws Exception {
        CcdBundleDTO bundle = testUtil.getCdamTestBundle(extendedCcdHelper.getBundleTesterUser());

        bundle.setDescription("y".repeat(300));
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(SYNC_CASE_JSON, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);
        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", notNullValue());
    }

    @Test
    void testWithoutCoversheets() throws Exception {
        CcdBundleDTO bundle = testUtil.getCdamTestBundle(extendedCcdHelper.getBundleTesterUser());
        bundle.setHasCoversheets(CcdBoolean.No);

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(SYNC_CASE_JSON, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[0].value.hasCoversheets", equalTo("No"))
                .body("data.caseBundles[0].value.stitchedDocument.document_url", notNullValue())
                .body("data.caseBundles[0].value.stitchedDocument.document_hash", notNullValue());
    }

    @Test
    void testWithImageRendering() throws Exception {
        CcdBundleDTO bundle = testUtil.getCdamTestBundleWithImageRendered(extendedCcdHelper.getBundleTesterUser());
        bundle.setHasCoversheets(CcdBoolean.No);

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(SYNC_CASE_JSON, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.documentImage.docmosisAssetId", equalTo("schmcts.png"))
                .body("data.caseBundles[0].value.documentImage.imageRenderingLocation", equalTo("firstPage"))
                .body("data.caseBundles[0].value.documentImage.imageRendering", equalTo("translucent"))
                .body("data.caseBundles[0].value.documentImage.coordinateX", equalTo(50))
                .body("data.caseBundles[0].value.documentImage.coordinateY", equalTo(50));
    }

    @Test
    void testPostBundleStitchAsync() throws Exception {
        CcdBundleDTO bundle = testUtil.getCdamTestBundle(extendedCcdHelper.getBundleTesterUser());
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(SYNC_CASE_JSON, json);

        ValidatableResponse response = postAsyncStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("Bundle title"));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        response = testUtil.poll(documentTaskId);
        response
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Bundle title"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testPostAsyncBundleStitchFileNameOneChar() throws Exception {
        CcdBundleDTO bundle = testUtil.getCdamTestBundle(extendedCcdHelper.getBundleTesterUser());
        bundle.setFileName("a");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(SYNC_CASE_JSON, json);

        ValidatableResponse response = postAsyncStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", equalTo(Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG));
    }

    @Test
    void testPostAsyncLongBundleDescriptionErrors() throws Exception {
        CcdBundleDTO bundle = testUtil.getCdamTestBundle(extendedCcdHelper.getBundleTesterUser());

        bundle.setDescription("y".repeat(300));
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(SYNC_CASE_JSON, json);

        ValidatableResponse response = postAsyncStitchCCDBundle(wrappedJson);
        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", notNullValue());
    }

    private ValidatableResponse postStitchCCDBundle(String wrappedJson) {
        return testUtil
                .cdamAuthRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .post("/api/stitch-ccd-bundles")
                .then();
    }

    private ValidatableResponse postAsyncStitchCCDBundle(String wrappedJson) {
        return testUtil
                .cdamAuthRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .post("/api/async-stitch-ccd-bundles")
                .then();
    }
}
