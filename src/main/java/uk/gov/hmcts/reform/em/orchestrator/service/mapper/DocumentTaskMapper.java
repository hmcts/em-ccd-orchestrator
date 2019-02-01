package uk.gov.hmcts.reform.em.orchestrator.service.mapper;

import org.mapstruct.Mapper;
import uk.gov.hmcts.reform.em.orchestrator.domain.DocumentTask;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.DocumentTaskDTO;

import java.util.List;

/**
 * Mapper for the entity DocumentTask and its DTO DocumentTaskDTO.
 */
@Mapper(componentModel = "spring", uses = {BundleMapper.class})
public interface DocumentTaskMapper extends EntityMapper<DocumentTaskDTO, DocumentTask> {


    DocumentTask toEntity(DocumentTaskDTO messageDTO);
    DocumentTaskDTO toDto(DocumentTask message);

    List<DocumentTask> toEntity(List<DocumentTaskDTO> list);
    List<DocumentTaskDTO> toDto(List<DocumentTask> list);

}
