package uk.gov.hmcts.reform.em.stitching.functional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.stitching.domain.enumeration.TaskState;
import uk.gov.hmcts.reform.em.stitching.service.dto.BundleDTO;
import uk.gov.hmcts.reform.em.stitching.service.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.stitching.testutil.TestUtil;
import uk.gov.hmcts.reform.em.stitching.testutil.Env;

import java.io.IOException;

public class DocumentTaskScenarios {

    TestUtil testUtil = new TestUtil();

    @Test
    public void testPostBundleStitch() throws IOException {
        BundleDTO bundle = testUtil.getTestBundle();

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(convertObjectToJsonBytes(bundle))
                .request("POST", Env.getTestUrl() + "/api/stitched-bundle");

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertNotNull( response.getBody().jsonPath().getString("stitchedDocId"));
    }

    @Test
    public void testPostBundleStitchWithWordDoc() throws IOException {
        BundleDTO bundle = testUtil.getTestBundleWithWordDoc();

        Response response = testUtil.authRequest()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(convertObjectToJsonBytes(bundle))
            .request("POST", Env.getTestUrl() + "/api/stitched-bundle");

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertNotNull( response.getBody().jsonPath().getString("stitchedDocId"));
    }

    @Test
    public void testPostDocumentTask() throws IOException {
        BundleDTO bundle = testUtil.getTestBundle();
        DocumentTaskDTO documentTask = new DocumentTaskDTO();

        documentTask.setBundle(bundle);

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(convertObjectToJsonBytes(documentTask))
                .request("POST", Env.getTestUrl() + "/api/document-tasks");

        Assert.assertEquals(201, response.getStatusCode());
        Assert.assertEquals( response.getBody().jsonPath().getString("taskState"), TaskState.NEW.toString());
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
