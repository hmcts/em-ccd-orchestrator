package uk.gov.hmcts.reform.em.orchestrator.stitching.dto;

import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.ImageRendering;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.ImageRenderingLocation;

public class DocumentImage {
    private String docmosisAssetId;
    private ImageRenderingLocation imageRenderingLocation;
    private int coordinateX;
    private int coordinateY;
    private ImageRendering imageRendering;

   public String getDocmosisAssetId() {
        return docmosisAssetId;
    }

    public void setDocmosisAssetId(String docmosisAssetId) {
        this.docmosisAssetId = docmosisAssetId;
    }

    public ImageRenderingLocation getImageRenderingLocation() {
        return imageRenderingLocation;
    }

    public void setImageRenderingLocation(ImageRenderingLocation imageRenderingLocation) {
        this.imageRenderingLocation = imageRenderingLocation;
    }

    public int getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(int coordinateX) {
        this.coordinateX = coordinateX;
    }

    public int getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(int coordinateY) {
        this.coordinateY = coordinateY;
    }

    public ImageRendering getImageRendering() {
        return imageRendering;
    }

    public void setImageRendering(ImageRendering imageRendering) {
        this.imageRendering = imageRendering;
    }
}
