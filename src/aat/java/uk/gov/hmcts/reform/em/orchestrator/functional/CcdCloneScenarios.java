package uk.gov.hmcts.reform.em.orchestrator.functional;

import io.restassured.response.ValidatableResponse;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CcdCloneScenarios extends BaseTest {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Before
    public void setUp() throws Exception {
        Assume.assumeFalse(enableCdamValidation);
    }

    @Test
    public void testSingleBundleClone() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setEligibleForCloningAsBoolean(true);
        List<CcdValue<CcdBundleDTO>> list = new ArrayList<>();
        list.add(new CcdValue<>(bundle));
        String json = mapper.writeValueAsString(list);
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\": %s } } }", json);

        ValidatableResponse response = postCloneCCDBundle(wrappedJson);

        response
                .assertThat()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("CLONED_Bundle title"))
                .body("data.caseBundles[0].value.eligibleForCloning", equalTo("no"))
                .body("data.caseBundles[1].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[1].value.eligibleForCloning", equalTo("no"));
    }

    @Test
    public void testSingleBundleCloneWithCaseId() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setEligibleForCloningAsBoolean(true);
        List<CcdValue<CcdBundleDTO>> list = new ArrayList<>();
        list.add(new CcdValue<>(bundle));
        String json = mapper.writeValueAsString(list);
        String wrappedJson = String.format("{ \"id\": \"123\", \"case_details\":"
            + "{ \"case_data\":{ \"caseBundles\": %s } } }", json);

        ValidatableResponse response = postCloneCCDBundle(wrappedJson);

        response
                .assertThat()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("CLONED_Bundle title"))
                .body("data.caseBundles[0].value.eligibleForCloning", equalTo("no"))
                .body("data.caseBundles[1].value.title", equalTo("Bundle title"))
                .body("data.caseBundles[1].value.eligibleForCloning", equalTo("no"));
    }

    @Test
    public void testMultipleBundlesClone() throws IOException {
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

        ValidatableResponse response = postCloneCCDBundle(wrappedJson);

        response
                .assertThat()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("CLONED_Bundle 2"))
                .body("data.caseBundles[1].value.title", equalTo("Bundle 2"))
                .body("data.caseBundles[2].value.title", equalTo("Bundle 1"))
                .body("data.caseBundles[1].value.eligibleForCloning", equalTo("no"))
                .body("data.caseBundles[0].value.fileName", equalTo("CLONED_FilenameBundle2"));
    }

    private ValidatableResponse postCloneCCDBundle(String wrappedJson) {
        return testUtil
                .authRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .post("/api/clone-ccd-bundles")
                .then();
    }

}
