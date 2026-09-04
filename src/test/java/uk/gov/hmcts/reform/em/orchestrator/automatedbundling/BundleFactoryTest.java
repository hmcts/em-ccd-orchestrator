package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfiguration;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfigurationDocument;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfigurationDocumentSelector;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfigurationDocumentSet;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfigurationDocumentSet.BundleConfigurationFilter;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfigurationSort;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfigurationSortOrder;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.ImageRendering;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.ImageRenderingLocation;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.PageNumberFormat;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBoolean;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundlePaginationStyle;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentImage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BundleFactoryTest {

    private final BundleFactory factory = new BundleFactory();
    private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectNode emptyJson = JsonNodeFactory.instance.objectNode();

    @Test
    void create() throws DocumentSelectorException {
        BundleConfiguration configuration = configBuilder().build();

        CcdBundleDTO bundle = factory.create(configuration, emptyJson);

        assertEquals(configuration.title, bundle.getTitle());
        assertEquals(configuration.filename, bundle.getFileName());
        assertEquals(configuration.hasCoversheets, bundle.getHasCoversheets() == CcdBoolean.Yes);
        assertEquals(configuration.hasTableOfContents, bundle.getHasTableOfContents() == CcdBoolean.Yes);
        assertEquals(configuration.hasFolderCoversheets, bundle.getHasFolderCoversheets() == CcdBoolean.Yes);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "filename.pdf, /case_details/id",
        "null, /case_details/id",
        "null, null"
    }, nullValues = {"null"})
    void createWithVariousFileNameConfigurations(String filename, String fileNameIdentifier)
        throws DocumentSelectorException {
        BundleConfiguration configuration = configBuilder()
            .filename(filename)
            .fileNameIdentifier(fileNameIdentifier)
            .build();

        CcdBundleDTO bundle = factory.create(configuration, emptyJson);

        assertEquals(configuration.filename, bundle.getFileName());
    }

    @Test
    void createWithDocumentSelect() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = configBuilder()
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocument("/folder/document")
            ))
            .build();

        JsonNode json = loadJsonResource("case-data1.json");
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document2.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
    }

    @Test
    void createWithDocWithRedactedFlagAndRedactedDocNode() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = configBuilder()
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocument("/folder/document")
            ))
            .hasCustomDocumentRedactedIdentifier(true)
            .customDocumentRedactedIdentifier("/customDocumentLink")
            .build();

        JsonNode json = loadJsonResource("case-data-custom.json");
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document2.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
    }

    @Test
    void createWithDocWithRedactedFalseAndRedactedDocNode() throws IOException {
        BundleConfiguration configuration = configBuilder()
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocument("/folder/document")
            ))
            .build();

        JsonNode json = loadJsonResource("case-data-custom.json");

        DocumentSelectorException exception = assertThrows(
            DocumentSelectorException.class,
            () -> factory.create(configuration, json)
        );

        assertTrue(exception.getMessage()
            .equalsIgnoreCase("Could not find the property /documentLink/document_url in the node: "));
    }

    @Test
    void createWithDocWithRedactedTrueAndWithoutRedactedDocNode() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = configBuilder()
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocument("/folder/document")
            ))
            .hasCustomDocumentRedactedIdentifier(true)
            .customDocumentRedactedIdentifier("/customDocumentLink")
            .build();

        JsonNode json = loadJsonResource("case-data1.json");
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document2.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
    }

    @Test
    void createWithDocumentSetSelect() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = configBuilder()
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", List.of())
            ))
            .build();

        JsonNode json = loadJsonResource("case-data2.json");
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document2.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals("document3.pdf", bundle.getDocuments().get(2).getValue().getSourceDocument().getFileName());
    }

    @Test
    void createWithDocumentFails() throws IOException {
        BundleConfiguration configuration = configBuilder()
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", List.of())
            ))
            .build();

        JsonNode json = loadJsonResource("case-data7.json");
        assertThrows(BundleException.class, () -> factory.create(configuration, json));
    }

    @Test
    void createWithDocumentSetFilters() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = configBuilder()
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", List.of(
                    new BundleConfigurationFilter("/selectMe", "yesPlease"),
                    new BundleConfigurationFilter("/alsoSelectMe", "okayThen")
                ))
            ))
            .build();

        JsonNode json = loadJsonResource("case-data3.json");
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document4.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals(2, bundle.getDocuments().size());
    }

    @Test
    void createWithDocumentSetRegex() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = configBuilder()
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", List.of(
                    new BundleConfigurationFilter("/selectMe", "yes.*"),
                    new BundleConfigurationFilter("/alsoSelectMe", "okay.*")
                ))
            ))
            .build();

        JsonNode json = loadJsonResource("case-data3.json");
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document4.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals(2, bundle.getDocuments().size());
    }

    @Test
    void createWithCustomDocumentNameDefined() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = configBuilder()
            .sortOrder(new BundleConfigurationSort("/customTimeField", BundleConfigurationSortOrder.ascending))
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", List.of())
            ))
            .documentNameIdentifier("/documentFileName")
            .build();

        JsonNode json = loadJsonResource("case-data4.json");
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document2.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals(0, bundle.getDocuments().get(0).getValue().getSortIndex());
        assertEquals("document4.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals(1, bundle.getDocuments().get(1).getValue().getSortIndex());
        assertEquals("document1.pdf", bundle.getDocuments().get(2).getValue().getSourceDocument().getFileName());
        assertEquals(2, bundle.getDocuments().get(2).getValue().getSortIndex());
    }

    @Test
    void createWithSortOrderAscending() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = configBuilder()
            .sortOrder(new BundleConfigurationSort("/customTimeField", BundleConfigurationSortOrder.ascending))
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", List.of())
            ))
            .build();

        JsonNode json = loadJsonResource("case-data2.json");
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document2.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals(0, bundle.getDocuments().get(0).getValue().getSortIndex());
        assertEquals("document3.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals(1, bundle.getDocuments().get(1).getValue().getSortIndex());
        assertEquals("document1.pdf", bundle.getDocuments().get(2).getValue().getSourceDocument().getFileName());
        assertEquals(2, bundle.getDocuments().get(2).getValue().getSortIndex());
    }

    @Test
    void createWithSortOrderDescending() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = configBuilder()
            .sortOrder(new BundleConfigurationSort("/customTimeField", BundleConfigurationSortOrder.descending))
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", List.of())
            ))
            .build();

        JsonNode json = loadJsonResource("case-data2.json");
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals(0, bundle.getDocuments().get(0).getValue().getSortIndex());
        assertEquals("document3.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals(1, bundle.getDocuments().get(1).getValue().getSortIndex());
        assertEquals("document2.pdf", bundle.getDocuments().get(2).getValue().getSourceDocument().getFileName());
        assertEquals(2, bundle.getDocuments().get(2).getValue().getSortIndex());
    }

    @Test
    void createWithSortOrderDescendingAndMixOfDateTypes() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = configBuilder()
            .sortOrder(new BundleConfigurationSort("/customTimeField", BundleConfigurationSortOrder.descending))
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", List.of())
            ))
            .build();

        JsonNode json = loadJsonResource("case-data5.json");
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals(0, bundle.getDocuments().get(0).getValue().getSortIndex());
        assertEquals("document3.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals(1, bundle.getDocuments().get(1).getValue().getSortIndex());
        assertEquals("document2.pdf", bundle.getDocuments().get(2).getValue().getSourceDocument().getFileName());
        assertEquals(2, bundle.getDocuments().get(2).getValue().getSortIndex());
    }

    @Test
    void createWithSortOrderAscendingWithNullDate() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = configBuilder()
            .sortOrder(new BundleConfigurationSort("/customTimeField", BundleConfigurationSortOrder.ascending))
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", List.of())
            ))
            .build();

        JsonNode json = loadJsonResource("case-data3.json");
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document2.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals(0, bundle.getDocuments().get(0).getValue().getSortIndex());
        assertEquals("document4.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals(1, bundle.getDocuments().get(1).getValue().getSortIndex());
        assertEquals("document1.pdf", bundle.getDocuments().get(2).getValue().getSourceDocument().getFileName());
        assertEquals(2, bundle.getDocuments().get(2).getValue().getSortIndex());
    }

    @Test
    void createWithCustomDateFieldNotDefined() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = configBuilder()
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", List.of())
            ))
            .documentNameIdentifier("/documentFileName")
            .build();

        JsonNode json = loadJsonResource("case-data4.json");
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals(0, bundle.getDocuments().get(0).getValue().getSortIndex());
        assertEquals("document2.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals(0, bundle.getDocuments().get(1).getValue().getSortIndex());
        assertEquals("document3.pdf", bundle.getDocuments().get(2).getValue().getSourceDocument().getFileName());
        assertEquals(0, bundle.getDocuments().get(2).getValue().getSortIndex());
        assertEquals("document4.pdf", bundle.getDocuments().get(3).getValue().getSourceDocument().getFileName());
        assertEquals(0, bundle.getDocuments().get(2).getValue().getSortIndex());
    }

    @Test
    void createWithImageRenderingDefined() throws IOException, DocumentSelectorException {
        DocumentImage docImg = new DocumentImage();
        docImg.setImageRendering(ImageRendering.opaque);
        docImg.setImageRenderingLocation(ImageRenderingLocation.allPages);
        docImg.setCoordinateX(40);
        docImg.setCoordinateY(50);
        docImg.setDocmosisAssetId("schmcts.png");

        BundleConfiguration configuration = configBuilder()
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", List.of())
            ))
            .documentNameIdentifier("/documentFileName")
            .documentImage(docImg)
            .build();

        JsonNode json = loadJsonResource("case-data4.json");
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document4.pdf", bundle.getDocuments().get(3).getValue().getSourceDocument().getFileName());
        assertEquals(0, bundle.getDocuments().get(2).getValue().getSortIndex());
        assertEquals("schmcts.png", bundle.getDocumentImage().getDocmosisAssetId());
        assertEquals(ImageRenderingLocation.allPages, bundle.getDocumentImage().getImageRenderingLocation());
        assertEquals(ImageRendering.opaque, bundle.getDocumentImage().getImageRendering());
        assertEquals(40, bundle.getDocumentImage().getCoordinateX());
        assertEquals(50, bundle.getDocumentImage().getCoordinateY());
    }

    @Test
    void createWithCustomDocumentLinkDefined() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = configBuilder()
            .sortOrder(new BundleConfigurationSort("/customTimeField", BundleConfigurationSortOrder.ascending))
            .documents(List.of(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", List.of())
            ))
            .documentNameIdentifier("/documentFileName")
            .documentRedactedIdentifier("/document")
            .build();

        JsonNode json = loadJsonResource("case-data6.json");
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document2.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals(0, bundle.getDocuments().get(0).getValue().getSortIndex());
        assertEquals("document4.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals(1, bundle.getDocuments().get(1).getValue().getSortIndex());
        assertEquals("document1.pdf", bundle.getDocuments().get(2).getValue().getSourceDocument().getFileName());
        assertEquals(2, bundle.getDocuments().get(2).getValue().getSortIndex());
    }

    @Test
    void testAddDocumentThrowsExceptionForArrayProperty() {
        final String arrayPropertyPath = "/arrayDocument";
        BundleConfiguration configuration = configBuilder()
            .fileNameIdentifier(null)
            .coverSheetTemplate(null)
            .documents(List.of(new BundleConfigurationDocument(arrayPropertyPath)))
            .build();

        ObjectNode caseData = JsonNodeFactory.instance.objectNode();
        ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
        arrayNode.add(JsonNodeFactory.instance.objectNode().put("field", "value"));
        caseData.set(arrayPropertyPath.substring(1), arrayNode);

        DocumentSelectorException exception = assertThrows(
            DocumentSelectorException.class,
            () -> factory.create(configuration, caseData)
        );

        assertEquals("Element is an array: " + arrayPropertyPath, exception.getMessage());
    }

    @Test
    void testAddDocumentSetThrowsExceptionForNonArrayProperty() {
        final String nonArrayPropertyPath = "/nonArrayDocumentSet";
        BundleConfiguration configuration = configBuilder()
            .fileNameIdentifier(null)
            .coverSheetTemplate(null)
            .documents(List.of(new BundleConfigurationDocumentSet(nonArrayPropertyPath, List.of())))
            .build();

        ObjectNode caseData = JsonNodeFactory.instance.objectNode();
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put("field", "value");
        caseData.set(nonArrayPropertyPath.substring(1), objectNode);

        DocumentSelectorException exception = assertThrows(
            DocumentSelectorException.class,
            () -> factory.create(configuration, caseData)
        );

        assertEquals("Element is not an array: " + nonArrayPropertyPath, exception.getMessage());
    }

    private JsonNode loadJsonResource(String resourceName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            return mapper.readTree(is);
        }
    }

    private static TestConfigBuilder configBuilder() {
        return new TestConfigBuilder();
    }

    private static class TestConfigBuilder {
        private String title = "Bundle title";
        private String filename = "filename.pdf";
        private String fileNameIdentifier = "/case_details/id";
        private String coverSheetTemplate = "FL-FRM-GOR-ENG-12345";
        private PageNumberFormat pageNumberFormat = PageNumberFormat.numberOfPages;
        private BundleConfigurationSort sortOrder = null;
        private boolean hasCoversheets = true;
        private boolean hasTableOfContents = true;
        private boolean hasFolderCoversheets = true;
        private List<BundleConfigurationDocumentSelector> documents = new ArrayList<>();
        private CcdBundlePaginationStyle paginationStyle = CcdBundlePaginationStyle.off;
        private String documentNameIdentifier = null;
        private DocumentImage documentImage = null;
        private boolean enableRedaction = false;
        private String documentRedactedIdentifier = null;
        private boolean hasCustomDocumentRedactedIdentifier = false;
        private String customDocumentRedactedIdentifier = null;

        public TestConfigBuilder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public TestConfigBuilder fileNameIdentifier(String fileNameIdentifier) {
            this.fileNameIdentifier = fileNameIdentifier;
            return this;
        }

        public TestConfigBuilder coverSheetTemplate(String coverSheetTemplate) {
            this.coverSheetTemplate = coverSheetTemplate;
            return this;
        }

        public TestConfigBuilder sortOrder(BundleConfigurationSort sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public TestConfigBuilder documents(List<BundleConfigurationDocumentSelector> documents) {
            this.documents = documents;
            return this;
        }

        public TestConfigBuilder documentNameIdentifier(String documentNameIdentifier) {
            this.documentNameIdentifier = documentNameIdentifier;
            return this;
        }

        public TestConfigBuilder documentImage(DocumentImage documentImage) {
            this.documentImage = documentImage;
            return this;
        }

        public TestConfigBuilder documentRedactedIdentifier(String documentRedactedIdentifier) {
            this.documentRedactedIdentifier = documentRedactedIdentifier;
            return this;
        }

        public TestConfigBuilder hasCustomDocumentRedactedIdentifier(boolean value) {
            this.hasCustomDocumentRedactedIdentifier = value;
            return this;
        }

        public TestConfigBuilder customDocumentRedactedIdentifier(String customDocumentRedactedIdentifier) {
            this.customDocumentRedactedIdentifier = customDocumentRedactedIdentifier;
            return this;
        }

        public BundleConfiguration build() {
            return new BundleConfiguration(
                title,
                filename,
                fileNameIdentifier,
                coverSheetTemplate,
                pageNumberFormat,
                sortOrder,
                hasCoversheets,
                hasTableOfContents,
                hasFolderCoversheets,
                new ArrayList<>(),
                documents,
                paginationStyle,
                documentNameIdentifier,
                documentImage,
                enableRedaction,
                documentRedactedIdentifier,
                hasCustomDocumentRedactedIdentifier,
                customDocumentRedactedIdentifier,
                null, // hasDocumentSubtitles
                null, // hasDocumentOutlineSubtitles
                null  // hasTableOfContentsSubtitles
            );
        }
    }
}