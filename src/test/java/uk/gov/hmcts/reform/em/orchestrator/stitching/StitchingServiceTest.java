package uk.gov.hmcts.reform.em.orchestrator.stitching;

import okhttp3.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.stitching.mapper.StitchingDTOMapper;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class StitchingServiceTest {

    @Value("${em-rpa-stitching-api.base-url}")
    private String stitchingBaseUrl;

    @Value("${em-rpa-stitching-api.resource}")
    private String stitchingResource;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private WebApplicationContext context;

    private static final String ID_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9"
            + ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsIm"
            + "p0aSI6ImQzNWRmMTRkLTA5ZjYtNDhmZi04YTkzLTdjNmYwMzM5MzE1OSIsImlhdCI6MTU0M"
            + "Tk3MTU4MywiZXhwIjoxNTQxOTc1MTgzfQ.QaQOarmV8xEUYV7yvWzX3cUE_4W1luMcWCwpr"
            + "oqqUrg";

    protected MockMvc restLogoutMockMvc;

    @Before
    public void setupMocks() {

        Map<String, Object> claims = new HashMap<>();
        claims.put("groups", "ROLE_USER");
        claims.put("sub", 123);

        OidcIdToken idToken = new OidcIdToken(ID_TOKEN, Instant.now(),
                Instant.now().plusSeconds(60), claims);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken(idToken));

        SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
        this.restLogoutMockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    private OAuth2AuthenticationToken authenticationToken(OidcIdToken idToken) {

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("USER"));
        OidcUser user = new DefaultOidcUser(authorities, idToken);

        return new OAuth2AuthenticationToken(user, authorities, "oidc");

    }

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
        CcdDocument docId = service.stitch(new CcdBundleDTO(), "token");

        Assert.assertEquals(docId.getUrl(), "AAAAAA");
        Assert.assertEquals(docId.getFileName(), "stitched.pdf");
    }

    @Test
    public void stitchSuccessfulWithFileName() throws StitchingServiceException, InterruptedException {
        List<String> responses = new ArrayList<>();
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"DONE\", \"bundle\": { \"stitchedDocumentURI\": \"AAAAAA\", \"fileName\": \"a.pdf\" } }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        CcdDocument docId = service.stitch(new CcdBundleDTO(), "token");

        Assert.assertEquals(docId.getUrl(), "AAAAAA");
        Assert.assertEquals(docId.getFileName(), "a.pdf");
    }

    @Test(expected = StitchingServiceException.class)
    public void stitchFailure() throws StitchingServiceException, InterruptedException {
        List<String> responses = new ArrayList<>();
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"NEW\", \"bundle\": { \"stitchedDocumentURI\": null } }");
        responses.add("{ \"id\": 1, \"taskState\": \"FAILED\", \"bundle\": { \"stitchedDocumentURI\": \"AAAAAA\", \"fileName\": \"a.pdf\" } }");

        OkHttpClient http = getMockHttp(responses);
        StitchingService service = getStitchingService(http);
        service.stitch(new CcdBundleDTO(), "token");
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
        service.stitch(new CcdBundleDTO(), "token");
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
