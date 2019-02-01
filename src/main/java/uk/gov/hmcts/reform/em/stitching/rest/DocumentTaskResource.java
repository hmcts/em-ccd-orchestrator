package uk.gov.hmcts.reform.em.stitching.rest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.em.stitching.domain.enumeration.TaskState;
import uk.gov.hmcts.reform.em.stitching.rest.errors.BadRequestAlertException;
import uk.gov.hmcts.reform.em.stitching.rest.util.HeaderUtil;
import uk.gov.hmcts.reform.em.stitching.service.DocumentTaskService;
import uk.gov.hmcts.reform.em.stitching.service.dto.BundleDTO;
import uk.gov.hmcts.reform.em.stitching.service.dto.DocumentTaskDTO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * REST controller for managing DocumentTask.
 */
@RestController
@RequestMapping("/api")
public class DocumentTaskResource {

    private final Logger log = LoggerFactory.getLogger(DocumentTaskResource.class);

    private static final String ENTITY_NAME = "documentTask";

    private final DocumentTaskService documentTaskService;

    public DocumentTaskResource(DocumentTaskService documentTaskService) {
        this.documentTaskService = documentTaskService;
    }

    /**
     * POST  /stitched-bundle : Synchronously create a new stitched bundle
     *
     * @param bundleDTO the bundle to updateCase
     * @return the ResponseEntity with status 201 (Created) and with body the new documentId
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @ApiOperation(value = "Create a stitched bundle", notes = "A POST request to create a stitched bundle")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Successfully created", response = DocumentTaskDTO.class),
        @ApiResponse(code = 400, message = "Bundle not valid"),
        @ApiResponse(code = 401, message = "Unauthorised"),
        @ApiResponse(code = 403, message = "Forbidden"),
    })
    @PostMapping("/stitched-bundle")
    ////@Timed
    public ResponseEntity<DocumentTaskDTO> stitchBundle(@RequestBody BundleDTO bundleDTO, @RequestHeader(value="Authorization", required=false) String authorisationHeader) throws URISyntaxException {
        log.debug("REST request to stitch bundle : {}", bundleDTO);

        DocumentTaskDTO documentTaskDTO = new DocumentTaskDTO();
        documentTaskDTO.setBundle(bundleDTO);
        documentTaskDTO.setJwt(authorisationHeader);
        documentTaskDTO.setTaskState(TaskState.NEW);
        DocumentTaskDTO processed = documentTaskService.process(documentTaskDTO);
        DocumentTaskDTO result = documentTaskService.save(processed);

        return ResponseEntity.created(new URI("/api/document-tasks/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * POST  /document-tasks : Create a new documentTask.
     *
     * @param documentTaskDTO the documentTaskDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new documentTaskDTO, or with status 400 (Bad Request) if the documentTask has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @ApiOperation(value = "Create a documentTaskDTO", notes = "A POST request to create a documentTaskDTO")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created", response = DocumentTaskDTO.class),
            @ApiResponse(code = 400, message = "documentTaskDTO not valid, invalid id"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
    })
    @PostMapping("/document-tasks")
    ////@Timed
    public ResponseEntity<DocumentTaskDTO> createDocumentTask(@RequestBody DocumentTaskDTO documentTaskDTO, @RequestHeader(value="Authorization", required=false) String authorisationHeader) throws URISyntaxException {
        log.debug("REST request to save DocumentTask : {}", documentTaskDTO);
        if (documentTaskDTO.getId() != null) {
            throw new BadRequestAlertException("A new documentTask cannot already have an ID", ENTITY_NAME, "id exists");
        }
        documentTaskDTO.setJwt(authorisationHeader);
        documentTaskDTO.setTaskState(TaskState.NEW);
        DocumentTaskDTO result = documentTaskService.save(documentTaskDTO);

        return ResponseEntity.created(new URI("/api/document-tasks/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * GET  /document-tasks/:id : get the "id" documentTask.
     *
     * @param id the id of the documentTaskDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the documentTaskDTO, or with status 404 (Not Found)
     */
    @ApiOperation(value = "Get an existing documentTaskDTO", notes = "A GET request to retrieve a documentTaskDTO")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = DocumentTaskDTO.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
    })
    @GetMapping("/document-tasks/{id}")
    //@Timed
    public ResponseEntity<DocumentTaskDTO> getDocumentTask(@PathVariable Long id) {
        log.debug("REST request to get DocumentTask : {}", id);
        Optional<DocumentTaskDTO> documentTaskDTO = documentTaskService.findOne(id);
        return ResponseUtil.wrapOrNotFound(documentTaskDTO);
    }

}
