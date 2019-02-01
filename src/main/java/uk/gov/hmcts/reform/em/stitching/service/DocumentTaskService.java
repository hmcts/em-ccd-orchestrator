package uk.gov.hmcts.reform.em.stitching.service;

import uk.gov.hmcts.reform.em.stitching.service.dto.DocumentTaskDTO;

import java.util.Optional;

/**
 * Service Interface for managing DocumentTask.
 */
public interface DocumentTaskService {

    /**
     * Save a documentTask.
     *
     * @param documentTaskDTO the entity to save
     * @return the persisted entity
     */
    DocumentTaskDTO save(DocumentTaskDTO documentTaskDTO);

    /**
     * Get the "id" documentTask.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Optional<DocumentTaskDTO> findOne(Long id);

    /**
     * Process a document task
     *
     * @param documentTaskDTO task to process
     * @return updated dto
     */
    DocumentTaskDTO process(DocumentTaskDTO documentTaskDTO);

}
