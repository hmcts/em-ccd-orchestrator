package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static pl.touk.throwing.ThrowingFunction.unchecked;

public abstract class UpdateCase implements CcdCaseUpdater {

    private final ObjectMapper objectMapper;

    private final JavaType type;

    public UpdateCase(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        type = objectMapper.getTypeFactory().constructParametricType(CcdValue.class, CcdBundleDTO.class);
    }

    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {
        Optional<ArrayNode> maybeBundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);

        if (maybeBundles.isPresent()) {
            List<JsonNode> newBundles = StreamSupport
                    .stream(Spliterators.spliteratorUnknownSize(
                            maybeBundles.get().iterator(), Spliterator.ORDERED), false)
                    .parallel()
                    .map(unchecked(this::bundleJsonToBundleValue))
                    .map(bundle -> bundle.getValue().getEligibleForStitchingAsBoolean()
                            ? this.stitchBundle(bundle, ccdCallbackDto) : bundle)
                    .map(bundleDto -> objectMapper.convertValue(bundleDto, JsonNode.class))
                    .toList();

            maybeBundles.get().removeAll();
            maybeBundles.get().addAll(CcdCaseUpdater.reorderBundles(newBundles, objectMapper, type));
        }
        return ccdCallbackDto.getCaseData();
    }

    abstract CcdValue<CcdBundleDTO> stitchBundle(CcdValue<CcdBundleDTO> bundle,
                                                 CcdCallbackDto ccdCallbackDto);

    private CcdValue<CcdBundleDTO> bundleJsonToBundleValue(JsonNode jsonNode) throws IOException {
        return objectMapper.readValue(objectMapper.treeAsTokens(jsonNode), type);
    }
}
