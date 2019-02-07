package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingServiceException;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.TaskState;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static pl.touk.throwing.ThrowingFunction.unchecked;

@Service
@Transactional
public class CcdBundleStitchingService implements CcdCaseUpdater {

    private final ObjectMapper objectMapper;
    private final JavaType type;
    private final StitchingService stitchingService;

    public CcdBundleStitchingService(ObjectMapper objectMapper, StitchingService stitchingService) {
        this.objectMapper = objectMapper;
        this.stitchingService = stitchingService;
        type = objectMapper.getTypeFactory().constructParametricType(CcdValue.class, BundleDTO.class);
    }

    @Override
    public boolean handles(CcdCallbackDto ccdCallbackDto) {
        return false;
    }

    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {
        Optional<ArrayNode> maybeBundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);

        if (maybeBundles.isPresent()) {
            List<JsonNode> newBundles = StreamSupport
                    .stream(Spliterators.spliteratorUnknownSize(maybeBundles.get().iterator(), Spliterator.ORDERED), false)
                    .parallel()
                    .map(unchecked(this::bundleJsonToBundleDto))
                    .map(unchecked(bundle -> this.stitchBundle(bundle, ccdCallbackDto.getJwt())))
                    .map(bundleDto -> objectMapper.convertValue(bundleDto, JsonNode.class))
                    .collect(Collectors.toList());

            maybeBundles.get().removeAll();
            maybeBundles.get().addAll(newBundles);
        }

        return ccdCallbackDto.getCaseData();
    }
    private CcdValue<BundleDTO> stitchBundle(CcdValue<BundleDTO> bundle, String jwt) throws InterruptedException {
        try {
            String stitchedDocumentURI = stitchingService.stitch(bundle.getValue(), jwt);
            bundle.getValue().setStitchedDocumentURI(stitchedDocumentURI);
            bundle.getValue().setStitchStatus(TaskState.DONE.toString());
        }
        catch (StitchingServiceException e) {
            bundle.getValue().setStitchStatus(TaskState.FAILED.toString());
        }

        return bundle;
    }

    private CcdValue<BundleDTO> bundleJsonToBundleDto(JsonNode jsonNode) throws IOException {
        return objectMapper.readValue(objectMapper.treeAsTokens(jsonNode), type);
    }
}
