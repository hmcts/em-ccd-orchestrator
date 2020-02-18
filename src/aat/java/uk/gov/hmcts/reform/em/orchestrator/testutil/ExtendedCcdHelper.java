package uk.gov.hmcts.reform.em.orchestrator.testutil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
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

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExtendedCcdHelper {

    @Value("${test.url}")
    private String testUrl;

    @Autowired
    private IdamHelper idamHelper;

    @Autowired
    private CcdDataHelper ccdDataHelper;

    @Autowired
    private CcdDefinitionHelper ccdDefinitionHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public final String createAutomatedBundlingCaseTemplate = "{\n"
            + "    \"caseTitle\": null,\n"
            + "    \"caseOwner\": null,\n"
            + "    \"caseCreationDate\": null,\n"
            + "    \"caseDescription\": null,\n"
            + "    \"caseComments\": null,\n"
            + "    \"caseDocuments\": [%s],\n"
            + "    \"bundleConfiguration\": \"f-tests-1-flat-docs.yaml\"\n"
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
    private String bundleTesterUser;
    private List<String> bundleTesterUserRoles = Stream.of("caseworker", "caseworker-publiclaw", "ccd-import").collect(Collectors.toList());

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

    public JsonNode triggerEvent(String caseId, String eventId) throws Exception {
        return objectMapper.readTree(objectMapper.writeValueAsString(ccdDataHelper.triggerEvent(bundleTesterUser, caseId, eventId)));
    }

    public JsonNode getCase(String caseId) throws Exception {
        return objectMapper.readTree(objectMapper.writeValueAsString(ccdDataHelper.getCase(bundleTesterUser, caseId)));
    }

    public String getEnvCcdCaseTypeId() {
        return String.format("BUND_ASYNC_%d", testUrl.hashCode());
    }

    public InputStream getEnvSpecificDefinitionFile() throws Exception {
        Workbook workbook = new XSSFWorkbook(ClassLoader.getSystemResourceAsStream("adv_bundling_functional_tests_ccd_def.xlsx"));
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

        Sheet caseTypeSheet = workbook.getSheet("CaseType");

        caseTypeSheet.getRow(3).getCell(3).setCellValue(getEnvCcdCaseTypeId());

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType().equals(CellType.STRING)
                            && cell.getStringCellValue().trim().equals("CCD_BUNDLE_MVP_TYPE_ASYNC")) {
                        cell.setCellValue(getEnvCcdCaseTypeId());
                    }
                    if (cell.getCellType().equals(CellType.STRING)
                            && cell.getStringCellValue().trim().equals("bundle-tester@gmail.com")) {
                        cell.setCellValue(bundleTesterUser);
                    }
                }
            }
        }

        File outputFile = File.createTempFile("ccd", "ftest-def");

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            workbook.write(fileOutputStream);
        }

        return new FileInputStream(outputFile);
    }

    private String getCallbackUrl() {
        if (testUrl.contains("localhost")) {
            return "http://rpa-em-ccd-orchestrator:8080";
        } else {
            return testUrl.replaceAll("https", "http");
        }
    }

    public void initBundleTesterUser() {
        bundleTesterUser = String.format("bundle-tester-%d@gmail.com", testUrl.hashCode());
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

}



