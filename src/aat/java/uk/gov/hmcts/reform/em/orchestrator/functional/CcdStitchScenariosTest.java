package uk.gov.hmcts.reform.em.orchestrator.functional;

import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.em.orchestrator.config.Constants;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBoolean;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.em.orchestrator.testutil.TestConsts.CASE_DETAILS_CASE_DATA_CASE_BUNDLES_S;
import static uk.gov.hmcts.reform.em.orchestrator.testutil.TestConsts.DATA_CASE_BUNDLES_0_VALUE_FILE_NAME;
import static uk.gov.hmcts.reform.em.orchestrator.testutil.TestConsts.DATA_CASE_BUNDLES_0_VALUE_STITCHED_DOCUMENT_DOCUMENT_URL;
import static uk.gov.hmcts.reform.em.orchestrator.testutil.TestConsts.DATA_CASE_BUNDLES_0_VALUE_TITLE;
import static uk.gov.hmcts.reform.em.orchestrator.testutil.TestConsts.ERRORS_0;

class CcdStitchScenariosTest extends BaseTest {

    public static final String BUNDLE_TITLE = "Bundle title";

    @Autowired
    protected CcdStitchScenariosTest(
            TestUtil testUtil,
            ExtendedCcdHelper extendedCcdHelper
    ) {
        super(testUtil, extendedCcdHelper);
    }

    @BeforeEach
    public void setUp() {
        Assumptions.assumeFalse(enableCdamValidation);
    }

    @Test
    void testPostBundleStitch() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(CASE_DETAILS_CASE_DATA_CASE_BUNDLES_S, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_TITLE, equalTo(BUNDLE_TITLE))
                .body(DATA_CASE_BUNDLES_0_VALUE_STITCHED_DOCUMENT_DOCUMENT_URL, notNullValue());
    }

    @Test
    void testPostBundleStitchWithCaseId() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson =
            String.format("{ \"id\": \"123\", \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_TITLE, equalTo(BUNDLE_TITLE))
                .body(DATA_CASE_BUNDLES_0_VALUE_STITCHED_DOCUMENT_DOCUMENT_URL, notNullValue());
    }

    @Test
    void testPostBundleStitchWithWordDoc() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundleWithWordDoc();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(CASE_DETAILS_CASE_DATA_CASE_BUNDLES_S, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_TITLE, equalTo(BUNDLE_TITLE))
                .body(DATA_CASE_BUNDLES_0_VALUE_STITCHED_DOCUMENT_DOCUMENT_URL, notNullValue());
    }

    @Test
    void testSpecificFilename() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName("my-file-name.pdf");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(CASE_DETAILS_CASE_DATA_CASE_BUNDLES_S, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_TITLE, equalTo(BUNDLE_TITLE))
                .body(DATA_CASE_BUNDLES_0_VALUE_FILE_NAME, equalTo("my-file-name.pdf"))
                .body(DATA_CASE_BUNDLES_0_VALUE_STITCHED_DOCUMENT_DOCUMENT_URL, notNullValue());

    }

    @Test
    void testFilenameWithoutExtension() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName("doc-file-name");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(CASE_DETAILS_CASE_DATA_CASE_BUNDLES_S, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.stitchedDocument.document_filename", equalTo("doc-file-name.pdf"))
                .body(DATA_CASE_BUNDLES_0_VALUE_FILE_NAME, equalTo("doc-file-name"));
    }

    @Test
    void testPostBundleStitchFileNameOneChar() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName("a");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(CASE_DETAILS_CASE_DATA_CASE_BUNDLES_S, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(400)
                .body(ERRORS_0, equalTo(Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG));
    }

    @Test
    void testPostBundleStitchFileName51Char() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName(Constants.FILE_NAME_WITH_51_CHARS_LENGTH);

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(CASE_DETAILS_CASE_DATA_CASE_BUNDLES_S, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(400)
                .body(ERRORS_0, equalTo(Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG));
    }

    @Test
    void testNoFileNameButBundleTitleOnly() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundleWithWordDoc();

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(CASE_DETAILS_CASE_DATA_CASE_BUNDLES_S, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_TITLE, equalTo(BUNDLE_TITLE))
                .body("data.caseBundles[0].value.stitchedDocument.document_filename", equalTo("Bundle title.pdf"));
    }

    @Test
    void testFilenameErrors() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName("1234567890123456789012345678901%.pdf");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(CASE_DETAILS_CASE_DATA_CASE_BUNDLES_S, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(400)
                .body(DATA_CASE_BUNDLES_0_VALUE_TITLE, equalTo(BUNDLE_TITLE))
                .body(DATA_CASE_BUNDLES_0_VALUE_FILE_NAME, equalTo("1234567890123456789012345678901%.pdf"))
                .body(ERRORS_0, notNullValue());
    }

    @Test
    void testLongBundleDescriptionErrors() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();

        bundle.setDescription("y".repeat(300));
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(CASE_DETAILS_CASE_DATA_CASE_BUNDLES_S, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);
        response
                .assertThat().log().all()
                .statusCode(400)
                .body(ERRORS_0, notNullValue());
    }

    @Test
    void testWithoutCoversheets() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setHasCoversheets(CcdBoolean.No);

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(CASE_DETAILS_CASE_DATA_CASE_BUNDLES_S, json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_TITLE, equalTo(BUNDLE_TITLE))
                .body("data.caseBundles[0].value.hasCoversheets", equalTo("No"))
                .body(DATA_CASE_BUNDLES_0_VALUE_STITCHED_DOCUMENT_DOCUMENT_URL, notNullValue());
    }

    @Test
    void testWithImageRendering() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundleWithImageRendered();
        bundle.setHasCoversheets(CcdBoolean.No);

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(CASE_DETAILS_CASE_DATA_CASE_BUNDLES_S, json);

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
    void testPostBundleStitchAsync() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(CASE_DETAILS_CASE_DATA_CASE_BUNDLES_S, json);

        ValidatableResponse response = postAsyncStitchCCDBundle(wrappedJson);
        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        response = testUtil.poll(documentTaskId);
        response
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo(BUNDLE_TITLE))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testPostAsyncBundleStitchFileNameOneChar() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName("a");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(CASE_DETAILS_CASE_DATA_CASE_BUNDLES_S, json);

        ValidatableResponse response = postAsyncStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(400)
                .body(ERRORS_0, equalTo(Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG));
    }

    @Test
    void testPostAsyncLongBundleDescriptionErrors() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();

        bundle.setDescription("y".repeat(300));
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format(CASE_DETAILS_CASE_DATA_CASE_BUNDLES_S, json);

        ValidatableResponse response = postAsyncStitchCCDBundle(wrappedJson);
        response
                .assertThat().log().all()
                .statusCode(400)
                .body(ERRORS_0, notNullValue());
    }

    private ValidatableResponse postStitchCCDBundle(String wrappedJson) {
        return testUtil
                .authRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .post("/api/stitch-ccd-bundles")
                .then();
    }

    private ValidatableResponse postAsyncStitchCCDBundle(String wrappedJson) {
        return testUtil
                .authRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .post("/api/async-stitch-ccd-bundles")
                .then();
    }
}
