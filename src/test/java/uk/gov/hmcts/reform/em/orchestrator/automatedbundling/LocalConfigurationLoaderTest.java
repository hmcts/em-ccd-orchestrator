package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfiguration;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.LocalConfigurationLoader;

import java.util.Optional;

import static org.junit.Assert.*;

public class LocalConfigurationLoaderTest {
    private final LocalConfigurationLoader loader = new LocalConfigurationLoader(new ObjectMapper(new YAMLFactory()));

    @Test
    public void load() {
        BundleConfiguration config = loader.load("example.yaml").get();

        assertEquals(config.title, "New bundle");
        assertEquals(config.folders.get(0).name, "Folder 1");
        assertEquals(config.folders.get(1).name, "Folder 2");
    }

    @Test
    public void loadMissingConfig() {
        Optional<BundleConfiguration> config = loader.load("does-not-exist.yaml");

        assertEquals(config.isPresent(), false);
    }
}