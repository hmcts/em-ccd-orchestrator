package uk.gov.hmcts.reform.em.orchestrator.stitching.mapper;

import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleFolderDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleFolderDTO;

import java.util.List;
import java.util.stream.Collectors;

public class StitchingDTOMapper {

    public StitchingBundleDTO toStitchingDTO(BundleDTO bundleDTO) {
        StitchingBundleDTO bundle = new StitchingBundleDTO();
        bundle.setBundleTitle(bundleDTO.getBundleTitle());
        bundle.setDescription(bundleDTO.getDescription());
        bundle.setDocuments(getDocuments(bundleDTO.getDocuments()));
        bundle.setFolders(getFolders(bundleDTO.getFolders()));

        return bundle;
    }

    private List<StitchingBundleFolderDTO> getFolders(List<CcdValue<BundleFolderDTO>> folders) {
        return folders.stream().map(this::getFolder).collect(Collectors.toList());
    }

    private StitchingBundleFolderDTO getFolder(CcdValue<BundleFolderDTO> folderDto) {
        StitchingBundleFolderDTO folder = new StitchingBundleFolderDTO();
        folder.setDescription(folderDto.getValue().getDescription());
        folder.setFolderName(folderDto.getValue().getFolderName());
        folder.setSortIndex(folderDto.getValue().getSortIndex());
        folder.setDocuments(getDocuments(folderDto.getValue().getDocuments()));
        folder.setFolders(getFolders(folderDto.getValue().getFolders()));

        return folder;
    }

    private List<StitchingBundleDocumentDTO> getDocuments(List<CcdValue<BundleDocumentDTO>> documents) {
        return documents.stream().map(this::getDocument).collect(Collectors.toList());
    }

    private StitchingBundleDocumentDTO getDocument(CcdValue<BundleDocumentDTO> documentDto) {
        StitchingBundleDocumentDTO document = new StitchingBundleDocumentDTO();
        document.setDocumentId(documentDto.getValue().getDocumentId());
        document.setDocumentURI(documentDto.getValue().getDocumentUri());
        document.setSortIndex(documentDto.getValue().getSortIndex());

        return document;
    }
}
