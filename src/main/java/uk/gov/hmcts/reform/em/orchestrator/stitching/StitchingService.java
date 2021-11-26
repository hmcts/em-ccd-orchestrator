package uk.gov.hmcts.reform.em.orchestrator.stitching;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.em.orchestrator.config.Constants;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CdamDetailsDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.TaskState;
import uk.gov.hmcts.reform.em.orchestrator.stitching.mapper.StitchingDTOMapper;
import uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;

import static uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities.ensurePdfExtension;

/**
 * Communicates with the Stitching API in order to turn a bundle into a stitched document.
 */
@SuppressWarnings("squid:S2139")
public class StitchingService {

    private final Logger logger = LoggerFactory.getLogger(StitchingService.class);

    private static final int DEFAULT_MAX_RETRIES = 200;
    private static final int SLEEP_TIME = 500;
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final StitchingDTOMapper dtoMapper;
    private final OkHttpClient http;
    private final String documentTaskEndpoint;
    private final AuthTokenGenerator authTokenGenerator;
    private final int maxRetries;

    private static final String STITCHED_DOC_URI = "$.bundle.stitchedDocumentURI";
    private static final String TASK_STATE = "$.taskState";
    private static final String FAILURE_MSG = "Calling Stitching Service for caseId : %s had issue : %s ";
    private static final String SUCCESS_MSG =
            "Calling Stitching Service for caseId : %s was completed with documentTaskId : %s ";

    public StitchingService(StitchingDTOMapper dtoMapper, OkHttpClient http, String documentTaskEndpoint,
                            AuthTokenGenerator authTokenGenerator) {
        this(dtoMapper, http, documentTaskEndpoint, authTokenGenerator, DEFAULT_MAX_RETRIES);
    }

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
    public CcdDocument stitch(CcdBundleDTO bundleDto, String jwt, String caseId) throws InterruptedException  {
        final StitchingBundleDTO bundle = dtoMapper.toStitchingDTO(bundleDto);
        final DocumentTaskDTO documentTask = new DocumentTaskDTO();
        documentTask.setBundle(bundle);
        documentTask.setJwt(jwt);

        logger.info(String.format("Calling Stitching Service for caseId : %s ",
                StringUtilities.convertValidLog(caseId)));
        try {
            final DocumentTaskDTO createdDocumentTaskDTO = startStitchingTask(documentTask, jwt, caseId);
            final String response = poll(createdDocumentTaskDTO.getId(), jwt);
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
                logger.error(String.format(FAILURE_MSG, StringUtilities.convertValidLog(caseId),
                        StringUtilities.convertValidLog(json.read("$.failureDescription"))));
                throw new StitchingServiceException(
                        "Stitching failed: " + json.read("$.failureDescription"));
            }
        } catch (IOException e) {
            logger.error(String.format(FAILURE_MSG, StringUtilities.convertValidLog(caseId),
                    StringUtilities.convertValidLog(e.getMessage())));
            throw new StitchingServiceException(
                    String.format("Unable to stitch bundle using %s: %s", documentTaskEndpoint, e.getMessage()), e);
        }
    }

    private static String uriWithBinarySuffix(String s) {
        return s.endsWith("/binary") ? s : s + "/binary";
    }

    public DocumentTaskDTO startStitchingTask(DocumentTaskDTO documentTask, String jwt, String caseId) throws IOException {
        populateCdamDetails(documentTask);
        final String json = jsonMapper.writeValueAsString(documentTask);
        final RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        final Request request = new Request.Builder()
            .addHeader("Authorization", jwt)
            .addHeader("ServiceAuthorization", authTokenGenerator.generate())
            .url(documentTaskEndpoint)
            .method("POST", body)
            .build();

        final Response response = http.newCall(request).execute();

        if (response.isSuccessful()) {
            DocumentTaskDTO documentTaskDTO =  jsonMapper.readValue(response.body().byteStream(), DocumentTaskDTO.class);
            logger.info(
                    String.format(SUCCESS_MSG, StringUtilities.convertValidLog(caseId),
                    StringUtilities.convertValidLog(documentTaskDTO.getId().toString())));
            return documentTaskDTO;
        } else {
            logger.error(String.format(FAILURE_MSG, StringUtilities.convertValidLog(caseId),
                    StringUtilities.convertValidLog(response.body().string())));
            throw new IOException("Unable to create stitching task: " + response.body().string());
        }
    }

    private String poll(long taskId, String jwt) throws IOException, InterruptedException {
        final Request request = new Request.Builder()
            .addHeader("Authorization", jwt)
            .addHeader("ServiceAuthorization", authTokenGenerator.generate())
            .url(documentTaskEndpoint + taskId)
            .get()
            .build();

        for (int i = 0; i < maxRetries; i++) {
            final Response response = http.newCall(request).execute();
            final String responseBody = response.body().string();
            final String taskState = JsonPath.read(responseBody, TASK_STATE);

            if (!taskState.equals(TaskState.NEW.toString())) {
                return responseBody;
            } else {
                Thread.sleep(SLEEP_TIME);
            }
        }

        throw new IOException("Task not complete after maximum number of retries");
    }

    private void populateCdamDetails(DocumentTaskDTO documentTaskDto) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        if (Objects.nonNull(request.getSession().getAttribute(Constants.CDAM_DEATILS))) {
            CdamDetailsDto cdamDetailsDto = (CdamDetailsDto) request.getSession().getAttribute(Constants.CDAM_DEATILS);
            documentTaskDto.setServiceAuth(cdamDetailsDto.getServiceAuth());
            documentTaskDto.setCaseTypeId(cdamDetailsDto.getCaseTypeId());
            documentTaskDto.setJurisdictionId(cdamDetailsDto.getJurisdictionId());
            request.getSession().removeAttribute(Constants.CDAM_DEATILS);
        }
    }
}
