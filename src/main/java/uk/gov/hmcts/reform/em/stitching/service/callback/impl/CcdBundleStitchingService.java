package uk.gov.hmcts.reform.em.stitching.service.callback.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.touk.throwing.ThrowingFunction;
import uk.gov.hmcts.reform.em.stitching.batch.DocumentTaskItemProcessor;
import uk.gov.hmcts.reform.em.stitching.domain.DocumentTask;
import uk.gov.hmcts.reform.em.stitching.service.callback.CcdCaseUpdater;
import uk.gov.hmcts.reform.em.stitching.service.dto.BundleDTO;
import uk.gov.hmcts.reform.em.stitching.service.mapper.BundleMapper;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class CcdBundleStitchingService implements CcdCaseUpdater {

    private DocumentTaskItemProcessor documentTaskItemProcessor;

    private BundleMapper bundleMapper;

    private final ObjectMapper mapper = new ObjectMapper();

    public CcdBundleStitchingService(DocumentTaskItemProcessor documentTaskItemProcessor, BundleMapper bundleMapper) {
        this.documentTaskItemProcessor = documentTaskItemProcessor;
        this.bundleMapper = bundleMapper;
    }

    @Override
    public void updateCase(JsonNode bundleData, String jwt) {

        ArrayNode bundles = castJsonDataToJsonArray(bundleData);

        List<JsonNode> newBundles = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(bundles.iterator(), Spliterator.ORDERED),false)
                .parallel()
                .map(ThrowingFunction.unchecked(this::bundleJsonToBundleDto))
                .map(bundleMapper::toEntity)
                .map(bundle -> new DocumentTask(bundle, jwt))
                .map(documentTaskItemProcessor::process)
                .map(DocumentTask::getBundle)
                .map(bundleMapper::toDto)
                .map( bundleDto -> mapper.convertValue(bundleDto, JsonNode.class))
                .collect(Collectors.toList());

        bundles.removeAll();
        bundles.addAll(newBundles);

    }

    private BundleDTO bundleJsonToBundleDto(JsonNode jsonNode) throws JsonProcessingException {
        return mapper.treeToValue(jsonNode, BundleDTO.class);
    }

    private ArrayNode castJsonDataToJsonArray(JsonNode bundleData) {
        try {
            return (ArrayNode) bundleData;
        } catch (ClassCastException e) {
            throw new IncorrectCcdCaseBundlesException( "Bundle data is not in correct format", e);
        }
    }

}
