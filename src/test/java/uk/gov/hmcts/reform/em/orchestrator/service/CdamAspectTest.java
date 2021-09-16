package uk.gov.hmcts.reform.em.orchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class CdamAspectTest {

    @Mock
    HttpServletRequest request;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    JsonNode payload;

    @Mock
    BufferedReader reader;

    @Mock
    JoinPoint joinPoint;

    @InjectMocks
    CdamAspect cdamAspect;

    //Below is required to intialize the Mock objects.
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void testPopulateCdamDetailsEmpty() throws IOException {
        when(request.getReader()).thenReturn(reader);

        cdamAspect.populateCdamDetails(joinPoint);

        Mockito.verify(joinPoint, Mockito.atLeast(0)).getArgs();
    }

    @Test
    public void testPopulateCdamDetailsIOException() throws IOException {
        when(request.getReader()).thenThrow(IOException.class);

        cdamAspect.populateCdamDetails(joinPoint);

        Mockito.verify(joinPoint, Mockito.atLeast(0)).getArgs();
    }

    @Test
    public void testPopulateCdamDetails() throws IOException {

        when(request.getHeader("serviceauthorization")).thenReturn("abc");
        BufferedReader bufferedReader = new BufferedReader(new FileReader("src/test/resources/cdampayload.json"));
        when(request.getReader()).thenReturn(bufferedReader);
        DocumentTaskDTO documentTaskDTO = new DocumentTaskDTO();
        DocumentTaskDTO[] documentTaskDtos = {documentTaskDTO};
        when(joinPoint.getArgs()).thenReturn(documentTaskDtos);

        cdamAspect.populateCdamDetails(joinPoint);

        assertNotNull(documentTaskDTO.getCaseTypeId());
        assertNotNull(documentTaskDTO.getJurisdictionId());
        assertNotNull(documentTaskDTO.getServiceAuth());
    }
}
