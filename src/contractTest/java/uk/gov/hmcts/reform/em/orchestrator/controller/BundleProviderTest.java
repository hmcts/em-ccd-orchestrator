package uk.gov.hmcts.reform.em.orchestrator.controller;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;


@Provider("em_ccd_orc_bundling")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
        host = "${PACT_BROKER_URL:localhost}",
        port = "${PACT_BROKER_PORT:80}")
@IgnoreNoPactsToVerify
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class})
public class BundleProviderTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DefaultUpdateCaller defaultUpdateCaller;

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        if (context != null) {
            context.setTarget(new MockMvcTestTarget(mockMvc));
        }
    }

}
