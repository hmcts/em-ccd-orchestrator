package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfiguration;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfigurationDocument;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfigurationDocumentSet;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBoolean;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;

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
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void create() throws DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            "FL-FRM-GOR-ENG-12345",
            true,
            true,
            true,
            new ArrayList<>(),
            new ArrayList<>()
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
                true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocument("/folder/document")
            )
        );

        JsonNode json = mapper.readTree(case1Json);
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document2.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
    }

    @Test(expected = DocumentSelectorException.class)
    public void createMissingProperty() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
                "FL-FRM-GOR-ENG-12345",
                true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/does not exist"),
                new BundleConfigurationDocument("/folder/document")
            )
        );

        JsonNode json = mapper.readTree(case1Json);
        factory.create(configuration, json);
    }

    @Test
    public void createWithDocumentSetSelect() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
                "FL-FRM-GOR-ENG-12345",
                true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/caseDocuments", Collections.emptyList())
            )
        );

        JsonNode json = mapper.readTree(case2Json);
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document2.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals("document3.pdf", bundle.getDocuments().get(2).getValue().getSourceDocument().getFileName());
    }

    @Test(expected = DocumentSelectorException.class)
    public void createWithDocumentSetDoesNotExist() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
                "FL-FRM-GOR-ENG-12345",
                true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/does not exist", Collections.emptyList())
            )
        );

        JsonNode json = mapper.readTree(case2Json);
        factory.create(configuration, json);
    }

    @Test(expected = DocumentSelectorException.class)
    public void createWithDocumentSetNotArray() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
                "FL-FRM-GOR-ENG-12345",
                true,
            true,
            true,
            new ArrayList<>(),
            Arrays.asList(
                new BundleConfigurationDocument("/document1"),
                new BundleConfigurationDocumentSet("/document1", Collections.emptyList())
            )
        );

        JsonNode json = mapper.readTree(case2Json);
        factory.create(configuration, json);
    }

    @Test
    public void createWithDocumentSetFilters() throws IOException, DocumentSelectorException {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
                "FL-FRM-GOR-ENG-12345",
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
            )
        );

        JsonNode json = mapper.readTree(case3Json);
        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals("document1.pdf", bundle.getDocuments().get(0).getValue().getSourceDocument().getFileName());
        assertEquals("document4.pdf", bundle.getDocuments().get(1).getValue().getSourceDocument().getFileName());
        assertEquals(2, bundle.getDocuments().size());
    }
}
