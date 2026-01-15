package uk.gov.hmcts.reform.em.orchestrator.endpoint.errors;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExceptionTranslatorTest {

    private final ExceptionTranslator exceptionTranslator = new ExceptionTranslator();

    @Test
    void shouldHandleFeignConflictException() {
        int status = HttpStatus.CONFLICT.value();
        String message = "Entity already exists";

        FeignException feignException = mock(FeignException.class);
        
        when(feignException.status()).thenReturn(status);
        when(feignException.getMessage()).thenReturn(message);

        ProblemDetail problemDetail = exceptionTranslator.handleFeignException(feignException);

        assertNotNull(problemDetail);
        assertEquals(status, problemDetail.getStatus());
        assertEquals(message, problemDetail.getDetail());
    }

    @Test
    void shouldHandleFeignBadGatewayException() {
        int status = HttpStatus.BAD_GATEWAY.value();
        String message = "Upstream service unavailable";
        
        FeignException feignException = mock(FeignException.class);
        
        when(feignException.status()).thenReturn(status);
        when(feignException.getMessage()).thenReturn(message);

        ProblemDetail problemDetail = exceptionTranslator.handleFeignException(feignException);

        assertNotNull(problemDetail);
        assertEquals(status, problemDetail.getStatus());
        assertEquals(message, problemDetail.getDetail());
    }
}