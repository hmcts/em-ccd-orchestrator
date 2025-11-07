package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class SecureCcdCloneScenariosTest extends BaseTest {

    public static final String DATA_CASE_BUNDLES_0_VALUE_TITLE = "data.caseBundles[0].value.title";
    public static final String DATA_CASE_BUNDLES_1_VALUE_TITLE = "data.caseBundles[1].value.title";
    public static final String DATA_CASE_BUNDLES_1_VALUE_ELIGIBLE_FOR_CLONING =
            "data.caseBundles[1].value.eligibleForCloning";

    @Autowired
    protected SecureCcdCloneScenariosTest(
            TestUtil testUtil,
            ExtendedCcdHelper extendedCcdHelper
    ) {
        super(testUtil, extendedCcdHelper);
    }

    @BeforeEach
    public void setUp() {
        assumeTrue(enableCdamValidation);
    }

    @Test
    void testSingleBundleClone() throws JsonProcessingException {
        CcdBundleDTO bundle = testUtil.getCdamTestBundle(extendedCcdHelper.getBundleTesterUser());
        bundle.setEligibleForCloningAsBoolean(true);
        List<CcdValue<CcdBundleDTO>> list = new ArrayList<>();
        list.add(new CcdValue<>(bundle));
        String json = mapper.writeValueAsString(list);
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\": %s } } }", json);
        String cdamJson = testUtil.addCdamProperties(wrappedJson);

        ValidatableResponse response = postCloneCCDBundle(cdamJson);

        response
                .assertThat()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_TITLE, equalTo("CLONED_Bundle title"))
                .body("data.caseBundles[0].value.eligibleForCloning", equalTo("no"))
                .body(DATA_CASE_BUNDLES_1_VALUE_TITLE, equalTo("Bundle title"))
                .body(DATA_CASE_BUNDLES_1_VALUE_ELIGIBLE_FOR_CLONING, equalTo("no"));
    }

    @Test
    void testSingleBundleCloneWithCaseId() throws JsonProcessingException {
        CcdBundleDTO bundle = testUtil.getCdamTestBundle(extendedCcdHelper.getBundleTesterUser());
        bundle.setEligibleForCloningAsBoolean(true);
        List<CcdValue<CcdBundleDTO>> list = new ArrayList<>();
        list.add(new CcdValue<>(bundle));
        String json = mapper.writeValueAsString(list);
        String wrappedJson =
            String.format("{ \"id\": \"123\", \"case_details\":{ \"case_data\":{ \"caseBundles\": %s } } }", json);
        String cdamJson = testUtil.addCdamProperties(wrappedJson);
        ValidatableResponse response = postCloneCCDBundle(cdamJson);

        response
                .assertThat()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_TITLE, equalTo("CLONED_Bundle title"))
                .body("data.caseBundles[0].value.eligibleForCloning", equalTo("no"))
                .body(DATA_CASE_BUNDLES_1_VALUE_TITLE, equalTo("Bundle title"))
                .body(DATA_CASE_BUNDLES_1_VALUE_ELIGIBLE_FOR_CLONING, equalTo("no"));
    }

    @Test
    void testMultipleBundlesClone() throws IOException {
        CcdBundleDTO bundle1 = testUtil.getTestBundle();
        bundle1.setTitle("Bundle 1");

        CcdBundleDTO bundle2 = testUtil.getTestBundle();
        bundle2.setTitle("Bundle 2");
        bundle2.setFileName("FilenameBundle2");
        bundle2.setEligibleForCloningAsBoolean(true);

        List<CcdValue<CcdBundleDTO>> list = new ArrayList<>();
        list.add(new CcdValue<>(bundle1));
        list.add(new CcdValue<>(bundle2));

        String jsonList = mapper.writeValueAsString(list);
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\": %s  } } }", jsonList);
        String cdamJson = testUtil.addCdamProperties(wrappedJson);
        ValidatableResponse response = postCloneCCDBundle(cdamJson);


        response
                .assertThat()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_TITLE, equalTo("CLONED_Bundle 2"))
                .body(DATA_CASE_BUNDLES_1_VALUE_TITLE, equalTo("Bundle 2"))
                .body("data.caseBundles[2].value.title", equalTo("Bundle 1"))
                .body(DATA_CASE_BUNDLES_1_VALUE_ELIGIBLE_FOR_CLONING, equalTo("no"))
                .body("data.caseBundles[0].value.fileName", equalTo("CLONED_FilenameBundle2"));
    }

    private ValidatableResponse postCloneCCDBundle(String wrappedJson) {
        return testUtil
                .cdamAuthRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .post("/api/clone-ccd-bundles")
                .then();
    }

}
