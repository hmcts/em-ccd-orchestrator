package uk.gov.hmcts.reform.em.orchestrator.testutil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.springframework.http.MediaType;

import java.io.*;
import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CcdHelper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public final String createAutomatedBundlingCaseTemplate = "{\n"
            + "  \"data\": {\n"
            + "    \"caseTitle\": null,\n"
            + "    \"caseOwner\": null,\n"
            + "    \"caseCreationDate\": null,\n"
            + "    \"caseDescription\": null,\n"
            + "    \"caseComments\": null,\n"
            + "    \"caseDocuments\": [%s],\n"
            + "    \"bundleConfiguration\": \"f-tests-1-flat-docs.yaml\"\n"
            + "  },\n"
            + "  \"event\": {\n"
            + "    \"id\": \"createCase\",\n"
            + "    \"summary\": \"\",\n"
            + "    \"description\": \"\"\n"
            + "  },\n"
            + "  \"event_token\": \"%s\",\n"
            + "  \"ignore_warning\": false,\n"
            + "  \"draft_id\": null\n"
            + "}";
    public final String finishEventTemplate = "{\"event_data\": %s, \"event\": {\"id\": \"%s\"}, \"event_token\": \"%s\"}";
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
    private IdamHelper idamHelper;
    private S2sHelper s2sHelper;
    private String bundleTesterUser = String.format("bundle-tester-%s@gmail.com", Env.getTestUrl().hashCode());
    private List<String> bundleTesterUserRoles = Stream.of("caseworker-publiclaw", "ccd-import").collect(Collectors.toList());

    public CcdHelper(IdamHelper idamHelper, S2sHelper s2sHelper) {
        this.idamHelper = idamHelper;
        this.s2sHelper = s2sHelper;

    }

    public void importCcdDefinitionFile() throws Exception {

        if (Env.isManualCcdDefFileImport()) {
            return;
        }

        /*
         *curl -XPUT \
         *   http://localhost:4451/api/user-role -v \
         *   -H "Authorization: Bearer ${userToken}" \
         *   -H "ServiceAuthorization: Bearer ${serviceToken}" \
         *   -H "Content-Type: application/json" \
         *   -d '{"role":"'${role}'","security_classification":"'${classification}'"}'
         */
        Assert.assertTrue(HttpHelper.isSuccessful(ccdGwRequest()
            .contentType(ContentType.JSON)
            .body("{\"role\":\"caseworker-publiclaw\",\"security_classification\":\"PUBLIC\"}")
            .put(Env.getCcdDefApiUrl() + "/api/user-role").andReturn().getStatusCode()));

        /*
         * curl --silent \
         *   http://localhost:4451/import \
         *   -H "Authorization: Bearer ${userToken}" \
         *   -H "ServiceAuthorization: Bearer ${serviceToken}" \
         *   -F file="@$1"
         * @param file
         */
        Assert.assertTrue(HttpHelper.isSuccessful(ccdGwRequest()
                .header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
                .multiPart("file", "adv_bundling_functional_tests_ccd_def.xlsx",
                        getEnvSpecificDefinitionFile(),
                        "application/octet-stream")
                .request("POST", Env.getCcdDefApiUrl() + "/import")
                .getStatusCode()));
    }

    public JsonNode startCaseEventAndGetToken(String caseId, String triggerId) {
        try {
            return objectMapper.readTree(ccdGwRequest()
                    .header("experimental", "true")
                    .get(Env.getCcdDataApiUrl() + String.format("/cases/%s/event-triggers/%s", caseId, triggerId))
                    .andReturn().getBody().print());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public JsonNode finishCaseEvent(String caseId, String triggerId, String token, JsonNode data) {
        try {
            return objectMapper.readTree(ccdGwRequest()
                    .contentType(ContentType.JSON)
                    .header("experimental", "true")
                    .body(String.format(finishEventTemplate, data.toString(), triggerId, token))
                    .post(Env.getCcdDataApiUrl() + String.format("/cases/%s/events", caseId))
                    .andReturn().getBody().print());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String createCase(String documents) {
        Response createTriggerResponse = ccdGwRequest()
                .header("experimental", "true")
                .get(Env.getCcdDataApiUrl() + "/case-types/" + getEnvCcdCaseTypeId() + "/event-triggers/createCase")
                .andReturn();

        Assert.assertTrue(HttpHelper.isSuccessful(createTriggerResponse.getStatusCode()));

        Response createCaseResponse = ccdGwRequest()
            .contentType(ContentType.JSON)
            .body(String.format(createAutomatedBundlingCaseTemplate, documents, createTriggerResponse.jsonPath().getString("token")))
            .post(Env.getCcdDataApiUrl() + String.format("/caseworkers/%s/jurisdictions/PUBLICLAW/case-types/" + getEnvCcdCaseTypeId() + "/cases",
                    idamHelper.getUserId(bundleTesterUser))).andReturn();

        Assert.assertTrue(HttpHelper.isSuccessful(createCaseResponse.getStatusCode()));

        return createCaseResponse.jsonPath().getString("id");

    }

    public JsonNode getCase(String caseId) {
        try {
            return objectMapper.readTree(
                    ccdGwRequest()
                            .header("experimental", "true")
                            .get(Env.getCcdDataApiUrl() + String.format("/cases/%s", caseId))
                            .andReturn()
                            .getBody()
                            .print());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getEnvCcdCaseTypeId() {
        return String.format("BUND_ASYNC_%d", Env.getTestUrl().hashCode());
    }

    public InputStream getEnvSpecificDefinitionFile() throws Exception {
        Workbook workbook = new XSSFWorkbook(ClassLoader.getSystemResourceAsStream(Env.getCcdDefFileName()));
        Sheet caseEventSheet = workbook.getSheet("CaseEvent");

        caseEventSheet.getRow(5).getCell(11).setCellValue(
                String.format("%s/api/new-bundle", getCallbackUrl())
        );
        caseEventSheet.getRow(7).getCell(11).setCellValue(
                String.format("%s/api/stitch-ccd-bundles", getCallbackUrl())
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

    private String getCallbackUrl() throws Exception {
        if (Env.getTestUrl().contains("localhost")) {
            return String.format("http://%s:8080", InetAddress.getLocalHost().getHostAddress());
        } else {
            return Env.getTestUrl().replaceAll("https", "http");
        }
    }

    public void initBundleTesterUser() {
        idamHelper.getIdamToken(bundleTesterUser,bundleTesterUserRoles);
    }

    public RequestSpecification ccdGwRequest() {
        String userToken = idamHelper.getIdamToken(bundleTesterUser,bundleTesterUserRoles);

        String s2sToken = s2sHelper.getCcdGwS2sToken();


        return RestAssured.given().log().all()
                .header("Authorization", userToken)
                .header("ServiceAuthorization", s2sToken);

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



