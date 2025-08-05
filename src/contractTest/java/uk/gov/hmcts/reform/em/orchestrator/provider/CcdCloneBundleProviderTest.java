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
import uk.gov.hmcts.reform.em.orchestrator.endpoint.CcdCloneBundleController;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdBundleCloningService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.em.orchestrator.provider.ProviderTestUtil.createCloneBundleResponse;

@Provider("em_orchestrator_clone_bundle_provider")
@WebMvcTest(value = CcdCloneBundleController.class, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    OAuth2ClientAutoConfiguration.class
})
public class CcdCloneBundleProviderTest extends BaseProviderTest {

    @Autowired
    private CcdCloneBundleController ccdCloneBundleController;

    @MockitoBean
    private DefaultUpdateCaller defaultUpdateCaller;

    @MockitoBean
    private CcdBundleCloningService ccdBundleCloningService;

    @Override
    protected Object[] getControllersUnderTest() {
        return new Object[]{ccdCloneBundleController};
    }

    @State("a request to clone a bundle is successful")
    public void cloneBundleSuccessState() {
        CcdCallbackResponseDto responseDto = createCloneBundleResponse();
        when(defaultUpdateCaller.executeUpdate(eq(ccdBundleCloningService), any(HttpServletRequest.class)))
            .thenReturn(ResponseEntity.ok(responseDto));
    }
}