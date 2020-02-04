package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.*;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.*;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBoolean;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundlePaginationStyle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class BundleFactoryTest {

    private final BundleFactory factory = new BundleFactory();
    private final ObjectNode emptyJson = new ObjectNode(new JsonNodeFactory(false));
    private final File case1Json = new File(ClassLoader.getSystemResource("case-data1.json").getPath());
    private final File case2Json = new File(ClassLoader.getSystemResource("case-data2.json").getPath());
    private final File case3Json = new File(ClassLoader.getSystemResource("case-data3.json").getPath());
    private final File case4Json = new File(ClassLoader.getSystemResource("case-data4.json").getPath());
    private final File case5Json = new File(ClassLoader.getSystemResource("case-data5.json").getPath());
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void create() throws DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
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
            false
        );

        CcdBundleDTO bundle = factory.create(configuration, emptyJson);

        assertEquals(configuration.title, bundle.getTitle());
        assertEquals(configuration.filename, bundle.getFileName());
        assertEquals(configuration.hasCoversheets, bundle.getHasCoversheets() == CcdBoolean.Yes);
        assertEquals(configuration.hasTableOfContents, bundle.getHasTableOfContents() == CcdBoolean.Yes);
        assertEquals(configuration.hasFolderCoversheets, bundle.getHasFolderCoversheets() == CcdBoolean.Yes);
    }

    @Test
    public void createWithDocumentSelect() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
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
            false
        );

        JsonNode json = mapper.readTree(case1Json);
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document2.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
    }

    @Test
    public void createWithDocumentSetSelect() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
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
            false
        );

        JsonNode json = mapper.readTree(case2Json);
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document2.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals("document3.pdf", bundle.getDocuments().get(2).getValue().getSourceDocument().getFileName());
    }

    @Test
    public void createWithDocumentSetFilters() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
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
            false
        );

        JsonNode json = mapper.readTree(case3Json);
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document4.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals(2, bundle.getDocuments().size());
    }

    @Test
    public void createWithDocumentSetRegex() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
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
            false
        );

        JsonNode json = mapper.readTree(case3Json);
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document4.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals(2, bundle.getDocuments().size());
    }

    @Test
    public void createWithCustomDocumentNameDefined() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
                "Bundle title",
                "filename.pdf",
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
                false
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
    public void createWithSortOrderAscending() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
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
            false
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
    public void createWithSortOrderDescending() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
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
            false
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
    public void createWithSortOrderDescendingAndMixOfDateTypes() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
                "Bundle title",
                "filename.pdf",
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
                false
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
    public void createWithSortOrderAscendingWithNullDate() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
                "Bundle title",
                "filename.pdf",
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
                false
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
    public void createWithCustomDateFieldNotDefined() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
                "Bundle title",
                "filename.pdf",
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
                false
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
}
