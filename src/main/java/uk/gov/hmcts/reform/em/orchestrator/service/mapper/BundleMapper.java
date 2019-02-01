package uk.gov.hmcts.reform.em.orchestrator.service.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.reform.em.orchestrator.domain.Bundle;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleDTO;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DocumentTaskMapper.class})
public interface BundleMapper extends EntityMapper<BundleDTO, Bundle> {

    @Mapping(target = "documentTask", source = "documentTask")
    Bundle toEntity(BundleDTO messageDTO);

    @InheritInverseConfiguration
    BundleDTO toDto(Bundle message);

    List<Bundle> toEntity(List<BundleDTO> list);
    List<BundleDTO> toDto(List<Bundle> list);

}
