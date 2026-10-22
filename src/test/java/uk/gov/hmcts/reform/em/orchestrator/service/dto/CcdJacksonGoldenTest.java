package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.em.orchestrator.config.JacksonGoldenMappers;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden round trips for CCD document / bundle DTOs on Jackson 3 paths used in production.
 */
class CcdJacksonGoldenTest {

    private static final String DOCUMENT_GOLDEN = "/jackson-golden/ccd-document.json";
    private static final String BUNDLE_GOLDEN = "/jackson-golden/ccd-bundle-with-document.json";

    @Test
    @DisplayName("CcdDocument round-trips snake_case with Boot-like ObjectMapper")
    void ccdDocumentRoundTripBootLikeMapper() throws Exception {
        assertDocumentRoundTrip(JacksonGoldenMappers.bootLikeJsonMapper());
    }

    @Test
    @DisplayName("CcdDocument round-trips snake_case with JacksonMapperFactory ObjectMapper")
    void ccdDocumentRoundTripFactoryMapper() throws Exception {
        assertDocumentRoundTrip(JacksonGoldenMappers.factoryJsonMapper());
    }

    @Test
    @DisplayName("CcdBundleDTO nested sourceDocument keeps document_url under Boot-like mapper")
    void ccdBundleWithDocumentRoundTripBootLikeMapper() throws Exception {
        assertBundleWithDocumentRoundTrip(JacksonGoldenMappers.bootLikeJsonMapper());
    }

    @Test
    @DisplayName("CcdBundleDTO nested sourceDocument keeps document_url under factory mapper")
    void ccdBundleWithDocumentRoundTripFactoryMapper() throws Exception {
        assertBundleWithDocumentRoundTrip(JacksonGoldenMappers.factoryJsonMapper());
    }

    @Test
    @DisplayName("MissingNode.asText() is empty string on Jackson 3 (not literal null)")
    void missingNodeAsTextIsEmptyString() {
        ObjectMapper mapper = JacksonGoldenMappers.bootLikeJsonMapper();
        JsonNode missing = mapper.createObjectNode().path("absentField");

        assertTrue(missing.isMissingNode());
        assertEquals("", missing.asText());
        assertFalse("null".equals(missing.asText()));
    }

    private static void assertDocumentRoundTrip(ObjectMapper mapper) throws Exception {
        String golden = readResource(DOCUMENT_GOLDEN);
        CcdDocument document = mapper.readValue(golden, CcdDocument.class);

        assertEquals("http://dm-store/documents/11111111-1111-1111-1111-111111111111", document.getUrl());
        assertEquals("evidence.pdf", document.getFileName());
        assertEquals(
            "http://dm-store/documents/11111111-1111-1111-1111-111111111111/binary",
            document.getBinaryUrl()
        );
        assertEquals("sha256-deadbeef", document.getHash());

        String written = mapper.writeValueAsString(document);
        JsonNode writtenNode = mapper.readTree(written);
        assertTrue(writtenNode.has("document_url"), written);
        assertTrue(writtenNode.has("document_filename"), written);
        assertTrue(writtenNode.has("document_binary_url"), written);
        assertTrue(writtenNode.has("document_hash"), written);
        assertFalse(writtenNode.has("url"), written);
        assertFalse(writtenNode.has("fileName"), written);
        assertFalse(writtenNode.has("binaryUrl"), written);
        assertFalse(writtenNode.has("hash"), written);

        CcdDocument roundTrip = mapper.readValue(written, CcdDocument.class);
        assertEquals(document.getUrl(), roundTrip.getUrl());
        assertEquals(document.getFileName(), roundTrip.getFileName());
        assertEquals(document.getBinaryUrl(), roundTrip.getBinaryUrl());
        assertEquals(document.getHash(), roundTrip.getHash());
    }

    private static void assertBundleWithDocumentRoundTrip(ObjectMapper mapper) throws Exception {
        String golden = readResource(BUNDLE_GOLDEN);
        JavaType type = mapper.getTypeFactory().constructParametricType(CcdValue.class, CcdBundleDTO.class);
        CcdValue<CcdBundleDTO> wrapped = mapper.readValue(golden, type);

        CcdBundleDTO bundle = wrapped.getValue();
        assertEquals("Golden Bundle", bundle.getTitle());
        assertEquals("golden-bundle.pdf", bundle.getFileName());
        assertEquals(1, bundle.getDocuments().size());

        CcdBundleDocumentDTO doc = bundle.getDocuments().getFirst().getValue();
        assertEquals("Evidence doc", doc.getName());
        assertEquals(
            "http://dm-store/documents/22222222-2222-2222-2222-222222222222",
            doc.getSourceDocument().getUrl()
        );
        assertEquals("evidence.pdf", doc.getSourceDocument().getFileName());

        JsonNode tree = mapper.valueToTree(wrapped);
        String written = mapper.writeValueAsString(tree);
        assertTrue(written.contains("\"document_url\""), written);
        assertTrue(written.contains("\"document_filename\""), written);
        assertFalse(written.contains("\"unknownFutureCcdField\""), written);

        CcdValue<CcdBundleDTO> roundTrip = mapper.readValue(written, type);
        assertEquals(bundle.getTitle(), roundTrip.getValue().getTitle());
        assertEquals(
            doc.getSourceDocument().getUrl(),
            roundTrip.getValue().getDocuments().getFirst().getValue().getSourceDocument().getUrl()
        );
    }

    private static String readResource(String path) throws Exception {
        try (InputStream in = CcdJacksonGoldenTest.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing test resource: " + path);
            }
            return new String(in.readAllBytes());
        }
    }
}
