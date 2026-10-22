package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.TypeFactory;
import uk.gov.hmcts.reform.em.orchestrator.config.JacksonMapperFactory;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CcdCaseUpdaterReorderBundlesTest {

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void reorderBundlesReturnsOriginalListWhenJacksonExceptionThrown() {
        ObjectMapper realMapper = JacksonMapperFactory.createJsonMapper();
        JsonNode bundle = realMapper.readTree("{\"value\":{\"title\":\"A\"}}");
        List<JsonNode> bundles = List.of(bundle);
        JavaType type = TypeFactory.createDefaultInstance()
            .constructParametricType(CcdValue.class, CcdBundleDTO.class);

        when(objectMapper.treeAsTokens(any(JsonNode.class))).thenReturn(realMapper.treeAsTokens(bundle));
        when(objectMapper.readValue(any(JsonParser.class), eq(type)))
            .thenThrow(new JacksonException("parse failed") {
            });

        List<JsonNode> result = CcdCaseUpdater.reorderBundles(bundles, objectMapper, type);

        assertSame(bundles, result);
    }
}
