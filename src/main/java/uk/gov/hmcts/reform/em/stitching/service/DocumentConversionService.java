package uk.gov.hmcts.reform.em.stitching.service;

import java.io.File;
import java.io.IOException;

public interface DocumentConversionService {

    File convert(File originalFile) throws IOException;

}
