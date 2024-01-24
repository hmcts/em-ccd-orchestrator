package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class LocalConfigurationLoaderTest {
    private final LocalConfigurationLoader loader = new LocalConfigurationLoader(new ObjectMapper(new YAMLFactory()));

    private static final String CUSTOM_DOCUMENT_LINK_VALUE_MISSING_MSG =
        "customDocumentLinkValue should be provided in testbundleconfiguration/custom-bundle-wrong-config.yaml "
            + "when customDocument is set to true.";

    @Test
    public void load() {
        BundleConfiguration config = loader.load("testbundleconfiguration/example.yaml");

        assertEquals("New bundle", config.title);
        assertEquals(true, config.hasTableOfContents);
        assertEquals(true, config.hasCoversheets);
        assertEquals(false, config.hasFolderCoversheets);
        assertEquals("Folder 1", config.folders.get(0).name);
        assertEquals("Folder 1.a", config.folders.get(0).folders.get(0).name);
        assertEquals("Folder 1.b", config.folders.get(0).folders.get(1).name);
        assertEquals("Folder 2", config.folders.get(1).name);
        assertEquals("stitched.pdf",config.filename);
    }

    @Test(expected = BundleConfigurationException.class)
    public void loadMissingConfig() {
        loader.load("does-not-exist.yaml");
    }

    @Test
    public void loadMissingCustomDocumentLinkValue() {
        try {
            loader.load("testbundleconfiguration/custom-bundle-wrong-config.yaml");
        }  catch (BundleConfigurationException exp) {
            Assert.assertTrue(CUSTOM_DOCUMENT_LINK_VALUE_MISSING_MSG.equalsIgnoreCase(exp.getMessage()));
        }
    }

    @Test
    public void filename() {
        BundleConfiguration config = loader.load("testbundleconfiguration/example-with-filename.yaml");

        assertEquals("Bundle with filename", config.title);
        assertEquals("bundle.pdf", config.filename);
        assertEquals(false, config.hasTableOfContents);
        assertEquals(false, config.hasCoversheets);
        assertEquals(true, config.hasFolderCoversheets);
    }

    @Test
    public void bundleWithNoFileName() {
        BundleConfiguration config = loader.load("testbundleconfiguration/example.yaml");

        assertEquals("New bundle", config.title);
        assertEquals("stitched.pdf", config.filename);
    }

    @Test(expected = BundleConfigurationException.class)
    public void fileContainsIncorrectFieldname() {
        loader.load("testbundleconfiguration/example-incorrect-key.yaml");
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void fileContainsIncorrectValueForField() {
        thrown.expect(BundleConfigurationException.class);
        thrown.expectMessage("Invalid configuration file entry in: example-incorrect-value-for-key.yaml"
                + "; Configuration file parameter(s) and/or parameter value(s)");
        loader.load("example-incorrect-value-for-key.yaml");
    }

    @Test
    public void documentSet() {
        BundleConfiguration config = loader.load("testbundleconfiguration/example-with-documents.yaml");

        assertEquals(2, config.documents.size());
        assertEquals(0, config.folders.get(0).documents.size());
        assertEquals(2, config.folders.get(1).documents.size());
    }

    @Test
    public void enableEmailNotificationNull() {
        BundleConfiguration config = loader.load("testbundleconfiguration/example-with-documents.yaml");

        assertEquals(null, config.enableEmailNotification);
    }

    @Test
    public void checkAllBundleConfigurationStructure() throws IOException {
        String resourceName = "bundleconfiguration";
        ClassLoader classLoader = getClass().getClassLoader();
        File folder = new File(classLoader.getResource(resourceName).getFile());

        var fileNameList = Files.list(folder.toPath())
                .map(file -> file.toFile().getName())
                .filter(fileName -> !fileName.contains("testbundleconfiguration"))
                .collect(Collectors.toSet());
        boolean success = false;
        for(String fileName:fileNameList) {
            try {
                loader.load(fileName);
                // everything works as expected
                success =true;
            } catch (Exception exp) {
                Assert.assertEquals("New config failed, check Actual->",exp.getMessage());
            }
        }
        Assert.assertTrue(success);
    }

}
