package uk.gov.hmcts.reform.em.orchestrator.stitching;

import okhttp3.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.core.user.UserResolver;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleDTO;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class StitchingServiceTest {

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private UserResolver userResolver;

    @Value("${em-rpa-stitching-api.base-url}")
    private String stitchingBaseUrl;

    @Value("${em-rpa-stitching-api.resource}")
    private String stitchingResource;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        BDDMockito.given(authTokenGenerator.generate()).willReturn("s2s");
        BDDMockito.given(userResolver.getTokenDetails(any())).willReturn(new User("id", null));
    }

    @Test
    public void stitchSuccessful() throws StitchingServiceException, InterruptedException {
        List<String> responses = new ArrayList<>();
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'DONE', bundle: { stitchedDocId: 'AAAAAA' } }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        String docId = service.stitch(new BundleDTO(), "token");

        Assert.assertEquals(docId, "AAAAAA");
    }

    @Test(expected = StitchingServiceException.class)
    public void stitchFailure() throws StitchingServiceException, InterruptedException {
        List<String> responses = new ArrayList<>();
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'FAILED', failureDescription: 'Docmosis failure', bundle: { stitchedDocId: null } }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        service.stitch(new BundleDTO(), "token");
    }

    @Test(expected = StitchingServiceException.class)
    public void stitchTimeout() throws StitchingServiceException, InterruptedException {
        List<String> responses = new ArrayList<>();
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'NEW', bundle: { stitchedDocId: null } }");
        responses.add("{ id: 1, taskState: 'DONE', bundle: { stitchedDocId: 'AAAAAA' } }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        service.stitch(new BundleDTO(), "token");
    }

    public StitchingService getStitchingService(OkHttpClient http) {
        return new StitchingService(
            http,
            authTokenGenerator,
            userResolver,
            stitchingBaseUrl + stitchingResource
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