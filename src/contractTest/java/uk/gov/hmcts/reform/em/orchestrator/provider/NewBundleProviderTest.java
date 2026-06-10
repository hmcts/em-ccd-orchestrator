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
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.endpoint.NewBundleController;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.em.orchestrator.provider.ProviderTestUtil.createNewBundleResponse;

@Provider("em_orchestrator_new_bundle_provider")
@WebMvcTest(value = NewBundleController.class, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    OAuth2ClientAutoConfiguration.class
})
public class NewBundleProviderTest extends BaseProviderTest {

    @Autowired
    private NewBundleController newBundleController;

    @MockitoBean
    private DefaultUpdateCaller defaultUpdateCaller;

    @MockitoBean
    private AutomatedCaseUpdater automatedCaseUpdater;

    @Override
    protected Object[] getControllersUnderTest() {
        return new Object[]{newBundleController};
    }

    @State("a request to prepare a new bundle is successful")
    public void prepareNewBundleSuccessState() {
        CcdCallbackResponseDto responseDto = createNewBundleResponse();
        when(defaultUpdateCaller.executeUpdate(any(AutomatedCaseUpdater.class), any(HttpServletRequest.class)))
            .thenReturn(ResponseEntity.ok(responseDto));
    }
}