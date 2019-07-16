package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfiguration;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBoolean;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class BundleFactoryTest {

    BundleFactory factory = new BundleFactory();
    ObjectNode json = new ObjectNode(new JsonNodeFactory(false));

    @Test
    public void create() {
        BundleConfiguration configuration = new BundleConfiguration(
            "Bundle title",
            "filename.pdf",
            true,
            true,
            true,
            new ArrayList<>(),
            new ArrayList<>()
        );

        CcdBundleDTO bundle = factory.create(configuration, json);

        assertEquals(configuration.title, bundle.getTitle());
        assertEquals(configuration.filename, bundle.getFileName());
        assertEquals(configuration.hasCoversheets, bundle.getHasCoversheets() == CcdBoolean.Yes);
        assertEquals(configuration.hasTableOfContents, bundle.getHasTableOfContents() == CcdBoolean.Yes);
        assertEquals(configuration.hasFolderCoversheets, bundle.getHasFolderCoversheets() == CcdBoolean.Yes);
    }
}
