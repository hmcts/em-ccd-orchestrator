package uk.gov.hmcts.reform.em.orchestrator.stitching;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CdamDto;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.TaskState;
import uk.gov.hmcts.reform.em.orchestrator.stitching.mapper.StitchingDTOMapper;
import uk.gov.hmcts.reform.em.orchestrator.util.HttpOkResponseCloser;
import uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities;

import java.io.IOException;

import static uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities.ensurePdfExtension;

/**
 * Communicates with the Stitching API in order to turn a bundle into a stitched document.
 */
@SuppressWarnings("squid:S2139")
public class StitchingService {

    private final Logger logger = LoggerFactory.getLogger(StitchingService.class);

    private static final int SLEEP_TIME = 1000;
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final StitchingDTOMapper dtoMapper;
    private final OkHttpClient http;
    private final String documentTaskEndpoint;
    private final AuthTokenGenerator authTokenGenerator;
    private final int maxRetries;
    private static final String STITCHED_DOC_URI = "$.bundle.stitchedDocumentURI";
    private static final String TASK_STATE = "$.taskState";
    private static final String FAILURE_MSG = "Failed Calling Stitching Service for caseId : %s had issue : %s ";
    private static final String SUCCESS_MSG =
            "Successfully Called Stitching Service for caseId : %s with documentTaskId : %s ";

    public StitchingService(StitchingDTOMapper dtoMapper, OkHttpClient http, String documentTaskEndpoint,
                            AuthTokenGenerator authTokenGenerator, int maxRetries) {
        this.dtoMapper = dtoMapper;
        this.http = http;
        this.documentTaskEndpoint = documentTaskEndpoint;
        this.authTokenGenerator = authTokenGenerator;
        this.maxRetries = maxRetries;
    }

    /**
     * This method creates a document task in the stitching API and polls until it is complete.
     * If the document was succesfully
     * stitched the new document ID from DM store will be returned, otherwise an exception is thrown.
     */
    public CcdDocument stitch(CcdBundleDTO bundleDto, CdamDto cdamDto) throws InterruptedException  {
        final StitchingBundleDTO bundle = dtoMapper.toStitchingDTO(bundleDto);
        final DocumentTaskDTO documentTask = new DocumentTaskDTO();
        documentTask.setBundle(bundle);
        documentTask.setJwt(cdamDto.getJwt());
        documentTask.setCaseTypeId(cdamDto.getCaseTypeId());
        documentTask.setJurisdictionId(cdamDto.getJurisdictionId());
        documentTask.setCaseId(cdamDto.getCaseId());
        documentTask.setServiceAuth(cdamDto.getServiceAuth());

        logger.info(String.format("Calling Stitching Service for caseId : %s ",
                StringUtilities.convertValidLog(cdamDto.getCaseId())));
        try {
            final DocumentTaskDTO createdDocumentTaskDTO = startStitchingTask(documentTask);
            final String response = poll(createdDocumentTaskDTO.getId(), cdamDto.getJwt());
            final DocumentContext json = JsonPath
                .using(Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL))
                .parse(response);

            if (JsonPath.read(response, TASK_STATE).equals(TaskState.DONE.toString())) {
                final String fileName = json.read("$.bundle.fileName");

                final String hashToken = json.read("$.bundle.hashToken");

                if (StringUtils.isNotBlank(hashToken)) {
                    return new CcdDocument(
                        json.read(STITCHED_DOC_URI),
                        ensurePdfExtension(fileName),
                        uriWithBinarySuffix(json.read(STITCHED_DOC_URI)),
                        hashToken);
                } else {
                    return new CcdDocument(
                        json.read(STITCHED_DOC_URI),
                        ensurePdfExtension(fileName),
                        uriWithBinarySuffix(json.read(STITCHED_DOC_URI)));
                }

            } else {
                logger.error(String.format(FAILURE_MSG, StringUtilities.convertValidLog(cdamDto.getCaseId()),
                        StringUtilities.convertValidLog(json.read("$.failureDescription"))));
                throw new StitchingServiceException(
                        "Stitching failed: " + json.read("$.failureDescription"));
            }
        } catch (IOException e) {
            logger.error(String.format(FAILURE_MSG, StringUtilities.convertValidLog(cdamDto.getCaseId()),
                    StringUtilities.convertValidLog(e.getMessage())));
            throw new StitchingServiceException(
                    String.format("Unable to stitch bundle using %s: %s", documentTaskEndpoint, e.getMessage()), e);
        }
    }

    private static String uriWithBinarySuffix(String s) {
        return s.endsWith("/binary") ? s : s + "/binary";
    }

    public DocumentTaskDTO startStitchingTask(DocumentTaskDTO documentTask) throws IOException {

        final String json = jsonMapper.writeValueAsString(documentTask);
        final RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        final Request request = new Request.Builder()
                .addHeader("Authorization", documentTask.getJwt())
                .addHeader("ServiceAuthorization", authTokenGenerator.generate())
                .url(documentTaskEndpoint)
                .method("POST", body)
                .build();

        logger.debug("calling documentTaskEndpoint {}, body {} ", documentTaskEndpoint, json);
        Response response = null;
        try {
            response = http.newCall(request).execute();

            if (response.isSuccessful()) {
                DocumentTaskDTO documentTaskDTO = jsonMapper.readValue(
                    response.body().byteStream(), DocumentTaskDTO.class);
                logger.info(
                        String.format(SUCCESS_MSG, StringUtilities.convertValidLog(documentTask.getCaseId()),
                                StringUtilities.convertValidLog(documentTaskDTO.getId().toString())));
                return documentTaskDTO;
            } else {
                String responseBody = response.body().string();
                logger.error(String.format(FAILURE_MSG, StringUtilities.convertValidLog(documentTask.getCaseId()),
                        StringUtilities.convertValidLog(responseBody)));
                throw new StitchingServiceException("Unable to create stitching task: " + responseBody);
            }
        } finally {
            HttpOkResponseCloser.closeResponse(response);
        }
    }

    private String poll(long taskId, String jwt) throws IOException, InterruptedException {
        final Request request = new Request.Builder()
            .addHeader("Authorization", jwt)
            .addHeader("ServiceAuthorization", authTokenGenerator.generate())
            .url(documentTaskEndpoint + "/" + taskId)
            .get()
            .build();
        Response response = null;
        int sleepTime = SLEEP_TIME;
        try {
            for (int i = 0; i < maxRetries; i++) {
                response = http.newCall(request).execute();
                final String responseBody = response.body().string();
                final String taskState = JsonPath.read(responseBody, TASK_STATE);

                if (!taskState.equals(TaskState.NEW.toString())
                    && (!taskState.equals(TaskState.IN_PROGRESS.toString()))) {
                    return responseBody;
                } else {
                    Thread.sleep(sleepTime);
                    sleepTime += SLEEP_TIME * (i + 2);
                }
                response.close();
            }
        } finally {
            HttpOkResponseCloser.closeResponse(response);
        }
        throw new StitchingTaskMaxRetryException(String.valueOf(taskId));
    }

}
