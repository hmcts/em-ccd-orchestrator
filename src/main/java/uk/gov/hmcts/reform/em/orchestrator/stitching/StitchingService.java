package uk.gov.hmcts.reform.em.orchestrator.stitching;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;
import okhttp3.*;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.core.user.UserResolver;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.em.orchestrator.service.TaskState;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.DocumentTaskDTO;

import java.io.IOException;

/**
 * Communicates with the Stitching API in order to turn a bundle into a stitched document.
 */
public class StitchingService {

    private static final int MAX_RETRIES = 10;
    private static final int SLEEP_TIME = 500;
    private final OkHttpClient http;
    private final AuthTokenGenerator tokenGenerator;
    private final UserResolver userResolver;
    private final String documentTaskEndpoint;

    public StitchingService(OkHttpClient http, AuthTokenGenerator tokenGenerator, UserResolver userResolver, String documentTaskEndpoint) {
        this.http = http;
        this.tokenGenerator = tokenGenerator;
        this.userResolver = userResolver;
        this.documentTaskEndpoint = documentTaskEndpoint;
    }

    /**
     * This method creates a document task in the stitching API and polls until it is complete. If the document was succesfully
     * stitched the new document ID from DM store will be returned, otherwise an exception is thrown.
     */
    public String stitch(BundleDTO bundle, String jwt) throws StitchingServiceException, InterruptedException {
        final DocumentTaskDTO documentTask = new DocumentTaskDTO();
        documentTask.setBundle(bundle);
        documentTask.setJwt(jwt);

        try {
            final String userId = getUserId(jwt);
            final int taskId = post(documentTask, userId);
            final String response = poll(taskId, userId);


            if (JsonPath.read(response, "$.taskState").equals(TaskState.DONE.toString())) {
                return JsonPath.read(response, "$.bundle.stitchedDocId");
            } else {
                throw new StitchingServiceException("Stitching failed: " + JsonPath.read(response, "$.failureDescription"));
            }
        }
        catch (IOException e) {
            throw new StitchingServiceException("Unable to stitch bundle", e);
        }
    }

    private int post(DocumentTaskDTO documentTask, String userId) throws IOException {
        final RequestBody body = RequestBody.create(MediaType.get("application/json"), getJson(documentTask));
        final Request request = new Request.Builder()
            .addHeader("user-id", userId)
            .addHeader("user-roles", "caseworker")
            .addHeader("ServiceAuthorization", tokenGenerator.generate())
            .url(documentTaskEndpoint)
            .method("POST", body)
            .build();

        final Response response = http.newCall(request).execute();

        if (response.isSuccessful()) {
            return JsonPath.read(response.body().string(), "$.id");
        } else {
            throw new IOException("Unable to stitching task");
        }
    }

    private String getUserId(String jwt) {
        final User user = userResolver.getTokenDetails(jwt);

        return user.getPrincipal();
    }

    private String getJson(Object object) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return mapper.writeValueAsString(object);
    }

    private String poll(int taskId, String userId) throws IOException, InterruptedException {
        final Request request = new Request.Builder()
            .addHeader("user-id", userId)
            .addHeader("user-roles", "caseworker")
            .addHeader("ServiceAuthorization", tokenGenerator.generate())
            .url(documentTaskEndpoint + "/" + taskId)
            .get()
            .build();

        for (int i = 0; i < MAX_RETRIES; i++) {
            final Response response = http.newCall(request).execute();
            final String responseBody = response.body().string();
            final String taskState = JsonPath.read(responseBody, "$.taskState");

            if (!taskState.equals(TaskState.NEW.toString())) {
                return responseBody;
            } else {
                Thread.sleep(SLEEP_TIME);
            }
        }

        throw new IOException("Task not complete after maximum number of retries");
    }
}
