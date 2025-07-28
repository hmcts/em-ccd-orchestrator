package uk.gov.hmcts.reform.em.orchestrator.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@IgnoreNoPactsToVerify
@AutoConfigureMockMvc(addFilters = false)
@PactFolder("pacts")
//@PactBroker(
//    url = "${PACT_BROKER_FULL_URL:http://localhost:80}",
//    providerBranch = "${pact.provider.branch}"
//)
public abstract class BaseProviderTest {

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void setupPactVerification(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget(mockMvc);
        testTarget.setControllers(getControllersUnderTest());

        if (context != null) {
            context.setTarget(testTarget);
        }
    }

    protected abstract Object[] getControllersUnderTest();

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @PactBrokerConsumerVersionSelectors
    public static SelectorBuilder consumerVersionSelectors() {
        return new SelectorBuilder()
            .matchingBranch()
            .mainBranch()
            .deployedOrReleased();
    }
}