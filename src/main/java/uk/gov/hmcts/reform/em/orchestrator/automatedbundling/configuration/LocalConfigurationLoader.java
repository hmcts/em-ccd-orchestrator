package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

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
    public BundleConfiguration load(String filename) {
        InputStream input = Thread
            .currentThread()
            .getContextClassLoader()
            .getResourceAsStream("bundleconfiguration/" + filename);

        try {
            BundleConfiguration bundleConfiguration = mapper.readValue(input, BundleConfiguration.class);
            if (!bundleConfiguration.validate()) {
                throw new BundleConfigurationException(
                    "customDocumentLinkValue should be provided in " + filename + " when customDocument is set to "
                        + "true.");
            }
            return bundleConfiguration;
        } catch (BundleConfigurationException bundleConfigExp) {
            throw bundleConfigExp;
        } catch (Exception e) {
            throw new BundleConfigurationException(
                    "Invalid configuration file entry in: " + filename + "; Configuration file parameter(s) and/or parameter value(s)", e);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }
}
