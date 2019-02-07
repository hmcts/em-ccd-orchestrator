package uk.gov.hmcts.reform.em.orchestrator.stitching.mapper;

import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleFolderDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;

import static org.junit.Assert.*;

public class StitchingDTOMapperTest {

    @Test
    public void convert() {
        BundleFolderDTO folder1DTO = getFolder(1);
        BundleFolderDTO folder2DTO = getFolder(2);
        BundleFolderDTO folder3DTO = getFolder(3);
        BundleDocumentDTO document1DTO = getDocument(1);
        BundleDocumentDTO document2DTO = getDocument(2);
        BundleDocumentDTO document3DTO = getDocument(3);
        BundleDocumentDTO document4DTO = getDocument(4);
        BundleDocumentDTO document5DTO = getDocument(5);

        folder1DTO.getDocuments().add(new CcdValue<>(document1DTO));
        folder1DTO.getFolders().add(new CcdValue<>(folder2DTO));

        folder2DTO.getDocuments().add(new CcdValue<>(document2DTO));
        folder2DTO.getDocuments().add(new CcdValue<>(document3DTO));

        folder3DTO.getDocuments().add(new CcdValue<>(document4DTO));
        folder3DTO.getDocuments().add(new CcdValue<>(document5DTO));

        BundleDTO bundleDTO = new BundleDTO();
        bundleDTO.setBundleTitle("title");
        bundleDTO.setDescription("description");
        bundleDTO.getFolders().add(new CcdValue<>(folder1DTO));
        bundleDTO.getFolders().add(new CcdValue<>(folder3DTO));

        StitchingDTOMapper mapper = new StitchingDTOMapper();
        StitchingBundleDTO stitchingBundleDTO = mapper.toStitchingDTO(bundleDTO);

        assertEquals(bundleDTO.getBundleTitle(), stitchingBundleDTO.getBundleTitle());
        assertEquals(bundleDTO.getDescription(), stitchingBundleDTO.getDescription());

        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolderName(), stitchingBundleDTO.getFolders().get(0).getFolderName());
        assertEquals(bundleDTO.getFolders().get(0).getValue().getDescription(), stitchingBundleDTO.getFolders().get(0).getDescription());
        assertEquals(bundleDTO.getFolders().get(0).getValue().getSortIndex(), stitchingBundleDTO.getFolders().get(0).getSortIndex());
        assertEquals(bundleDTO.getFolders().get(0).getValue().getDocuments().get(0).getValue().getDocumentURI(), stitchingBundleDTO.getFolders().get(0).getDocuments().get(0).getDocumentURI());
        assertEquals(bundleDTO.getFolders().get(0).getValue().getDocuments().get(0).getValue().getDocumentId(), stitchingBundleDTO.getFolders().get(0).getDocuments().get(0).getDocumentId());
        assertEquals(bundleDTO.getFolders().get(0).getValue().getDocuments().get(0).getValue().getSortIndex(), stitchingBundleDTO.getFolders().get(0).getDocuments().get(0).getSortIndex());

        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue().getFolderName(), stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getFolderName());
        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue().getDescription(), stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getDescription());
        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue().getSortIndex(), stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getSortIndex());
        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue().getDocuments().get(0).getValue().getDocumentURI(), stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getDocuments().get(0).getDocumentURI());
        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue().getDocuments().get(0).getValue().getDocumentId(), stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getDocuments().get(0).getDocumentId());
        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue().getDocuments().get(0).getValue().getSortIndex(), stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getDocuments().get(0).getSortIndex());

        assertEquals(bundleDTO.getFolders().get(1).getValue().getFolderName(), stitchingBundleDTO.getFolders().get(1).getFolderName());
        assertEquals(bundleDTO.getFolders().get(1).getValue().getDescription(), stitchingBundleDTO.getFolders().get(1).getDescription());
        assertEquals(bundleDTO.getFolders().get(1).getValue().getSortIndex(), stitchingBundleDTO.getFolders().get(1).getSortIndex());
        assertEquals(bundleDTO.getFolders().get(1).getValue().getDocuments().get(0).getValue().getDocumentURI(), stitchingBundleDTO.getFolders().get(1).getDocuments().get(0).getDocumentURI());
        assertEquals(bundleDTO.getFolders().get(1).getValue().getDocuments().get(0).getValue().getDocumentId(), stitchingBundleDTO.getFolders().get(1).getDocuments().get(0).getDocumentId());
        assertEquals(bundleDTO.getFolders().get(1).getValue().getDocuments().get(0).getValue().getSortIndex(), stitchingBundleDTO.getFolders().get(1).getDocuments().get(0).getSortIndex());

    }

    private BundleFolderDTO getFolder(int index) {
        BundleFolderDTO folderDTO = new BundleFolderDTO();
        folderDTO.setFolderName(String.format("folder %s name", index));
        folderDTO.setDescription(String.format("folder %s description", index));
        folderDTO.setSortIndex(index);

        return folderDTO;
    }

    private BundleDocumentDTO getDocument(int index) {
        BundleDocumentDTO documentDTO = new BundleDocumentDTO();
        documentDTO.setDocTitle(String.format("document %s title", index));
        documentDTO.setDocumentURI(String.format("/document/%s", index));
        documentDTO.setSortIndex(index);

        return documentDTO;
    }

}