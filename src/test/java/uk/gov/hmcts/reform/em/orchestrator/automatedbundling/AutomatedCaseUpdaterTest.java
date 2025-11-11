package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.LocalConfigurationLoader;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AutomatedCaseUpdaterTest {

    @Mock
    private AutomatedStitchingExecutor automatedStitchingExecutor;

    private AutomatedCaseUpdater updater;

    private final CcdCallbackDtoCreator ccdCallbackDtoCreator = new CcdCallbackDtoCreator(
        new ObjectMapper()
    );

    @BeforeEach
    void setup() {

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
    void updateCase() throws IOException {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader())
            .thenReturn(
                new BufferedReader(
                    new StringReader("{\"case_details\":{\"case_data\": "
                        + "{\"bundleConfiguration\":\"testbundleconfiguration/example.yaml\", \"caseBundles\": []}}}")
                )
            );

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest, "caseBundles");
        updater.updateCase(ccdCallbackDto);

        Optional<ArrayNode> bundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);

        assertTrue(bundles.isPresent());
        assertEquals(1, bundles.get().size());
        assertEquals("Folder 1", bundles.get().get(0).at("/value/folders")
            .get(0).at("/value/name").asText());
        assertEquals("Folder 1.a", bundles.get().get(0).at("/value/folders")
            .get(0).at("/value/folders").get(0).at("/value/name").asText());
        assertEquals("Folder 1.b", bundles.get().get(0).at("/value/folders")
            .get(0).at("/value/folders").get(1).at("/value/name").asText());
        assertEquals("Folder 2", bundles.get().get(0).at("/value/folders")
            .get(1).at("/value/name").asText());
        assertEquals("stitched.pdf", bundles.get().get(0).at("/value/fileName").asText());
    }

    @Test
    void updateCaseWithArrayBundleConfig() throws IOException {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader())
            .thenReturn(
                new BufferedReader(
                    new StringReader("{\"case_details\":{\"case_data\": {\"multiBundleConfiguration\":"
                        + "[{\"value\":\"testbundleconfiguration/example.yaml\"}], "
                        + "\"caseBundles\": []}}}")
                )
            );

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest, "caseBundles");
        updater.updateCase(ccdCallbackDto);

        Optional<ArrayNode> bundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);

        assertTrue(bundles.isPresent());
        assertEquals(1, bundles.get().size());
        assertEquals("Folder 1", bundles.get().get(0).at("/value/folders")
            .get(0).at("/value/name").asText());
        assertEquals("Folder 1.a", bundles.get().get(0).at("/value/folders")
            .get(0).at("/value/folders").get(0).at("/value/name").asText());
        assertEquals("Folder 1.b", bundles.get().get(0).at("/value/folders")
            .get(0).at("/value/folders").get(1).at("/value/name").asText());
        assertEquals("Folder 2", bundles.get().get(0).at("/value/folders")
            .get(1).at("/value/name").asText());
        assertEquals("stitched.pdf", bundles.get().get(0).at("/value/fileName").asText());
    }

    @Test
    void updateCaseWithEmptyArrayBundleConfig() throws IOException {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader())
            .thenReturn(
                new BufferedReader(
                    new StringReader("{\"case_details\":{\"case_data\": {\"multiBundleConfiguration\":[], "
                        + "\"caseBundles\": []}}}")
                )
            );

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest, "caseBundles");
        updater.updateCase(ccdCallbackDto);

        Optional<ArrayNode> bundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);

        assertTrue(bundles.isPresent());
        assertEquals(1, bundles.get().size());
    }

    @Test
    void updateCaseWithOutBundleConfig() throws IOException {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader())
            .thenReturn(
                new BufferedReader(
                    new StringReader("{\"case_details\":{\"case_data\": {\"caseBundles\": []}}}")
                )
            );

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest, "caseBundles");
        updater.updateCase(ccdCallbackDto);

        Optional<ArrayNode> bundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);

        assertTrue(bundles.isPresent());
        assertEquals(1, bundles.get().size());
    }

    @Test
    void updateCaseWithFileIdentifier() throws IOException {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader())
                .thenReturn(
                        new BufferedReader(
                                new StringReader("{\"case_details\":{ \"id\": \"1\", \"case_data\": "
                                    + "{\"bundleConfiguration\":\"sscs-bundle-config.yaml\", \"caseBundles\": []}}}"))
                            );

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest, "caseBundles");
        updater.updateCase(ccdCallbackDto);

        Optional<ArrayNode> bundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);

        assertTrue(bundles.isPresent());
        assertEquals(1, bundles.get().size());
        assertEquals("1-SscsBundle.pdf", bundles.get().get(0).at("/value/fileName").asText());
    }

    @Test
    void createCaseBundlesPropertyWhenItDoesntExist() throws IOException {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito
                .when(mockRequest.getReader())
                .thenReturn(
                        new BufferedReader(
                                new StringReader("{\"case_details\":{\"case_data\":"
                                        + "{\"bundleConfiguration\":\"testbundleconfiguration/example.yaml\"}}}")
                        ));

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest, "caseBundles");
        updater.updateCase(ccdCallbackDto);

        Optional<ArrayNode> bundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);

        assertTrue(bundles.isPresent());
        assertEquals(1, bundles.get().size());
    }

    @Test
    void addBundleToHeadOfListIfOneAlreadyExists() throws IOException {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader())
                .thenReturn(
                        new BufferedReader(
                                new StringReader("""
                                    {"case_details":{"case_data": {"bundleConfiguration":\
                                    "testbundleconfiguration/example.yaml",\
                                    "caseBundles": \
                                    [{
                                    "value": {
                                    "id": "19c9f411-e9a0-43f4-af13-e3d7ee98a50b",
                                    "title": "New bundle",
                                    "description": null,
                                    "eligibleForStitching": "no",
                                    "eligibleForCloning": "no",
                                    "stitchedDocument": null,
                                    "documents": [],
                                    "folders": [{
                                    "value": {
                                    "name": "Folder 1 Original",
                                    "documents": [],
                                    "folders": [{
                                    "value": {
                                    "name": "Folder 1.a Original",
                                    "documents": [],
                                    "sortIndex": 0
                                    }
                                    }],
                                    "sortIndex": 0
                                    }
                                    }],
                                    "fileName": "original_bundle.pdf",
                                    "coverpageTemplate": "FL-FRM-APP-ENG-00002.docx",
                                    "hasTableOfContents": "Yes",
                                    "hasCoversheets": "Yes",
                                    "hasFolderCoversheets": "No",
                                    "stitchStatus": null,
                                    "paginationStyle": "topLeft",
                                    "pageNumberFormat": "numberOfPages",
                                    "stitchingFailureMessage": null
                                    }
                                    }]}}}""")
                        ));

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest, "caseBundles");
        updater.updateCase(ccdCallbackDto);

        Optional<ArrayNode> bundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);

        assertTrue(bundles.isPresent());
        assertEquals(2, bundles.get().size());

        assertEquals("Folder 1", bundles.get().get(0).at("/value/folders")
            .get(0).at("/value/name").asText());
        assertEquals("Folder 1.a", bundles.get().get(0).at("/value/folders")
            .get(0).at("/value/folders").get(0).at("/value/name").asText());
        assertEquals("Folder 1.b", bundles.get().get(0).at("/value/folders")
            .get(0).at("/value/folders").get(1).at("/value/name").asText());
        assertEquals("Folder 2", bundles.get().get(0).at("/value/folders")
            .get(1).at("/value/name").asText());
        assertEquals("stitched.pdf", bundles.get().get(0).at("/value/fileName").asText());

        assertEquals("Folder 1 Original", bundles.get().get(1).at("/value/folders")
            .get(0).at("/value/name").asText());
        assertEquals("Folder 1.a Original", bundles.get().get(1).at("/value/folders")
            .get(0).at("/value/folders").get(0).at("/value/name").asText());
        assertEquals("original_bundle.pdf", bundles.get().get(1).at("/value/fileName").asText());
    }
}
