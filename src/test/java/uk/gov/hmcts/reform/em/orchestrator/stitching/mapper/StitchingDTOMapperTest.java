package uk.gov.hmcts.reform.em.orchestrator.stitching.mapper;


import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.ImageRendering;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.ImageRenderingLocation;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBoolean;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleFolderDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentImage;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StitchingDTOMapperTest {

    @Test
    void testBundleDetails() {

        DocumentImage documentImage = new DocumentImage();
        documentImage.setDocmosisAssetId("schmcts.png");
        documentImage.setCoordinateY(50);
        documentImage.setCoordinateY(50);
        documentImage.setImageRenderingLocation(ImageRenderingLocation.firstPage);
        documentImage.setImageRendering(ImageRendering.translucent);

        CcdBundleDTO bundleDTO = new CcdBundleDTO();
        bundleDTO.setTitle("title");
        bundleDTO.setDescription("description");
        bundleDTO.setFileName("a-file.pdf");
        bundleDTO.setHasCoversheets(CcdBoolean.Yes);
        bundleDTO.setHasTableOfContents(CcdBoolean.Yes);
        bundleDTO.setHasFolderCoversheets(CcdBoolean.Yes);
        bundleDTO.setEnableEmailNotification(CcdBoolean.Yes);
        bundleDTO.setDocumentImage(documentImage);

        StitchingDTOMapper mapper = new StitchingDTOMapper();
        StitchingBundleDTO stitchingBundleDTO = mapper.toStitchingDTO(bundleDTO);

        assertEquals(bundleDTO.getTitle(), stitchingBundleDTO.getBundleTitle());
        assertEquals(bundleDTO.getDescription(), stitchingBundleDTO.getDescription());
        assertEquals(bundleDTO.getFileName(), stitchingBundleDTO.getFileName());
        assertEquals(bundleDTO.getHasCoversheets() == CcdBoolean.Yes,
            stitchingBundleDTO.getHasCoversheets());
        assertEquals(bundleDTO.getHasTableOfContents() == CcdBoolean.Yes,
            stitchingBundleDTO.getHasTableOfContents());
        assertEquals(bundleDTO.getHasFolderCoversheets() == CcdBoolean.Yes,
            stitchingBundleDTO.getHasFolderCoversheets());
        assertEquals(bundleDTO.getEnableEmailNotification() == CcdBoolean.Yes,
            stitchingBundleDTO.getEnableEmailNotification());
        assertEquals(bundleDTO.getDocumentImage().getDocmosisAssetId(),
            stitchingBundleDTO.getDocumentImage().getDocmosisAssetId());
        assertEquals(bundleDTO.getDocumentImage().getCoordinateX(),
            stitchingBundleDTO.getDocumentImage().getCoordinateX());
        assertEquals(bundleDTO.getDocumentImage().getImageRendering(),
            stitchingBundleDTO.getDocumentImage().getImageRendering());
        assertEquals(bundleDTO.getDocumentImage().getImageRenderingLocation(),
            stitchingBundleDTO.getDocumentImage().getImageRenderingLocation());
    }

    @Test
    void testBundleDocuments() {
        CcdBundleDocumentDTO document1DTO = getDocument(1);

        CcdBundleDTO bundleDTO = new CcdBundleDTO();
        bundleDTO.getDocuments().add(new CcdValue<>(document1DTO));
        bundleDTO.setEnableEmailNotification(CcdBoolean.Yes);

        StitchingDTOMapper mapper = new StitchingDTOMapper();
        StitchingBundleDTO stitchingBundleDTO = mapper.toStitchingDTO(bundleDTO);

        assertEquals(
            bundleDTO.getDocuments().getFirst().getValue().getSourceDocument().getUrl(),
            stitchingBundleDTO.getDocuments().getFirst().getDocumentURI());
        assertEquals(
            bundleDTO.getDocuments().getFirst().getValue().getName(),
            stitchingBundleDTO.getDocuments().getFirst().getDocTitle());
        assertEquals(
            bundleDTO.getDocuments().getFirst().getValue().getSortIndex(),
            stitchingBundleDTO.getDocuments().getFirst().getSortIndex());

    }

    @Test
    void testBundleFolders() {
        CcdBundleFolderDTO folder1DTO = getFolder(1);
        CcdBundleFolderDTO folder2DTO = getFolder(2);
        CcdBundleFolderDTO folder3DTO = getFolder(3);
        CcdBundleDocumentDTO document1DTO = getDocument(1);
        CcdBundleDocumentDTO document2DTO = getDocument(2);
        CcdBundleDocumentDTO document3DTO = getDocument(3);
        CcdBundleDocumentDTO document4DTO = getDocument(4);
        CcdBundleDocumentDTO document5DTO = getDocument(5);

        folder1DTO.getDocuments().add(new CcdValue<>(document1DTO));
        folder1DTO.getFolders().add(new CcdValue<>(folder2DTO));

        folder2DTO.getDocuments().add(new CcdValue<>(document2DTO));
        folder2DTO.getDocuments().add(new CcdValue<>(document3DTO));

        folder3DTO.getDocuments().add(new CcdValue<>(document4DTO));
        folder3DTO.getDocuments().add(new CcdValue<>(document5DTO));

        CcdBundleDTO bundleDTO = new CcdBundleDTO();
        bundleDTO.getFolders().add(new CcdValue<>(folder1DTO));
        bundleDTO.getFolders().add(new CcdValue<>(folder3DTO));

        StitchingDTOMapper mapper = new StitchingDTOMapper();
        StitchingBundleDTO stitchingBundleDTO = mapper.toStitchingDTO(bundleDTO);

        assertEquals(
                bundleDTO.getFolders().getFirst().getValue().getName(),
                stitchingBundleDTO.getFolders().getFirst().getFolderName());
        assertEquals(
                bundleDTO.getFolders().getFirst().getValue().getSortIndex(),
                stitchingBundleDTO.getFolders().getFirst().getSortIndex());
        assertEquals(
                bundleDTO.getFolders().getFirst().getValue()
                    .getDocuments().getFirst().getValue().getSourceDocument().getUrl(),
                stitchingBundleDTO.getFolders().getFirst().getDocuments().getFirst().getDocumentURI());
        assertEquals(
                bundleDTO.getFolders().getFirst().getValue().getDocuments().getFirst().getValue().getSortIndex(),
                stitchingBundleDTO.getFolders().getFirst().getDocuments().getFirst().getSortIndex());

        assertEquals(
                bundleDTO.getFolders().getFirst().getValue().getFolders().getFirst().getValue().getName(),
                stitchingBundleDTO.getFolders().getFirst().getFolders().getFirst().getFolderName());
        assertEquals(
                bundleDTO.getFolders().getFirst().getValue().getFolders().getFirst().getValue().getSortIndex(),
                stitchingBundleDTO.getFolders().getFirst().getFolders().getFirst().getSortIndex());
        assertEquals(
                bundleDTO.getFolders().get(0).getValue().getFolders().getFirst().getValue()
                        .getDocuments().getFirst().getValue().getSourceDocument().getUrl(),
                stitchingBundleDTO.getFolders().get(0).getFolders().getFirst()
                    .getDocuments().getFirst().getDocumentURI());
        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolders().getFirst().getValue()
                        .getDocuments().getFirst().getValue().getSortIndex(),
                stitchingBundleDTO.getFolders().get(0).getFolders().getFirst()
                    .getDocuments().getFirst().getSortIndex());

        assertEquals(
                bundleDTO.getFolders().get(1).getValue().getName(),
                stitchingBundleDTO.getFolders().get(1).getFolderName());
        assertEquals(
                bundleDTO.getFolders().get(1).getValue().getSortIndex(),
                stitchingBundleDTO.getFolders().get(1).getSortIndex());
        assertEquals(
                bundleDTO.getFolders().get(1).getValue()
                    .getDocuments().getFirst().getValue().getSourceDocument().getUrl(),
                stitchingBundleDTO.getFolders().get(1).getDocuments().getFirst().getDocumentURI());
        assertEquals(
                bundleDTO.getFolders().get(1).getValue().getDocuments().getFirst().getValue().getSortIndex(),
                stitchingBundleDTO.getFolders().get(1).getDocuments().getFirst().getSortIndex());

    }

    @Test
    void convertNullEmailNotification() {
        CcdBundleDTO bundleDTO = new CcdBundleDTO();
        bundleDTO.setTitle("title");
        bundleDTO.setDescription("description");
        bundleDTO.setFileName("a-file.pdf");
        bundleDTO.setHasCoversheets(CcdBoolean.Yes);
        bundleDTO.setHasTableOfContents(CcdBoolean.Yes);
        bundleDTO.setHasFolderCoversheets(CcdBoolean.Yes);
        bundleDTO.setEnableEmailNotification(null);

        StitchingDTOMapper mapper = new StitchingDTOMapper();
        StitchingBundleDTO stitchingBundleDTO = mapper.toStitchingDTO(bundleDTO);

        assertNull(stitchingBundleDTO.getEnableEmailNotification());
    }

    @Test
    void convertNoEmailNotification() {
        CcdBundleDTO bundleDTO = new CcdBundleDTO();
        bundleDTO.setTitle("title");
        bundleDTO.setDescription("description");
        bundleDTO.setFileName("a-file.pdf");
        bundleDTO.setHasCoversheets(CcdBoolean.Yes);
        bundleDTO.setHasTableOfContents(CcdBoolean.Yes);
        bundleDTO.setHasFolderCoversheets(CcdBoolean.Yes);
        bundleDTO.setEnableEmailNotification(CcdBoolean.No);

        StitchingDTOMapper mapper = new StitchingDTOMapper();
        StitchingBundleDTO stitchingBundleDTO = mapper.toStitchingDTO(bundleDTO);

        assertEquals(false, stitchingBundleDTO.getEnableEmailNotification());
    }

    private CcdBundleFolderDTO getFolder(int index) {
        CcdBundleFolderDTO folderDTO = new CcdBundleFolderDTO();
        folderDTO.setName(String.format("folder %s name", index));
        folderDTO.setSortIndex(index);

        return folderDTO;
    }

    private CcdBundleDocumentDTO getDocument(int index) {
        return new CcdBundleDocumentDTO(
            String.format("document %s title", index),
            String.format("document %s description", index),
            index,
                new CcdDocument(String.format("/document/%s", index), "xxx",
                        String.format("/document/%s/binary", index))

        );
    }

}
