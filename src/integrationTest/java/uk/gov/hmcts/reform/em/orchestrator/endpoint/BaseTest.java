package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.doReturn;

@TestPropertySource(properties = {
    "case_document_am.url=http://localhost:8090",
    "notify.apiKey=test-api-key-dummy",
    "idam.s2s-auth.totp_secret=test-s2s-key-dummy"
})
public abstract class BaseTest {

    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext wac;

    @Mock
    protected Authentication authentication;

    @Mock
    protected SecurityContext securityContext;

    @BeforeEach
    public void setupMocks() {

        MockitoAnnotations.openMocks(this);

        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
}