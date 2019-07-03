package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * This loading strategy takes a file from the local filesystem and converts it to a BundleConfiguration object. The local
 * configuration file can be any format that Jackson will support (e.g. JSON or YAML).
 */
public class LocalConfigurationLoader implements ConfigurationLoader {

    private final ObjectMapper mapper;

    public LocalConfigurationLoader(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Load the file from the file system and then pass it to Jackson for parsing. If an exception returns the Optional will
     * be empty.
     */
    @Override
    public Optional<BundleConfiguration> load(String filename) {
        InputStream input = Thread
            .currentThread()
            .getContextClassLoader()
            .getResourceAsStream("bundleconfiguration/" + filename);

        try {
            return Optional.of(mapper.readValue(input, BundleConfiguration.class));
        }
        catch (IOException e) {
            return Optional.empty();
        }
    }
}
