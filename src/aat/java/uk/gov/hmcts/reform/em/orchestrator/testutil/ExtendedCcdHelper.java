package uk.gov.hmcts.reform.em.orchestrator.testutil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.em.test.ccddata.CcdDataHelper;
import uk.gov.hmcts.reform.em.test.ccddefinition.CcdDefinitionHelper;
import uk.gov.hmcts.reform.em.test.idam.IdamHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExtendedCcdHelper {

    public static final String CCD_BUNDLE_MVP_TYPE_ASYNC = "CCD_BUNDLE_MVP_TYPE_ASYNC";

    @Value("${test.url}")
    private String testUrl;

    @Autowired
    private IdamHelper idamHelper;

    @Autowired
    private CcdDataHelper ccdDataHelper;

    @Autowired
    private CcdDefinitionHelper ccdDefinitionHelper;


    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public final String createAutomatedBundlingCaseTemplate = "{\n"
            + "    \"caseTitle\": null,\n"
            + "    \"caseOwner\": null,\n"
            + "    \"caseCreationDate\": null,\n"
            + "    \"caseDescription\": null,\n"
            + "    \"caseComments\": null,\n"
            + "    \"caseDocuments\": [%s],\n"
            + "    \"bundleConfiguration\": \"test-files/f-tests-1-flat-docs.yaml\"\n"
            + "  }";
    public final String createCdamAutomatedBundlingCaseTemplate = "{\n"
        + "    \"caseTitle\": null,\n"
        + "    \"caseOwner\": null,\n"
        + "    \"caseCreationDate\": null,\n"
        + "    \"caseDescription\": null,\n"
        + "    \"caseComments\": null,\n"
        + "    \"caseDocuments\": %s,\n"
        + "    \"bundleConfiguration\": \"test-files/f-tests-1-flat-docs.yaml\"\n"
        + "  }";
    public final String documentTemplate = "{\n"
                    + "        \"value\": {\n"
                    + "          \"documentName\": \"%s\",\n"
                    + "          \"documentLink\": {\n"
                    + "            \"document_url\": \"%s\",\n"
                    + "            \"document_binary_url\": \"%s/binary\",\n"
                    + "            \"document_filename\": \"%s\"\n"
                    + "          }\n"
                    + "        }\n"
                    + "      }";
    @Getter
    private String bundleTesterUser;
    private List<String> bundleTesterUserRoles = Stream.of("caseworker", "caseworker-publiclaw", "ccd-import")
        .collect(Collectors.toList());

    @PostConstruct
    public void init() throws Exception {
        initBundleTesterUser();
        importCcdDefinitionFile();
    }


    public void importCcdDefinitionFile() throws Exception {

        ccdDefinitionHelper.importDefinitionFile(
                bundleTesterUser,
                "caseworker-publiclaw",
                getEnvSpecificDefinitionFile());

    }

    public CaseDetails createCase(String documents) throws Exception {
        return ccdDataHelper.createCase(bundleTesterUser, "PUBLICLAW", getEnvCcdCaseTypeId(), "createCase",
                objectMapper.readTree(String.format(createAutomatedBundlingCaseTemplate, documents)));
    }

    public CaseDetails createCdamCase(String documents) throws Exception {
        return ccdDataHelper.createCase(bundleTesterUser, "PUBLICLAW", getEnvCcdCaseTypeId(), "createCase",
            objectMapper.readTree(String.format(createCdamAutomatedBundlingCaseTemplate, documents)));
    }

    public JsonNode triggerEvent(String caseId, String eventId) throws Exception {
        return objectMapper.readTree(objectMapper.writeValueAsString(
            ccdDataHelper.triggerEvent(bundleTesterUser, caseId, eventId)));
    }

    public JsonNode getCase(String caseId) throws Exception {
        return objectMapper.readTree(objectMapper.writeValueAsString(ccdDataHelper.getCase(bundleTesterUser, caseId)));
    }

    public String getEnvCcdCaseTypeId() {
        return CCD_BUNDLE_MVP_TYPE_ASYNC;
    }

    public InputStream getEnvSpecificDefinitionFile() throws Exception {
        Workbook workbook = new XSSFWorkbook(ClassLoader
            .getSystemResourceAsStream("adv_bundling_functional_tests_ccd_def.xlsx"));
        Sheet caseEventSheet = workbook.getSheet("CaseEvent");

        caseEventSheet.getRow(5).getCell(11).setCellValue(
                String.format("%s/api/new-bundle", getCallbackUrl())
        );
        caseEventSheet.getRow(7).getCell(11).setCellValue(
                String.format("%s/api/async-stitch-ccd-bundles", getCallbackUrl())
        );
        caseEventSheet.getRow(8).getCell(11).setCellValue(
                String.format("%s/api/clone-ccd-bundles", getCallbackUrl())
        );

        File outputFile = File.createTempFile("ccd", "ftest-def");

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            workbook.write(fileOutputStream);
        }

        return new FileInputStream(outputFile);
    }

    private String getCallbackUrl() {
        if (testUrl.contains("localhost")) {
            return "http://localhost:8080";
        } else {
            return testUrl;
        }
    }

    public void initBundleTesterUser() {
        bundleTesterUser = "bundle-tester@gmail.com";
        idamHelper.createUser(bundleTesterUser, bundleTesterUserRoles);
    }

    public String getCcdDocumentJson(String documentName, String dmUrl, String fileName) {
        return String.format(documentTemplate, documentName, dmUrl, dmUrl, fileName);
    }

    public JsonNode assignEnvCcdCaseTypeIdToCase(JsonNode ccdCase) {
        ((ObjectNode) ccdCase.get("case_details")).put("case_type_id", getEnvCcdCaseTypeId());
        return ccdCase;
    }

    public JsonNode loadCaseFromFile(String file) throws Exception {
        return assignEnvCcdCaseTypeIdToCase(
                objectMapper.readTree(ClassLoader.getSystemResource(file)));
    }

    public JsonNode loadMissingPropertiesCase(String file) throws IOException {
        return objectMapper.readTree(ClassLoader.getSystemResource(file));
    }

    //////// CDAM //////////

}



