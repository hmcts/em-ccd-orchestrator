package uk.gov.hmcts.reform.em.stitching.service.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.reform.em.stitching.domain.Bundle;
import uk.gov.hmcts.reform.em.stitching.service.dto.BundleDTO;

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
