package uk.gov.hmcts.reform.em.orchestrator.functional;

import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class CcdCloneScenariosTest extends BaseTest {

    @BeforeEach
    public void setUp() {
        assumeFalse(enableCdamValidation);
    }

    @Test
    void testSingleBundleClone() throws IOException {
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
    void testSingleBundleCloneWithCaseId() throws IOException {
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
