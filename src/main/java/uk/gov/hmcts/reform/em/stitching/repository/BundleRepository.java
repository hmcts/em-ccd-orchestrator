package uk.gov.hmcts.reform.em.stitching.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.em.stitching.domain.Bundle;


/**
 * Spring Data  repository for the Bundle entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BundleRepository extends JpaRepository<Bundle, Long> {

}
