package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LocalConfigurationLoaderTest {
    private final LocalConfigurationLoader loader = new LocalConfigurationLoader(new ObjectMapper(new YAMLFactory()));

    @Test
    public void load() {
        BundleConfiguration config = loader.load("example.yaml");

        assertEquals(config.title, "New bundle");
        assertEquals(config.folders.get(0).name, "Folder 1");
        assertEquals(config.folders.get(0).folders.get(0).name, "Folder 1.a");
        assertEquals(config.folders.get(0).folders.get(1).name, "Folder 1.b");
        assertEquals(config.folders.get(1).name, "Folder 2");
        assertEquals(config.filename, "stitched.pdf");
    }

    @Test(expected = BundleConfigurationException.class)
    public void loadMissingConfig() {
        loader.load("does-not-exist.yaml");
    }

    @Test
    public void filename() {
        BundleConfiguration config = loader.load("example-with-filename.yaml");

        assertEquals(config.title, "Bundle with filename");
        assertEquals(config.filename, "bundle.pdf");
    }
}
