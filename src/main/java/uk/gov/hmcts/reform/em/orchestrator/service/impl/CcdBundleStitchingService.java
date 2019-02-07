package uk.gov.hmcts.reform.em.orchestrator.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.touk.throwing.ThrowingFunction;
import uk.gov.hmcts.reform.em.orchestrator.service.CcdCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingServiceException;

import java.io.IOException;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static pl.touk.throwing.ThrowingFunction.unchecked;

@Service
@Transactional
public class CcdBundleStitchingService implements CcdCaseUpdater {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JavaType type = mapper.getTypeFactory().constructParametricType(CcdValue.class, BundleDTO.class);
    private final StitchingService stitchingService;

    public CcdBundleStitchingService(StitchingService stitchingService) {
        this.stitchingService = stitchingService;
    }

    @Override
    public void updateCase(JsonNode bundleData, String jwt) {
        ArrayNode bundles = castJsonDataToJsonArray(bundleData);

        List<JsonNode> newBundles = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(bundles.iterator(), Spliterator.ORDERED),false)
                .parallel()
                .map(unchecked(this::bundleJsonToBundleDto))
                .map(unchecked(bundle -> this.stitchBundle(bundle, jwt)))
                .map(bundleDto -> mapper.convertValue(bundleDto, JsonNode.class))
                .collect(Collectors.toList());

        bundles.removeAll();
        bundles.addAll(newBundles);
    }

    private CcdValue<BundleDTO> stitchBundle(CcdValue<BundleDTO> bundle, String jwt) throws StitchingServiceException, InterruptedException {
        String stitchedDocId = stitchingService.stitch(bundle.getValue(), jwt);
        bundle.getValue().setStitchedDocId(stitchedDocId);

        return bundle;
    }

    private CcdValue<BundleDTO> bundleJsonToBundleDto(JsonNode jsonNode) throws IOException {
        return mapper.readValue(mapper.treeAsTokens(jsonNode), type);
    }

    private ArrayNode castJsonDataToJsonArray(JsonNode bundleData) {
        try {
            return (ArrayNode) bundleData;
        } catch (ClassCastException e) {
            throw new IncorrectCcdCaseBundlesException( "Bundle data is not in correct format", e);
        }
    }

}
