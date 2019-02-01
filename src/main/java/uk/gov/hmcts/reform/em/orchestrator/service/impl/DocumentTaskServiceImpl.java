package uk.gov.hmcts.reform.em.orchestrator.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.orchestrator.batch.DocumentTaskItemProcessor;
import uk.gov.hmcts.reform.em.orchestrator.domain.DocumentTask;
import uk.gov.hmcts.reform.em.orchestrator.repository.BundleRepository;
import uk.gov.hmcts.reform.em.orchestrator.repository.DocumentTaskRepository;
import uk.gov.hmcts.reform.em.orchestrator.service.DocumentTaskService;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.mapper.DocumentTaskMapper;

import java.util.Optional;

/**
 * Service Implementation for managing DocumentTask.
 */
@Service
@Transactional
public class DocumentTaskServiceImpl implements DocumentTaskService {

    private final Logger log = LoggerFactory.getLogger(DocumentTaskServiceImpl.class);

    private final DocumentTaskRepository documentTaskRepository;

    private final DocumentTaskMapper documentTaskMapper;

    private final BundleRepository bundleRepository;

    private final DocumentTaskItemProcessor itemProcessor;


    public DocumentTaskServiceImpl(DocumentTaskRepository documentTaskRepository,
                                   DocumentTaskMapper documentTaskMapper,
                                   BundleRepository bundleRepository,
                                   DocumentTaskItemProcessor itemProcessor) {
        this.documentTaskRepository = documentTaskRepository;
        this.documentTaskMapper = documentTaskMapper;
        this.bundleRepository = bundleRepository;
        this.itemProcessor = itemProcessor;
    }

    /**
     * Save a documentTask.
     *
     * @param documentTaskDTO the entity to save
     * @return the persisted entity
     */
    @Override
    @Transactional
    public DocumentTaskDTO save(DocumentTaskDTO documentTaskDTO) {
        log.debug("Request to save DocumentTask : {}", documentTaskDTO);
        DocumentTask documentTask = documentTaskMapper.toEntity(documentTaskDTO);

        bundleRepository.save(documentTask.getBundle());

        documentTask = documentTaskRepository.save(documentTask);

        return documentTaskMapper.toDto(documentTask);
    }

    /**
     * Get one documentTask by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<DocumentTaskDTO> findOne(Long id) {
        log.debug("Request to get DocumentTask : {}", id);
        return documentTaskRepository.findById(id)
            .map(documentTaskMapper::toDto);
    }

    /**
     * Use the task processor to process the task
     *
     * @param documentTaskDTO task to process
     * @return updated dto
     */
    @Override
    public DocumentTaskDTO process(DocumentTaskDTO documentTaskDTO) {
        DocumentTask documentTask = documentTaskMapper.toEntity(documentTaskDTO);

        itemProcessor.process(documentTask);

        return documentTaskMapper.toDto(documentTask);
    }
}
