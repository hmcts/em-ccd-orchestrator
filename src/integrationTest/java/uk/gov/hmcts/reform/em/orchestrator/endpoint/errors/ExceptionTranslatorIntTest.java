package uk.gov.hmcts.reform.em.orchestrator.endpoint.errors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.endpoint.BaseTest;
import uk.gov.hmcts.reform.em.orchestrator.endpoint.TestSecurityConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the ExceptionTranslator controller advice.
 *
 * @see ExceptionTranslator
 */
@SpringBootTest(classes = {Application.class, TestSecurityConfiguration.class})
class ExceptionTranslatorIntTest extends BaseTest {

    @Autowired
    private ExceptionTranslatorTestController controller;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Test
    void testMissingServletRequestPartException() throws Exception {
        mockMvc.perform(get("/test/missing-servlet-request-part"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("Required part 'part' is not present."));
    }

    @Test
    void testMissingServletRequestParameterException() throws Exception {
        mockMvc.perform(get("/test/missing-servlet-request-parameter"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("Required parameter 'param' is not present."));
    }

    @Test
    void testFeignConflict() throws Exception {
        mockMvc.perform(get("/test/feign-conflict"))
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void testFeignBadGateway() throws Exception {
        mockMvc.perform(get("/test/feign-bad-gateway"))
            .andExpect(status().isBadGateway())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }


}