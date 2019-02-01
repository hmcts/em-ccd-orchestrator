package uk.gov.hmcts.reform.em.orchestrator.service;

import uk.gov.hmcts.reform.em.orchestrator.domain.DocumentTask;
import uk.gov.hmcts.reform.em.orchestrator.service.impl.DocumentTaskProcessingException;

import java.io.File;

public interface DmStoreUploader {

    void uploadFile(File file, DocumentTask documentTask) throws DocumentTaskProcessingException;

}
