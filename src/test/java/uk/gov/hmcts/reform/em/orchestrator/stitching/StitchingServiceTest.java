package uk.gov.hmcts.reform.em.orchestrator.stitching;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Assert;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CdamDto;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.mapper.StitchingDTOMapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StitchingServiceTest {

    private String stitchingBaseUrl = "http://localhost:4630";

    private String stitchingResource = "/api/document-tasks/";

    private static final CdamDto cdamDto = CdamDto.builder().jwt("token").caseId("123456789").build();

    @Test
    public void stitchSuccessful() throws StitchingServiceException, InterruptedException {
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

        Assert.assertEquals("AAAAAA", docId.getUrl());
        Assert.assertEquals("stitched.pdf", docId.getFileName());
    }

    @Test
    public void stitchCdamSuccessful() throws StitchingServiceException, InterruptedException {
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

        Assert.assertEquals("AAAAAA", docId.getUrl());
        Assert.assertEquals("2355678kggkhghgjhvhmgv345678", docId.getHash());
        Assert.assertEquals("stitched.pdf", docId.getFileName());
    }

    @Test
    public void stitchSuccessfulWithFileName() throws StitchingServiceException, InterruptedException {
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

        Assert.assertEquals("AAAAAA", docId.getUrl());
        Assert.assertEquals("Dummy.Bundle.01.docx", docId.getFileName());
    }

    @Test
    public void stitchSuccessfulWithFileNameWithoutSuffix() throws StitchingServiceException, InterruptedException {
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

        Assert.assertEquals("AAAAAA", docId.getUrl());
        Assert.assertEquals("Dummy-Bundle-01.pdf", docId.getFileName());
    }

    @Test
    public void stitchSuccessfulWithEmptyFileName() throws StitchingServiceException, InterruptedException {
        List<String> responses = new ArrayList<>();
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"DONE\", \"bundle\": { \"stitchedDocumentURI\": \"AAAAAA\""
            + ", \"fileName\": \"\" } }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        CcdDocument docId = service.stitch(new CcdBundleDTO(), cdamDto);

        Assert.assertEquals("AAAAAA", docId.getUrl());
        Assert.assertEquals("stitched.pdf", docId.getFileName());
    }

    @Test
    public void stitchSuccessfulWithNullFileName() throws StitchingServiceException, InterruptedException {
        List<String> responses = new ArrayList<>();
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"DONE\", \"bundle\": { \"stitchedDocumentURI\": \"AAAAAA\"} }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        CcdDocument docId = service.stitch(new CcdBundleDTO(), cdamDto);

        Assert.assertEquals("AAAAAA", docId.getUrl());
        Assert.assertEquals("stitched.pdf", docId.getFileName());
    }

    @Test(expected = StitchingServiceException.class)
    public void stitchFailure() throws StitchingServiceException, InterruptedException {
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
        service.stitch(new CcdBundleDTO(), cdamDto);
    }

    @Test(expected = StitchingServiceException.class)
    public void stitchTimeout() throws StitchingServiceException, InterruptedException {
        List<String> responses = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocumentURI: null } }");
        }

        responses.add("{ id: 1, taskState: 'DONE', bundle: { stitchedDocumentURI: 'AAAAAA' } }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        service.stitch(new CcdBundleDTO(), cdamDto);
    }

    @Test
    public void doesAppendBinary() throws Exception {
        List<String> responses = new ArrayList<>();
        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);

        String testString = "testString";

        Method binarySuffixAdder = StitchingService.class.getDeclaredMethod("uriWithBinarySuffix", String.class);
        binarySuffixAdder.setAccessible(true);
        String processedString = (String) binarySuffixAdder.invoke(service, testString);

        Assert.assertEquals("testString/binary", processedString);
    }

    @Test
    public void doesNotAppendBinary() throws Exception {
        List<String> responses = new ArrayList<>();
        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);

        String testString = "testString/binary";

        Method binarySuffixAdder = StitchingService.class.getDeclaredMethod("uriWithBinarySuffix", String.class);
        binarySuffixAdder.setAccessible(true);
        String processedString = (String) binarySuffixAdder.invoke(service, testString);

        Assert.assertEquals("testString/binary", processedString);
    }

    @Test
    public void testCaseId() throws Exception {
        List<String> responses = new ArrayList<>();

        responses.add("{ \"id\": 2, \"taskState\": \"DONE\", \"bundle\": { \"stitchedDocumentURI\": \"AAAAAA\" }, "
                + "" + "\"caseId\": " + cdamDto.getCaseId() + " }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        DocumentTaskDTO documentTaskDTO = new DocumentTaskDTO();
        documentTaskDTO.setCaseId(cdamDto.getCaseId());
        documentTaskDTO.setJwt(cdamDto.getJwt());

        DocumentTaskDTO documentTaskDTO1 = service.startStitchingTask(documentTaskDTO);
        Assert.assertEquals(documentTaskDTO1.getCaseId(), cdamDto.getCaseId());
        Assert.assertEquals(documentTaskDTO.getCaseId(), cdamDto.getCaseId());

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
                .body(ResponseBody.create(MediaType.get("application/json"), iterator.next()))
                .request(chain.request())
                .message("")
                .code(200)
                .protocol(Protocol.HTTP_2)
                .build())
            .build();
    }
}
