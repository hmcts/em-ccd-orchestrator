package uk.gov.hmcts.reform.em.orchestrator.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.LambdaDslObject;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.PageNumberFormat;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundlePaginationStyle;

import java.util.UUID;
import java.util.function.Consumer;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;


public final class ConsumerTestUtil {

    private ConsumerTestUtil() {
        // Utility class
    }

    public static void buildCcdDocumentDsl(LambdaDslObject doc) {
        doc
            .stringType("document_url", "http://dm-store:8080/documents/b9a3416c-66d4-4a24-9580-a631e78d1275")
            .stringType("document_binary_url", "http://dm-store:8080/documents/b9a3416c-66d4-4a24-9580-a631e78d1275/binary")
            .stringType("document_filename", "stitched.pdf")
            .stringType("document_hash", "sha256-c38944298e827135e533f7c4621d34b4139f408990c6d7a5a894769a6c9d7491");
    }

    public static void buildCcdBundleDsl(LambdaDslObject bundle) {
        bundle
            .uuid("id", UUID.fromString("a585a03b-a521-443b-826c-9411ebd44733"))
            .stringType("title", "Test Bundle")
            .stringType("description", "This is a test bundle description.")
            .stringMatcher("eligibleForStitching","Yes|No", "Yes")
            .stringMatcher("eligibleForCloning", "Yes|No", "No")
            .stringMatcher("fileName", "^[-._A-Za-z0-9]+$", "bundle-filename")
            .stringType("fileNameIdentifier", "test-identifier")
            .stringType("coverpageTemplate", "FL-FRM-APP-ENG-00002.docx")
            .stringMatcher("hasTableOfContents", "Yes|No", "Yes")
            .stringMatcher("hasCoversheets", "Yes|No", "Yes")
            .stringMatcher("hasFolderCoversheets", "Yes|No", "No")
            .stringType("stitchStatus", "DONE")
            .stringMatcher("paginationStyle", buildEnumRegex(CcdBundlePaginationStyle.class), "off")
            .stringMatcher("pageNumberFormat", buildEnumRegex(PageNumberFormat.class), "numberOfPages")
            .stringType("stitchingFailureMessage", "")
            .stringMatcher("enableEmailNotification", "Yes|No", "Yes")
            .eachLike("documents", doc ->
                doc.object("value", val -> val.stringType("name", "DocumentName")))
            .eachLike("folders", folder ->
                folder.object("value", val -> val.stringType("name", "FolderName")));
    }

    public static void buildCcdCallbackRequest(LambdaDslObject body, Consumer<LambdaDslObject> caseDataBuilder) {
        body
            .stringType("event_id", "someEventId")
            .object("case_details", details ->
                details.object("case_data", caseDataBuilder)
            );
    }

    public static void buildCcdCallbackResponse(LambdaDslObject body, Consumer<LambdaDslObject> dataBuilder) {
        body
            .object("data", dataBuilder)
            .array("errors", arr -> {})
            .array("warnings", arr -> {});
    }

    public static DslPart createCloneRequestDsl() {
        return newJsonBody(body -> buildCcdCallbackRequest(body, data ->
            data.eachLike("caseBundles", bundle ->
                bundle.object("value", value -> {
                    buildCcdBundleDsl(value);
                    value.stringMatcher("eligibleForCloning", "Yes|No", "Yes");
                })
            )
        )).build();
    }

    public static DslPart createCloneResponseDsl() {
        return newJsonBody(body -> buildCcdCallbackResponse(body, data ->
            data.minArrayLike("caseBundles", 2, bundle ->
                bundle.object("value", value -> {
                    buildCcdBundleDsl(value);
                    value.stringMatcher("eligibleForCloning", "Yes|No", "No");
                })
            )
        )).build();
    }

    private static String buildEnumRegex(Class<? extends Enum<?>> enumClass) {
        StringBuilder regex = new StringBuilder();
        for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
            if (!regex.isEmpty()) {
                regex.append("|");
            }
            regex.append(enumConstant.name());
        }
        return regex.toString();
    }
}