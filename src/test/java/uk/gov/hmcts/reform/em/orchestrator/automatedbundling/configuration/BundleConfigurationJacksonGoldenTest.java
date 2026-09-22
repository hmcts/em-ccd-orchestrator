package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.yaml.YAMLMapper;
import uk.gov.hmcts.reform.em.orchestrator.config.JacksonMapperFactory;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden YAML round trips for bundle configuration on the production YAML mapper
 * ({@link JacksonMapperFactory#createYamlMapper()}).
 */
class BundleConfigurationJacksonGoldenTest {

    private final YAMLMapper yamlMapper = JacksonMapperFactory.createYamlMapper();
    private final LocalConfigurationLoader loader = new LocalConfigurationLoader(yamlMapper);

    @Test
    @DisplayName("Polymorphic document and documentSet selectors deserialize via factory YAML mapper")
    void polymorphicDocumentSelectorsRoundTrip() throws Exception {
        BundleConfiguration config = loader.load("testbundleconfiguration/example-with-documents.yaml");

        assertEquals("New bundle", config.title);
        assertEquals(2, config.documents.size());
        assertInstanceOf(BundleConfigurationDocumentSet.class, config.documents.get(0));
        assertInstanceOf(BundleConfigurationDocument.class, config.documents.get(1));

        BundleConfigurationDocumentSet documentSet = (BundleConfigurationDocumentSet) config.documents.get(0);
        assertEquals("/caseDocuments", documentSet.property);
        assertEquals(1, documentSet.filters.size());
        assertEquals("/name", documentSet.filters.getFirst().property);
        assertEquals("Document 1", documentSet.filters.getFirst().value);

        BundleConfigurationDocument document = (BundleConfigurationDocument) config.documents.get(1);
        assertEquals("/otherDocument", document.property);

        BundleConfigurationFolder folder2 = config.folders.get(1);
        assertEquals(2, folder2.documents.size());
        assertInstanceOf(BundleConfigurationDocument.class, folder2.documents.get(0));
        assertEquals("/someFolder/someDocument",
            ((BundleConfigurationDocument) folder2.documents.get(0)).property);
    }

    @Test
    @DisplayName("Serialized YAML retains polymorphic type discriminators")
    void writeRetainsTypeDiscriminators() throws Exception {
        BundleConfiguration config = loader.load("testbundleconfiguration/example-with-documents.yaml");
        String written = yamlMapper.writeValueAsString(config);

        // YAMLMapper emits Jackson type tags (!<document> / !<documentSet>) for @JsonTypeInfo
        assertTrue(
            written.contains("!<documentSet>") || written.contains("type: documentSet"),
            written
        );
        assertTrue(
            written.contains("!<document>") || written.contains("type: document"),
            written
        );
        assertTrue(written.contains("/caseDocuments"), written);
        assertTrue(written.contains("/otherDocument"), written);

        BundleConfiguration roundTrip = yamlMapper.readValue(written, BundleConfiguration.class);
        assertEquals(config.title, roundTrip.title);
        assertEquals(config.documents.size(), roundTrip.documents.size());
        assertInstanceOf(BundleConfigurationDocumentSet.class, roundTrip.documents.get(0));
        assertInstanceOf(BundleConfigurationDocument.class, roundTrip.documents.get(1));
        assertEquals(
            ((BundleConfigurationDocumentSet) config.documents.get(0)).property,
            ((BundleConfigurationDocumentSet) roundTrip.documents.get(0)).property
        );
    }

    @Test
    @DisplayName("Unknown YAML property fails with factory FAIL_ON_UNKNOWN_PROPERTIES enabled")
    void unknownPropertyFailsOnFactoryMapper() throws Exception {
        String yaml = readResource("/jackson-golden/bundle-config-unknown-property.yaml");
        assertThrows(Exception.class, () -> yamlMapper.readValue(yaml, BundleConfiguration.class));
    }

    private static String readResource(String path) throws Exception {
        try (InputStream in = BundleConfigurationJacksonGoldenTest.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing test resource: " + path);
            }
            return new String(in.readAllBytes());
        }
    }
}
