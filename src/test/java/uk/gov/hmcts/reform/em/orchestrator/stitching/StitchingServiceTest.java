package uk.gov.hmcts.reform.em.orchestrator.stitching;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CdamDto;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.mapper.StitchingDTOMapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StitchingServiceTest {

    private String stitchingBaseUrl = "http://localhost:4630";

    private String stitchingResource = "/api/document-tasks/";

    private static final CdamDto cdamDto = CdamDto.builder().jwt("token").caseId("123456789").build();

    @Test
    void stitchSuccessful() throws StitchingServiceException, InterruptedException {
        List<String> responses = new ArrayList<>();

        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"DONE\", \"bundle\": { \"stitchedDocumentURI\": \"AAAAAA\" } }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        CcdDocument docId = service.stitch(new CcdBundleDTO(), cdamDto);

        assertEquals("AAAAAA", docId.getUrl());
        assertEquals("stitched.pdf", docId.getFileName());
    }

    @Test
    void stitchCdamSuccessful() throws StitchingServiceException, InterruptedException {
        List<String> responses = new ArrayList<>();

        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"DONE\", \"bundle\": { \"stitchedDocumentURI\": \"AAAAAA\", "
            + "\"hashToken\": \"2355678kggkhghgjhvhmgv345678\" } }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        CcdDocument docId = service.stitch(new CcdBundleDTO(), cdamDto);

        assertEquals("AAAAAA", docId.getUrl());
        assertEquals("2355678kggkhghgjhvhmgv345678", docId.getHash());
        assertEquals("stitched.pdf", docId.getFileName());
    }

    @Test
    void stitchSuccessfulWithFileName() throws StitchingServiceException, InterruptedException {
        List<String> responses = new ArrayList<>();
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"DONE\", \"bundle\": { \"stitchedDocumentURI\": \"AAAAAA\","
            + " \"fileName\": \"Dummy.Bundle.01.docx\" } }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        CcdDocument docId = service.stitch(new CcdBundleDTO(), cdamDto);

        assertEquals("AAAAAA", docId.getUrl());
        assertEquals("Dummy.Bundle.01.docx", docId.getFileName());
    }

    @Test
    void stitchSuccessfulWithFileNameWithoutSuffix() throws StitchingServiceException, InterruptedException {
        List<String> responses = new ArrayList<>();
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"DONE\", \"bundle\": { \"stitchedDocumentURI\": \"AAAAAA\""
            + ", \"fileName\": \"Dummy-Bundle-01\" } }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        CcdDocument docId = service.stitch(new CcdBundleDTO(), cdamDto);

        assertEquals("AAAAAA", docId.getUrl());
        assertEquals("Dummy-Bundle-01.pdf", docId.getFileName());
    }

    @Test
    void stitchSuccessfulWithEmptyFileName() throws StitchingServiceException, InterruptedException {
        List<String> responses = new ArrayList<>();
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"DONE\", \"bundle\": { \"stitchedDocumentURI\": \"AAAAAA\""
            + ", \"fileName\": \"\" } }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        CcdDocument docId = service.stitch(new CcdBundleDTO(), cdamDto);

        assertEquals("AAAAAA", docId.getUrl());
        assertEquals("stitched.pdf", docId.getFileName());
    }

    @Test
    void stitchSuccessfulWithNullFileName() throws StitchingServiceException, InterruptedException {
        List<String> responses = new ArrayList<>();
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"DONE\", \"bundle\": { \"stitchedDocumentURI\": \"AAAAAA\"} }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        CcdDocument docId = service.stitch(new CcdBundleDTO(), cdamDto);

        assertEquals("AAAAAA", docId.getUrl());
        assertEquals("stitched.pdf", docId.getFileName());
    }

    @Test
    void stitchFailure() throws StitchingServiceException {
        List<String> responses = new ArrayList<>();
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"FAILED\", \"failureDescription\": \"Something went wrong\", "
            + "\"bundle\": { \"stitchedDocumentURI\": \"AAAAAA\", \"fileName\": \"a.pdf\" } }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        CcdBundleDTO bundleDTO = new CcdBundleDTO();
        assertThrows(StitchingServiceException.class, () -> service.stitch(bundleDTO, cdamDto));
    }

    @Test
    void stitchTimeout() throws StitchingServiceException {
        List<String> responses = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocumentURI: null } }");
        }

        responses.add("{ id: 1, taskState: 'DONE', bundle: { stitchedDocumentURI: 'AAAAAA' } }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        CcdBundleDTO bundleDTO = new CcdBundleDTO();
        assertThrows(StitchingServiceException.class, () -> service.stitch(bundleDTO, cdamDto));
    }

    @Test
    void doesAppendBinary() throws Exception {
        List<String> responses = new ArrayList<>();
        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);

        String testString = "testString";

        Method binarySuffixAdder = StitchingService.class.getDeclaredMethod("uriWithBinarySuffix", String.class);
        binarySuffixAdder.setAccessible(true);
        String processedString = (String) binarySuffixAdder.invoke(service, testString);

        assertEquals("testString/binary", processedString);
    }

    @Test
    void doesNotAppendBinary() throws Exception {
        List<String> responses = new ArrayList<>();
        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);

        String testString = "testString/binary";

        Method binarySuffixAdder = StitchingService.class.getDeclaredMethod("uriWithBinarySuffix", String.class);
        binarySuffixAdder.setAccessible(true);
        String processedString = (String) binarySuffixAdder.invoke(service, testString);

        assertEquals("testString/binary", processedString);
    }

    @Test
    void testCaseId() throws Exception {
        List<String> responses = new ArrayList<>();

        responses.add("{ \"id\": 2, \"taskState\": \"DONE\", \"bundle\": { \"stitchedDocumentURI\": \"AAAAAA\" }, "
                + "\"caseId\": " + cdamDto.getCaseId() + " }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        DocumentTaskDTO documentTaskDTO = new DocumentTaskDTO();
        documentTaskDTO.setCaseId(cdamDto.getCaseId());
        documentTaskDTO.setJwt(cdamDto.getJwt());

        DocumentTaskDTO documentTaskDTO1 = service.startStitchingTask(documentTaskDTO);
        assertEquals(documentTaskDTO1.getCaseId(), cdamDto.getCaseId());
        assertEquals(documentTaskDTO.getCaseId(), cdamDto.getCaseId());

    }

    @Test
    void stitchMaxRetryException() {
        List<String> responses = new ArrayList<>();
        String taskId = "1";
        responses.add(String.format("{ \"id\": %s, \"taskState\": \"NEW\", \"bundle\":"
            + " { \"stitchedDocumentURI\": null } }", taskId));

        int maxRetries = 5;
        for (int i = 0; i < maxRetries; i++) {
            responses.add(String.format("{ \"id\": %s, \"taskState\": \"IN_PROGRESS\","
                + " \"bundle\": { \"stitchedDocumentURI\": null } }", taskId));
        }

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        CcdBundleDTO bundleDTO = new CcdBundleDTO();

        StitchingTaskMaxRetryException exception = assertThrows(
            StitchingTaskMaxRetryException.class,
            () -> service.stitch(bundleDTO, cdamDto)
        );

        assertEquals(
            String.format("Task not complete after maximum number of retries for DocumentTaskId : %s", taskId),
            exception.getMessage()
        );
    }

    @Test
    void httpCallFailsThrowsStitchingServiceException() {
        String errorResponseBody = "{\"errorCode\":\"ST-001\",\"errorMessage\":\"Stitching service unavailable\"}";

        OkHttpClient mockHttp = new OkHttpClient.Builder()
            .addInterceptor(chain -> new Response.Builder()
                .body(ResponseBody.create(errorResponseBody, MediaType.get("application/json")))
                .request(chain.request())
                .message("Server Error")
                .code(500)
                .protocol(Protocol.HTTP_2)
                .build())
            .build();

        StitchingService service = getStitchingService(mockHttp);
        DocumentTaskDTO documentTaskDTO = new DocumentTaskDTO();
        documentTaskDTO.setJwt(cdamDto.getJwt());
        documentTaskDTO.setCaseId(cdamDto.getCaseId());

        StitchingServiceException exception = assertThrows(
            StitchingServiceException.class,
            () -> service.startStitchingTask(documentTaskDTO)
        );

        assertEquals("Unable to create stitching task: " + errorResponseBody, exception.getMessage());
    }

    public StitchingService getStitchingService(OkHttpClient http) {
        return new StitchingService(
            new StitchingDTOMapper(),
            http,
            stitchingBaseUrl + stitchingResource,
            () -> "ServiceAuthorizationToken",
            5
        );
    }

    public OkHttpClient getMockHttp(List<String> body) {
        Iterator<String> iterator = body.iterator();

        return new OkHttpClient
            .Builder()
            .addInterceptor(chain -> new Response.Builder()
                .body(ResponseBody.create(iterator.next(), MediaType.get("application/json")))
                .request(chain.request())
                .message("")
                .code(200)
                .protocol(Protocol.HTTP_2)
                .build())
            .build();
    }
}
