package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.LocalConfigurationLoader;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AutomatedCaseUpdaterTest {

    @Mock
    private AutomatedStitchingExecutor automatedStitchingExecutor;

    private AutomatedCaseUpdater updater = new AutomatedCaseUpdater(
        new LocalConfigurationLoader(
            new ObjectMapper(
                new YAMLFactory()
            )
        ),
        new ObjectMapper(),
        new BundleFactory(),
        automatedStitchingExecutor
    );

    private final CcdCallbackDtoCreator ccdCallbackDtoCreator = new CcdCallbackDtoCreator(
        new ObjectMapper()
    );

    @Before
    public void setup() {
        updater = new AutomatedCaseUpdater(
            new LocalConfigurationLoader(
                    new ObjectMapper(
                            new YAMLFactory()
                    )
            ),
            new ObjectMapper(),
            new BundleFactory(),
            automatedStitchingExecutor
        );
    }

    @Test
    public void handles() throws IOException {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader())
            .thenReturn(
                new BufferedReader(
                    new StringReader("{\"case_details\":{\"case_data\": {\"bundleConfiguration\":\"b\"}}}")
                )
            );

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest, "caseBundles");

        assertTrue(updater.handles(ccdCallbackDto));
    }

    @Test
    public void doesNotHandleWithOutBundleConfigValue() throws IOException {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader())
                .thenReturn(
                        new BufferedReader(
                                new StringReader("{\"case_details\":{\"case_data\": {\"bundleConfiguration\":\"\"}}}")
                        ));

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest, "caseBundles");

        assertFalse(updater.handles(ccdCallbackDto));
    }

    @Test
    public void doesNotHandle() throws IOException {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader())
            .thenReturn(
                new BufferedReader(
                    new StringReader("{\"case_details\":{\"case_data\": {\"a\":\"b\"}}}")
                )
            );

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest, "caseBundles");

        assertFalse(updater.handles(ccdCallbackDto));
    }

    @Test
    public void updateCase() throws IOException {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader())
            .thenReturn(
                new BufferedReader(
                    new StringReader("{\"case_details\":{\"case_data\": {\"bundleConfiguration\":\"example.yaml\", \"caseBundles\": []}}}")
                )
            );

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest, "caseBundles");
        updater.updateCase(ccdCallbackDto);

        Optional<ArrayNode> bundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);

        assertTrue(bundles.isPresent());
        assertEquals(1, bundles.get().size());
        assertEquals("Folder 1", bundles.get().get(0).at("/value/folders").get(0).at("/value/name").asText());
        assertEquals("Folder 1.a", bundles.get().get(0).at("/value/folders").get(0).at("/value/folders").get(0).at("/value/name").asText());
        assertEquals("Folder 1.b", bundles.get().get(0).at("/value/folders").get(0).at("/value/folders").get(1).at("/value/name").asText());
        assertEquals("Folder 2", bundles.get().get(0).at("/value/folders").get(1).at("/value/name").asText());
        assertEquals("stitched.pdf", bundles.get().get(0).at("/value/fileName").asText());
    }

    @Test
    public void createCaseBundlesPropertyWhenItDoesntExist() throws IOException {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader())
            .thenReturn(
                new BufferedReader(
                    new StringReader("{\"case_details\":{\"case_data\": {\"bundleConfiguration\":\"example.yaml\"}}}")
                )
            );

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest, "caseBundles");
        updater.updateCase(ccdCallbackDto);

        Optional<ArrayNode> bundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);

        assertTrue(bundles.isPresent());
        assertEquals(1, bundles.get().size());
    }
}
