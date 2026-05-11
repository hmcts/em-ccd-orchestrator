package uk.gov.hmcts.reform.em.orchestrator.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.em.orchestrator.endpoint.CcdStitchBundleCallbackController;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.AsyncCcdBundleStitchingService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdBundleStitchingService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.em.orchestrator.provider.ProviderTestUtil.createEmStitchBundle;

@Provider("em_stitchBundle")
@WebMvcTest(value = CcdStitchBundleCallbackController.class, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    OAuth2ClientAutoConfiguration.class
})
public class EMStitchBundleProviderTest extends BaseProviderTest {

    @Autowired
    private CcdStitchBundleCallbackController ccdStitchBundleCallbackController;

    @MockitoBean
    private DefaultUpdateCaller defaultUpdateCaller;

    @MockitoBean
    private AsyncCcdBundleStitchingService asyncCcdBundleStitchingService;

    @MockitoBean
    private CcdBundleStitchingService ccdBundleStitchingService;

    @Override
    protected Object[] getControllersUnderTest() {
        return new Object[]{ccdStitchBundleCallbackController};
    }

    @State("a stitch bundle request")
    public void stitchBundleRequest() {
        stubSuccessfulStitchBundleResponse();
    }

    private void stubSuccessfulStitchBundleResponse() {
        CcdCallbackResponseDto responseDto = createEmStitchBundle();
        when(defaultUpdateCaller.executeUpdate(eq(ccdBundleStitchingService), any(HttpServletRequest.class)))
                .thenReturn(ResponseEntity.ok(responseDto));
    }
}