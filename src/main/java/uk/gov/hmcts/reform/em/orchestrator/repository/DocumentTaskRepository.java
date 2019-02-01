package uk.gov.hmcts.reform.em.orchestrator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.em.orchestrator.domain.DocumentTask;


/**
 * Spring Data  repository for the DocumentTask entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DocumentTaskRepository extends JpaRepository<DocumentTask, Long> {

}
