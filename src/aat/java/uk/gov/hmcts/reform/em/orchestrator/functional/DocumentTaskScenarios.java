package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.response.Response;
import jodd.json.JsonArray;
import jodd.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.orchestrator.service.TaskState;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;
import uk.gov.hmcts.reform.em.orchestrator.testutil.Env;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DocumentTaskScenarios {

    TestUtil testUtil = new TestUtil();

    @Test
    public void testPostBundleStitch() throws IOException {
        BundleDTO bundle = testUtil.getTestBundle();
        List<BundleDTO> bundles = new ArrayList<>();
        bundles.add(bundle);

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(convertObjectToJsonBytes(bundles))
                .request("POST", Env.getTestUrl() + "/api/stitch-cdd-bundles");

        Assert.assertEquals(200, response.getStatusCode());
        String body = response.getBody().prettyPrint();
        Assert.assertNotNull(response.getBody().jsonPath().getString("$[0].stitchedDocId"));
    }

    @Test
    public void testPostBundleStitchWithWordDoc() throws IOException {
        BundleDTO bundle = testUtil.getTestBundleWithWordDoc();

        Response response = testUtil.authRequest()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(convertObjectToJsonBytes(bundle))
            .request("POST", Env.getTestUrl() + "/api/stitch-cdd-bundles");

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertNotNull( response.getBody().jsonPath().getString("stitchedDocId"));
    }


    /**
     * Convert an object to JSON byte array.
     *
     * @param object
     *            the object to convert
     * @return the JSON byte array
     * @throws IOException
     */
    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);

        return mapper.writeValueAsBytes(object);
    }

}
