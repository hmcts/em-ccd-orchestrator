package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalConfigurationLoaderTest {
    private final LocalConfigurationLoader loader = new LocalConfigurationLoader(new ObjectMapper(new YAMLFactory()));

    private static final String CUSTOM_DOCUMENT_LINK_VALUE_MISSING_MSG =
        "customDocumentLinkValue should be provided in testbundleconfiguration/custom-bundle-wrong-config.yaml "
            + "when customDocument is set to true.";

    @Test
    void load() {
        BundleConfiguration config = loader.load("testbundleconfiguration/example.yaml");

        assertEquals("New bundle", config.title);
        assertTrue(config.hasTableOfContents);
        assertTrue(config.hasCoversheets);
        assertFalse(config.hasFolderCoversheets);
        assertEquals("Folder 1", config.folders.getFirst().name);
        assertEquals("Folder 1.a", config.folders.get(0).folders.get(0).name);
        assertEquals("Folder 1.b", config.folders.get(0).folders.get(1).name);
        assertEquals("Folder 2", config.folders.get(1).name);
        assertEquals("stitched.pdf",config.filename);
    }

    @Test
    void loadMissingConfig() {
        assertThrows(BundleConfigurationException.class, () -> loader.load("does-not-exist.yaml"));
    }

    @Test
    void loadMissingCustomDocumentLinkValue() {
        try {
            loader.load("testbundleconfiguration/custom-bundle-wrong-config.yaml");
        }  catch (BundleConfigurationException exp) {
            assertTrue(CUSTOM_DOCUMENT_LINK_VALUE_MISSING_MSG.equalsIgnoreCase(exp.getMessage()));
        }
    }

    @Test
    void filename() {
        BundleConfiguration config = loader.load("testbundleconfiguration/example-with-filename.yaml");

        assertEquals("Bundle with filename", config.title);
        assertEquals("bundle.pdf", config.filename);
        assertFalse(config.hasTableOfContents);
        assertFalse(config.hasCoversheets);
        assertTrue(config.hasFolderCoversheets);
    }

    @Test
    void bundleWithNoFileName() {
        BundleConfiguration config = loader.load("testbundleconfiguration/example.yaml");

        assertEquals("New bundle", config.title);
        assertEquals("stitched.pdf", config.filename);
    }

    @Test
    void fileContainsIncorrectFieldname() {
        assertThrows(BundleConfigurationException.class,
            () -> loader.load("testbundleconfiguration/example-incorrect-key.yaml"));
    }

    @Test
    void fileContainsIncorrectValueForField() {
        BundleConfigurationException exception = assertThrows(BundleConfigurationException.class,
            () -> loader.load("example-incorrect-value-for-key.yaml"));
        assertEquals("Invalid configuration file entry in: example-incorrect-value-for-key.yaml"
            + "; Configuration file parameter(s) and/or parameter value(s)", exception.getMessage());
    }

    @Test
    void documentSet() {
        BundleConfiguration config = loader.load("testbundleconfiguration/example-with-documents.yaml");

        assertEquals(2, config.documents.size());
        assertEquals(0, config.folders.get(0).documents.size());
        assertEquals(2, config.folders.get(1).documents.size());
    }

    @Test
    void enableEmailNotificationNull() {
        BundleConfiguration config = loader.load("testbundleconfiguration/example-with-documents.yaml");

        assertNull(config.enableEmailNotification);
    }

    @Test
    void checkAllBundleConfigurationStructure() throws IOException {
        String resourceName = "bundleconfiguration";
        ClassLoader classLoader = getClass().getClassLoader();
        File folder = new File(classLoader.getResource(resourceName).getFile());

        var fileNameList = Files.list(folder.toPath())
                .map(file -> file.toFile().getName())
                .filter(fileName -> !fileName.contains("testbundleconfiguration"))
                .collect(Collectors.toSet());
        boolean success = false;
        for (String fileName : fileNameList) {
            try {
                loader.load(fileName);
                // everything works as expected
                success = true;
            } catch (Exception exp) {
                assertEquals("New config failed, check Actual->", exp.getMessage());
            }
        }
        assertTrue(success);
    }

}
