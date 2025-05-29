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
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfigurationDocumentSet;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfigurationSort;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfigurationSortOrder;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.ImageRendering;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.ImageRenderingLocation;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.PageNumberFormat;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBoolean;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundlePaginationStyle;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentImage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class BundleFactoryTest {

    private final BundleFactory factory = new BundleFactory();
    private final ObjectNode emptyJson = new ObjectNode(new JsonNodeFactory(false));
    private final File case1Json = new File(ClassLoader.getSystemResource("case-data1.json").getPath());
    private final File case2Json = new File(ClassLoader.getSystemResource("case-data2.json").getPath());
    private final File case3Json = new File(ClassLoader.getSystemResource("case-data3.json").getPath());
    private final File case4Json = new File(ClassLoader.getSystemResource("case-data4.json").getPath());
    private final File case5Json = new File(ClassLoader.getSystemResource("case-data5.json").getPath());
    private final File case6Json = new File(ClassLoader.getSystemResource("case-data6.json").getPath());
    private final File case7Json = new File(ClassLoader.getSystemResource("case-data7.json").getPath());
    private final File customCaseJson = new File(ClassLoader.getSystemResource("case-data-custom.json").getPath());
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void create() throws DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            null,
            true,
            true,
            true,
            new ArrayList<>(),
            new ArrayList<>(),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            false,
            null
        );

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
    void createWithVariousFileNameConfigurations(String filename,
                                                 String fileNameIdentifier) throws DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            filename,
            fileNameIdentifier,
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            null,
            true,
            true,
            true,
            new ArrayList<>(),
            new ArrayList<>(),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            false,
            null
        );

        CcdBundleDTO bundle = factory.create(configuration, emptyJson);

        assertEquals(configuration.filename, bundle.getFileName());
    }

    @Test
    void createWithDocumentSelect() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            null,
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocument("/folder/document")
            ),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            false,
            null
        );

        JsonNode json = mapper.readTree(case1Json);
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document2.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
    }

    @Test
    void createWithDocWithRedactedFlagAndRedactedDocNode() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            null,
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocument("/folder/document")
            ),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            true,
            "/customDocumentLink"
        );

        JsonNode json = mapper.readTree(customCaseJson);
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document2.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
    }

    @Test
    void createWithDocWithRedactedFalseAndRedactedDocNode() throws IOException, DocumentSelectorException {
        //Redacted flag is set to false. We should have "/documentLink" node. In this case, we have
        // "/redactedDocumentLink" node
        // but the corresponding "/documentLink" node is missing
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            null,
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocument("/folder/document")
            ),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            false,
            null
        );

        try {
            JsonNode json = mapper.readTree(customCaseJson);
            factory.create(configuration, json);
        } catch (DocumentSelectorException docExp) {
            assertTrue(docExp.getMessage()
                .equalsIgnoreCase("Could not find the property /documentLink/document_url in the node: "));
        }

    }

    @Test
    void createWithDocWithRedactedTrueAndWithoutRedactedDocNode() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            null,
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocument("/folder/document")
            ),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            true,
            "/customDocumentLink"
        );

        JsonNode json = mapper.readTree(case1Json);
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document2.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
    }

    @Test
    void createWithDocumentSetSelect() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            null,
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", Collections.emptyList())
            ),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            false,
            null
        );

        JsonNode json = mapper.readTree(case2Json);
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document2.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals("document3.pdf", bundle.getDocuments().get(2).getValue().getSourceDocument().getFileName());
    }

    @Test
    void createWithDocumentFails() throws IOException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            null,
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", Collections.emptyList())
            ),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            false,
            null
        );

        JsonNode json = mapper.readTree(case7Json);
        assertThrows(BundleException.class, () -> factory.create(configuration, json));
    }

    @Test
    void createWithDocumentSetFilters() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            null,
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", Arrays.asList(
                    new BundleConfigurationDocumentSet.BundleConfigurationFilter("/selectMe", "yesPlease"),
                    new BundleConfigurationDocumentSet.BundleConfigurationFilter("/alsoSelectMe", "okayThen")
                ))
            ),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            false,
            null
        );

        JsonNode json = mapper.readTree(case3Json);
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document4.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals(2, bundle.getDocuments().size());
    }

    @Test
    void createWithDocumentSetRegex() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            null,
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", Arrays.asList(
                    new BundleConfigurationDocumentSet.BundleConfigurationFilter("/selectMe", "yes.*"),
                    new BundleConfigurationDocumentSet.BundleConfigurationFilter("/alsoSelectMe", "okay.*")
                ))
            ),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            false,
            null
        );

        JsonNode json = mapper.readTree(case3Json);
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document4.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals(2, bundle.getDocuments().size());
    }

    @Test
    void createWithCustomDocumentNameDefined() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            new BundleConfigurationSort("/customTimeField", BundleConfigurationSortOrder.ascending),
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", Collections.emptyList())
            ),
            CcdBundlePaginationStyle.off,
            "/documentFileName",
            null,
            false,
            null,
            false,
            null
        );

        JsonNode json = mapper.readTree(case4Json);
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
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            new BundleConfigurationSort("/customTimeField", BundleConfigurationSortOrder.ascending),
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", Collections.emptyList())
            ),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            false,
            null
        );

        JsonNode json = mapper.readTree(case2Json);
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
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            new BundleConfigurationSort("/customTimeField", BundleConfigurationSortOrder.descending),
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", Collections.emptyList())
            ),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            false,
            null
        );

        JsonNode json = mapper.readTree(case2Json);
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
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            new BundleConfigurationSort("/customTimeField", BundleConfigurationSortOrder.descending),
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", Collections.emptyList())
            ),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            false,
            null
        );

        JsonNode json = mapper.readTree(case5Json);
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
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            new BundleConfigurationSort("/customTimeField", BundleConfigurationSortOrder.ascending),
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", Collections.emptyList())
            ),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            false,
            null
        );

        JsonNode json = mapper.readTree(case3Json);
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
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            null,
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", Collections.emptyList())
            ),
            CcdBundlePaginationStyle.off,
            "/documentFileName",
            null,
            false,
            null,
            false,
            null
        );

        JsonNode json = mapper.readTree(case4Json);
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

        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            null,
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", Collections.emptyList())
            ),
            CcdBundlePaginationStyle.off,
            "/documentFileName",
            docImg,
            false,
            null,
            false,
            null
        );

        JsonNode json = mapper.readTree(case4Json);
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document4.pdf", bundle.getDocuments().get(3).getValue().getSourceDocument().getFileName());
        assertEquals(0, bundle.getDocuments().get(2).getValue().getSortIndex());
        assertEquals("schmcts.png", bundle.getDocumentImage().getDocmosisAssetId());
        assertEquals(ImageRenderingLocation.allPages, bundle.getDocumentImage().getImageRenderingLocation());
        assertEquals(ImageRendering.opaque, bundle.getDocumentImage().getImageRendering());
        assertEquals(Integer.valueOf(40), bundle.getDocumentImage().getCoordinateX());
        assertEquals(Integer.valueOf(50), bundle.getDocumentImage().getCoordinateY());

    }

    @Test
    void createWithCustomDocumentLinkDefined() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "/case_details/id",
            "FL-FRM-GOR-ENG-12345",
            PageNumberFormat.numberOfPages,
            new BundleConfigurationSort("/customTimeField", BundleConfigurationSortOrder.ascending),
            true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", Collections.emptyList())
            ),
            CcdBundlePaginationStyle.off,
            "/documentFileName",
            null,
            false,
            "/document",
            false,
            null
        );


        JsonNode json = mapper.readTree(case6Json);

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
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            null,
            null,
            PageNumberFormat.numberOfPages,
            null,
            true,
            true,
            true,
            new ArrayList<>(),
            Collections.singletonList(
                new BundleConfigurationDocument(arrayPropertyPath)
            ),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            false,
            null
        );

        ObjectNode caseData = JsonNodeFactory.instance.objectNode();
        ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
        arrayNode.add(JsonNodeFactory.instance.objectNode().put("field", "value"));
        caseData.set(arrayPropertyPath.substring(1), arrayNode); // remove leading "/"

        DocumentSelectorException exception = assertThrows(
            DocumentSelectorException.class,
            () -> factory.create(configuration, caseData)
        );

        assertEquals("Element is an array: " + arrayPropertyPath, exception.getMessage());
    }

    @Test
    void testAddDocumentSetThrowsExceptionForNonArrayProperty() {
        final String nonArrayPropertyPath = "/nonArrayDocumentSet";
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            null,
            null,
            PageNumberFormat.numberOfPages,
            null,
            true,
            true,
            true,
            new ArrayList<>(),
            Collections.singletonList(
                new BundleConfigurationDocumentSet(nonArrayPropertyPath, Collections.emptyList())
            ),
            CcdBundlePaginationStyle.off,
            null,
            null,
            false,
            null,
            false,
            null
        );

        ObjectNode caseData = JsonNodeFactory.instance.objectNode();
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put("field", "value");
        caseData.set(nonArrayPropertyPath.substring(1), objectNode); // remove leading "/"

        DocumentSelectorException exception = assertThrows(
            DocumentSelectorException.class,
            () -> factory.create(configuration, caseData)
        );

        assertEquals("Element is not an array: " + nonArrayPropertyPath, exception.getMessage());
    }
}