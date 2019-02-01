package uk.gov.hmcts.reform.em.stitching.service.callback;

import com.fasterxml.jackson.databind.JsonNode;

public interface CcdCaseUpdater {

    void updateCase(JsonNode bundleData, String jwt);

}
