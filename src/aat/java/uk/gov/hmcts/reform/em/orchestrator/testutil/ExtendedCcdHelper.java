package uk.gov.hmcts.reform.em.orchestrator.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.stream.Stream;

@Service
public class ExtendedCcdHelper {

    public static final String CCD_BUNDLE_MVP_TYPE_ASYNC = "CCD_BUNDLE_MVP_TYPE_ASYNC";

    @Value("${test.url}")
    private String testUrl;

    private IdamHelper idamHelper;

    private CcdDataHelper ccdDataHelper;

    private CcdDefinitionHelper ccdDefinitionHelper;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static final String CREATE_AUTOMATED_BUNDLING_CASE_TEMPLATE = """
        {
            "caseTitle": null,
            "caseOwner": null,
            "caseCreationDate": null,
            "caseDescription": null,
            "caseComments": null,
            "caseDocuments": [%s],
            "bundleConfiguration": "f-tests-1-flat-docs.yaml"
          }""";
    public static final String CREATE_CDAM_AUTOMATED_BUNDLING_CASE_TEMPLATE = """
        {
            "caseTitle": null,
            "caseOwner": null,
            "caseCreationDate": null,
            "caseDescription": null,
            "caseComments": null,
            "caseDocuments": %s,
            "bundleConfiguration": "f-tests-1-flat-docs.yaml"
          }""";
    public static final String DOCUMENT_TEMPLATE = """
        {
                "value": {
                  "documentName": "%s",
                  "documentLink": {
                    "document_url": "%s",
                    "document_binary_url": "%s/binary",
                    "document_filename": "%s"
                  }
                }
              }""";
    @Getter
    private String bundleTesterUser;
    private final List<String> bundleTesterUserRoles = Stream.of("caseworker", "caseworker-publiclaw", "ccd-import")
        .toList();


    @Autowired
    public ExtendedCcdHelper(
            IdamHelper idamHelper,
            CcdDataHelper ccdDataHelper,
            CcdDefinitionHelper ccdDefinitionHelper
    ) {
        this.idamHelper = idamHelper;
        this.ccdDataHelper = ccdDataHelper;
        this.ccdDefinitionHelper = ccdDefinitionHelper;
    }

    @PostConstruct
    public void init() throws IOException {
        initBundleTesterUser();
        importCcdDefinitionFile();
    }


    public void importCcdDefinitionFile() throws IOException {

        ccdDefinitionHelper.importDefinitionFile(
                bundleTesterUser,
                "caseworker-publiclaw",
                getEnvSpecificDefinitionFile());

    }

    public CaseDetails createCase(String documents) throws JsonProcessingException {
        return ccdDataHelper.createCase(bundleTesterUser, "PUBLICLAW", getEnvCcdCaseTypeId(), "createCase",
                objectMapper.readTree(String.format(CREATE_AUTOMATED_BUNDLING_CASE_TEMPLATE, documents)));
    }

    public CaseDetails createCdamCase(String documents) throws JsonProcessingException {
        return ccdDataHelper.createCase(bundleTesterUser, "PUBLICLAW", getEnvCcdCaseTypeId(), "createCase",
            objectMapper.readTree(String.format(CREATE_CDAM_AUTOMATED_BUNDLING_CASE_TEMPLATE, documents)));
    }

    public JsonNode triggerEvent(String caseId, String eventId) throws JsonProcessingException {
        return objectMapper.readTree(objectMapper.writeValueAsString(
            ccdDataHelper.triggerEvent(bundleTesterUser, caseId, eventId)));
    }

    public JsonNode getCase(String caseId) throws JsonProcessingException {
        return objectMapper.readTree(objectMapper.writeValueAsString(ccdDataHelper.getCase(bundleTesterUser, caseId)));
    }

    public String getEnvCcdCaseTypeId() {
        return CCD_BUNDLE_MVP_TYPE_ASYNC;
    }

    public InputStream getEnvSpecificDefinitionFile() throws IOException {
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
        return String.format(DOCUMENT_TEMPLATE, documentName, dmUrl, dmUrl, fileName);
    }

    public JsonNode assignEnvCcdCaseTypeIdToCase(JsonNode ccdCase) {
        ((ObjectNode) ccdCase.get("case_details")).put("case_type_id", getEnvCcdCaseTypeId());
        return ccdCase;
    }

    public JsonNode loadCaseFromFile(String file) throws IOException {
        return assignEnvCcdCaseTypeIdToCase(
                objectMapper.readTree(ClassLoader.getSystemResource(file)));
    }

    public JsonNode loadMissingPropertiesCase(String file) throws IOException {
        return objectMapper.readTree(ClassLoader.getSystemResource(file));
    }

}



