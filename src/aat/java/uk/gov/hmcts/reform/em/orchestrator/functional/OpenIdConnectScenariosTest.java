package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OpenIdConnectScenariosTest extends BaseTest {

    public static final String API_STITCH_CCD_BUNDLES = "/api/stitch-ccd-bundles";

    @Autowired
    protected OpenIdConnectScenariosTest(
            TestUtil testUtil,
            ExtendedCcdHelper extendedCcdHelper
    ) {
        super(testUtil, extendedCcdHelper);
    }


    @Test
    void testValidAuthenticationAndAuthorisation() throws IOException {
        assumeFalse(enableCdamValidation);
        String wrappedJson = caseBundleJsonPayload();

        Response response =
                testUtil
                        .authRequest()
                        .baseUri(testUtil.getTestUrl())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body(wrappedJson)
                        .post(API_STITCH_CCD_BUNDLES);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testCdamValidAuthenticationAndAuthorisation() throws IOException {
        assumeTrue(enableCdamValidation);
        String wrappedJson = cdamBundleJsonPayload();

        Response response =
                testUtil
                        .cdamAuthRequest()
                        .baseUri(testUtil.getTestUrl())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body(wrappedJson)
                        .post(API_STITCH_CCD_BUNDLES);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    // Invalid S2SAuth
    void testInvalidS2SAuth() throws IOException {
        String wrappedJson = caseBundleJsonPayload();

        Response response =
                testUtil
                        .invalidS2SAuth()
                        .baseUri(testUtil.getTestUrl())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body(wrappedJson)
                        .post(API_STITCH_CCD_BUNDLES);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    // Invalid S2SAuth
    void testWithInvalidIdamAuth() throws IOException {
        String wrappedJson = caseBundleJsonPayload();

        Response response =
                testUtil
                        .invalidIdamAuthrequest()
                        .baseUri(testUtil.getTestUrl())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body(wrappedJson)
                        .post(API_STITCH_CCD_BUNDLES);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    // Empty IdamAuth and Valid S2S Auth
    void testWithEmptyIdamAuthAndValidS2SAuth() throws IOException {
        String wrappedJson = caseBundleJsonPayload();

        Response response = testUtil.validS2SAuthWithEmptyIdamAuth()
            .baseUri(testUtil.getTestUrl())
            .contentType(APPLICATION_JSON_VALUE)
            .body(wrappedJson)
            .post(API_STITCH_CCD_BUNDLES);
        assertEquals(401, response.getStatusCode());

    }

    @Test
    // Empty IdamAuth and Empty S2SAuth
    void testIdamAuthAndS2SAuthAreEmpty() throws IOException {
        String wrappedJson = caseBundleJsonPayload();

        Response response = testUtil.emptyIdamAuthAndEmptyS2SAuth()
            .baseUri(testUtil.getTestUrl())
            .contentType(APPLICATION_JSON_VALUE)
            .body(wrappedJson)
            .post(API_STITCH_CCD_BUNDLES);
        assertEquals(401, response.getStatusCode());

    }

    private String caseBundleJsonPayload() throws JsonProcessingException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        return String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);
    }

    private String cdamBundleJsonPayload() throws JsonProcessingException {
        CcdBundleDTO bundle = testUtil.getCdamTestBundle(extendedCcdHelper.getBundleTesterUser());
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String caseDetails = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);
        return testUtil.addCdamProperties(caseDetails);
    }

}

