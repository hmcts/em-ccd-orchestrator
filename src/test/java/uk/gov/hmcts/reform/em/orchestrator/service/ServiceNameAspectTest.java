package uk.gov.hmcts.reform.em.orchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.em.orchestrator.config.security.SecurityUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ServiceNameAspectTest {

    @Mock
    SecurityUtils securityUtils;

    @Mock
    HttpServletRequest request;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    BufferedReader bufferedReader;

    JsonNode payload;
    String jsonString = "{ \"id\" : \n{\n\"firstName\": \"something\",\n"
            + "\"lastName\" : \"something\"\n}\n}";

    @InjectMocks
    ServiceNameAspect serviceNameAspect;

    @Before
    public void setUp() throws IOException {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(request.getReader()).thenReturn(bufferedReader);
        ObjectMapper mapper = new ObjectMapper();
        payload = mapper.readTree(jsonString);
        when(objectMapper.readTree(bufferedReader)).thenReturn(payload);
    }

    @Test
    public void testLogServiceNameEmptyServiceAuth() {

        when(request.getHeader(Mockito.anyString())).thenReturn(null);

        serviceNameAspect.logServiceName();

        Mockito.verify(securityUtils, Mockito.atLeast(0)).getServiceName(Mockito.anyString());
    }

    @Test
    public void testLogServiceName() {

        when(request.getHeader("serviceauthorization")).thenReturn("abc");
        when(securityUtils.getServiceName(Mockito.anyString())).thenReturn("xxx");
        serviceNameAspect.logServiceName();

        Mockito.verify(securityUtils, Mockito.atLeast(1)).getServiceName(Mockito.anyString());
    }

    @Test
    public void testLogServiceNameBearer() {

        when(request.getHeader("serviceauthorization")).thenReturn("Bearer abc");
        when(securityUtils.getServiceName(Mockito.anyString())).thenReturn("xxx");
        serviceNameAspect.logServiceName();

        Mockito.verify(securityUtils, Mockito.atLeast(1)).getServiceName(Mockito.anyString());
    }

    @Test
    public void testLogServiceNameThrowsInvalidTokenException() {

        when(request.getHeader("serviceauthorization")).thenReturn("abc");
        when(securityUtils.getServiceName(Mockito.anyString())).thenThrow(InvalidTokenException.class);
        serviceNameAspect.logServiceName();

        Mockito.verify(securityUtils, Mockito.atLeast(1)).getServiceName(Mockito.anyString());
    }

}
