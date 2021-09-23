package uk.gov.hmcts.reform.em.orchestrator.config;

/**
 * Application constants.
 */
public final class Constants {

    public static final String STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG = "File Name should contain at least 2 and "
        + "not more than 50 Chars";
    public static final String FILE_NAME_WITH_51_CHARS_LENGTH = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    public static final String BUNDLE_DESCRIPTION_FIELD_LENGTH_ERROR_MSG = "Bundle Description should not contain "
        + "more than 255 Chars";
    public static final String CASE_TYPE_ID = "caseTypeId";
    public static final String JURISDICTION_ID = "jurisdictionId";
    public static final String CASE_ID = "caseId";
    public static final String SERVICE_AUTH = "serviceauthorization";
    public static final String CDAM_DEATILS = "cdamDetails";

    private Constants() {
    }
}
