package uk.gov.hmcts.reform.em.orchestrator.stitching.mapper;

import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;

import static org.junit.Assert.*;

public class StitchingDTOMapperTest {

    @Test
    public void convert() {
//        BundleFolderDTO folder1DTO = getFolder(1);
//        BundleFolderDTO folder2DTO = getFolder(2);
//        BundleFolderDTO folder3DTO = getFolder(3);
        CcdBundleDocumentDTO document1DTO = getDocument(1);
        CcdBundleDocumentDTO document2DTO = getDocument(2);
        CcdBundleDocumentDTO document3DTO = getDocument(3);
        CcdBundleDocumentDTO document4DTO = getDocument(4);
        CcdBundleDocumentDTO document5DTO = getDocument(5);

//        folder1DTO.getDocuments().add(new CcdValue<>(document1DTO));
//        folder1DTO.getFolders().add(new CcdValue<>(folder2DTO));
//
//        folder2DTO.getDocuments().add(new CcdValue<>(document2DTO));
//        folder2DTO.getDocuments().add(new CcdValue<>(document3DTO));
//
//        folder3DTO.getDocuments().add(new CcdValue<>(document4DTO));
//        folder3DTO.getDocuments().add(new CcdValue<>(document5DTO));

        CcdBundleDTO bundleDTO = new CcdBundleDTO();
        bundleDTO.setTitle("title");
        bundleDTO.setDescription("description");
        bundleDTO.getDocuments().add(new CcdValue<>(document1DTO));
//        bundleDTO.getFolders().add(new CcdValue<>(folder1DTO));
//        bundleDTO.getFolders().add(new CcdValue<>(folder3DTO));

        StitchingDTOMapper mapper = new StitchingDTOMapper();
        StitchingBundleDTO stitchingBundleDTO = mapper.toStitchingDTO(bundleDTO);

        assertEquals(bundleDTO.getTitle(), stitchingBundleDTO.getBundleTitle());
        assertEquals(bundleDTO.getDescription(), stitchingBundleDTO.getDescription());

        assertEquals(bundleDTO.getDocuments().get(0).getValue().getDocumentUri(), stitchingBundleDTO.getDocuments().get(0).getDocumentURI());
        assertEquals(bundleDTO.getDocuments().get(0).getValue().getName(), stitchingBundleDTO.getDocuments().get(0).getDocTitle());
        assertEquals(bundleDTO.getDocuments().get(0).getValue().getSortIndex(), (Integer) stitchingBundleDTO.getDocuments().get(0).getSortIndex());

//        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolderName(), stitchingBundleDTO.getFolders().get(0).getFolderName());
//        assertEquals(bundleDTO.getFolders().get(0).getValue().getDescription(), stitchingBundleDTO.getFolders().get(0).getDescription());
//        assertEquals(bundleDTO.getFolders().get(0).getValue().getSortIndex(), stitchingBundleDTO.getFolders().get(0).getSortIndex());
//        assertEquals(bundleDTO.getFolders().get(0).getValue().getDocuments().get(0).getValue().getDocumentUri(), stitchingBundleDTO.getFolders().get(0).getDocuments().get(0).getDocumentURI());
//        assertEquals(bundleDTO.getFolders().get(0).getValue().getDocuments().get(0).getValue().getDocumentId(), stitchingBundleDTO.getFolders().get(0).getDocuments().get(0).getDocumentId());
//        assertEquals(bundleDTO.getFolders().get(0).getValue().getDocuments().get(0).getValue().getSortIndex(), stitchingBundleDTO.getFolders().get(0).getDocuments().get(0).getSortIndex());
//
//        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue().getFolderName(), stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getFolderName());
//        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue().getDescription(), stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getDescription());
//        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue().getSortIndex(), stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getSortIndex());
//        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue().getDocuments().get(0).getValue().getDocumentUri(), stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getDocuments().get(0).getDocumentURI());
//        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue().getDocuments().get(0).getValue().getDocumentId(), stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getDocuments().get(0).getDocumentId());
//        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue().getDocuments().get(0).getValue().getSortIndex(), stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getDocuments().get(0).getSortIndex());
//
//        assertEquals(bundleDTO.getFolders().get(1).getValue().getFolderName(), stitchingBundleDTO.getFolders().get(1).getFolderName());
//        assertEquals(bundleDTO.getFolders().get(1).getValue().getDescription(), stitchingBundleDTO.getFolders().get(1).getDescription());
//        assertEquals(bundleDTO.getFolders().get(1).getValue().getSortIndex(), stitchingBundleDTO.getFolders().get(1).getSortIndex());
//        assertEquals(bundleDTO.getFolders().get(1).getValue().getDocuments().get(0).getValue().getDocumentUri(), stitchingBundleDTO.getFolders().get(1).getDocuments().get(0).getDocumentURI());
//        assertEquals(bundleDTO.getFolders().get(1).getValue().getDocuments().get(0).getValue().getDocumentId(), stitchingBundleDTO.getFolders().get(1).getDocuments().get(0).getDocumentId());
//        assertEquals(bundleDTO.getFolders().get(1).getValue().getDocuments().get(0).getValue().getSortIndex(), stitchingBundleDTO.getFolders().get(1).getDocuments().get(0).getSortIndex());

    }

//    private BundleFolderDTO getFolder(int index) {
//        BundleFolderDTO folderDTO = new BundleFolderDTO();
//        folderDTO.setFolderName(String.format("folder %s name", index));
//        folderDTO.setDescription(String.format("folder %s description", index));
//        folderDTO.setSortIndex(index);
//
//        return folderDTO;
//    }

    private CcdBundleDocumentDTO getDocument(int index) {
        CcdBundleDocumentDTO documentDTO = new CcdBundleDocumentDTO(
            String.format("document %s title", index),
            String.format("document %s description", index),
            index,
            String.format("/document/%s", index)
        );

        return documentDTO;
    }

}