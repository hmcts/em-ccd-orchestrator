package uk.gov.hmcts.reform.em.stitching.service.callback.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.stitching.service.callback.CasePropertyFinder;

import java.util.Optional;

@Service
@Transactional
public class CasePropertyFinderImpl implements CasePropertyFinder {

    @Override
    public Optional<JsonNode> findCaseProperty(JsonNode jsonNode, String propertyName) {
        return Optional.ofNullable(jsonNode.findValue(propertyName));
    }

}
