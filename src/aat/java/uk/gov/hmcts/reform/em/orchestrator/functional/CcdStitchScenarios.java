package uk.gov.hmcts.reform.em.orchestrator.functional;

import io.restassured.response.ValidatableResponse;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.config.Constants;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBoolean;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CcdStitchScenarios extends BaseTest {

    @Rule
    public RetryRule retryRule = new RetryRule(5);

    @Before
    public void setUp() throws Exception {
        Assume.assumeFalse(enableCdamValidation);
    }

    @Test
    public void testPostBundleStitch() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[0].value.stitchedDocument.document_url", notNullValue());
    }

    @Test
    public void testPostBundleStitchWithCaseId() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"id\": \"123\", \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[0].value.stitchedDocument.document_url", notNullValue());
    }

    @Test
    public void testPostBundleStitchWithWordDoc() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundleWithWordDoc();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[0].value.stitchedDocument.document_url", notNullValue());
    }

    @Test
    public void testSpecificFilename() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName("my-file-name.pdf");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[0].value.fileName", equalTo("my-file-name.pdf"))
                .body("data.caseBundles[0].value.stitchedDocument.document_url", notNullValue());

    }

    @Test
    public void testFilenameWithoutExtension() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName("doc-file-name");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.stitchedDocument.document_filename", equalTo("doc-file-name.pdf"))
                .body("data.caseBundles[0].value.fileName", equalTo("doc-file-name"));
    }

    @Test
    public void testPostBundleStitchFileNameOneChar() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName("a");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", equalTo(Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG));
    }

    @Test
    public void testPostBundleStitchFileName51Char() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName(Constants.FILE_NAME_WITH_51_CHARS_LENGTH);

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", equalTo(Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG));
    }

    @Test
    public void testNoFileNameButBundleTitleOnly() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundleWithWordDoc();

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[0].value.stitchedDocument.document_filename", equalTo("Bundle title.pdf"));
    }

    @Test
    public void testFilenameErrors() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName("1234567890123456789012345678901%.pdf");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(400)
                .body("data.caseBundles[0].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[0].value.fileName", equalTo("1234567890123456789012345678901%.pdf"))
                .body("errors[0]", notNullValue());
    }

    @Test
    public void testLongBundleDescriptionErrors() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();

        bundle.setDescription("y".repeat(300));
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);
        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", notNullValue());
    }

    @Test
    public void testWithoutCoversheets() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setHasCoversheets(CcdBoolean.No);

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[0].value.hasCoversheets", equalTo("No"))
                .body("data.caseBundles[0].value.stitchedDocument.document_url", notNullValue());
    }

    @Test
    public void testWithImageRendering() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundleWithImageRendered();
        bundle.setHasCoversheets(CcdBoolean.No);

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

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
    public void testPostBundleStitchAsync() throws IOException, InterruptedException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postAsyncStitchCCDBundle(wrappedJson);
        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        response = testUtil.poll(documentTaskId);
        response
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Bundle title"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    public void testPostAsyncBundleStitchFileNameOneChar() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName("a");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postAsyncStitchCCDBundle(wrappedJson);

        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", equalTo(Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG));
    }

    @Test
    public void testPostAsyncLongBundleDescriptionErrors() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();

        bundle.setDescription("y".repeat(300));
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postAsyncStitchCCDBundle(wrappedJson);
        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", notNullValue());
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
