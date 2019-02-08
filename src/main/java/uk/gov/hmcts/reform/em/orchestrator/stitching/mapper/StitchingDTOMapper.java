package uk.gov.hmcts.reform.em.orchestrator.stitching.mapper;

import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDocumentDTO;
// import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleFolderDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleFolderDTO;

import java.util.List;
import java.util.stream.Collectors;

public class StitchingDTOMapper {

    public StitchingBundleDTO toStitchingDTO(CcdBundleDTO bundleDTO) {
        StitchingBundleDTO bundle = new StitchingBundleDTO();
        bundle.setBundleTitle(bundleDTO.getTitle());
        bundle.setDescription(bundleDTO.getDescription());
        bundle.setDocuments(getDocuments(bundleDTO.getDocuments()));
//        bundle.setFolders(getFolders(bundleDTO.getFolders()));

        return bundle;
    }

//    private List<StitchingBundleFolderDTO> getFolders(List<CcdValue<CcdBundleFolderDTO>> folders) {
//        return folders.stream().map(this::getFolder).collect(Collectors.toList());
//    }
//
//    private StitchingBundleFolderDTO getFolder(CcdValue<CcdBundleFolderDTO> folderDto) {
//        StitchingBundleFolderDTO folder = new StitchingBundleFolderDTO();
//        folder.setDescription(folderDto.getValue().getDescription());
//        folder.setFolderName(folderDto.getValue().getFolderName());
//        folder.setSortIndex(folderDto.getValue().getSortIndex());
//        folder.setDocuments(getDocuments(folderDto.getValue().getDocuments()));
//        folder.setFolders(getFolders(folderDto.getValue().getFolders()));
//
//        return folder;
//    }

    private List<StitchingBundleDocumentDTO> getDocuments(List<CcdValue<CcdBundleDocumentDTO>> documents) {
        return documents.stream().map(this::getDocument).collect(Collectors.toList());
    }

    private StitchingBundleDocumentDTO getDocument(CcdValue<CcdBundleDocumentDTO> documentDto) {
        String uri = documentDto.getValue().getDocumentUri();

        StitchingBundleDocumentDTO document = new StitchingBundleDocumentDTO();
        document.setDocTitle(documentDto.getValue().getName());
        document.setDocDescription(documentDto.getValue().getDescription());
        document.setSortIndex(documentDto.getValue().getSortIndex());
        document.setDocumentURI(uri);
        document.setDocumentId(uri.substring(uri.lastIndexOf("/") + 1));

        return document;
    }
}
