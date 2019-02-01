package uk.gov.hmcts.reform.em.stitching.rest;

import okhttp3.OkHttpClient;
import okhttp3.mock.MockInterceptor;
import okhttp3.mock.Rule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.em.stitching.Application;
import uk.gov.hmcts.reform.em.stitching.domain.Bundle;
import uk.gov.hmcts.reform.em.stitching.domain.BundleTest;
import uk.gov.hmcts.reform.em.stitching.domain.DocumentTask;
import uk.gov.hmcts.reform.em.stitching.domain.enumeration.TaskState;
import uk.gov.hmcts.reform.em.stitching.repository.DocumentTaskRepository;
import uk.gov.hmcts.reform.em.stitching.rest.errors.ExceptionTranslator;
import uk.gov.hmcts.reform.em.stitching.service.DocumentTaskService;
import uk.gov.hmcts.reform.em.stitching.service.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.stitching.service.mapper.DocumentTaskMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.hmcts.reform.em.stitching.rest.TestUtil.createFormattingConversionService;

/**
 * Test class for the DocumentTaskResource REST controller.
 *
 * @see DocumentTaskResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class DocumentTaskResourceIntTest {

    private static final TaskState DEFAULT_TASK_STATE = TaskState.NEW;

    @Autowired
    private DocumentTaskRepository documentTaskRepository;

    @Autowired
    private DocumentTaskMapper documentTaskMapper;
    
    @Autowired
    private DocumentTaskService documentTaskService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private OkHttpClient okHttpClient;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private SubjectResolver<User> userResolver;

    @Value("${dm-store-app.base-url}")
    private String dmBaseUrl;

    @Value("${em-rpa-stitching-api.base-url}")
    private String emStitchingAppBaseUrl;

    private MockMvc restDocumentTaskMockMvc;

    private DocumentTask documentTask;

    private Bundle testBundle;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final DocumentTaskResource documentTaskResource = new DocumentTaskResource(documentTaskService);
        this.restDocumentTaskMockMvc = MockMvcBuilders.standaloneSetup(documentTaskResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();


    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public DocumentTask createEntity() {
        testBundle = BundleTest.getTestBundle();
        DocumentTask documentTask = new DocumentTask()
            .bundle(testBundle)
            .taskState(DEFAULT_TASK_STATE);
        documentTask.setJwt("userjwt");
        return documentTask;
    }

    @Before
    public void initTest() {
        documentTask = createEntity();
        MockInterceptor mockInterceptor = (MockInterceptor)okHttpClient.interceptors().get(0);
        mockInterceptor.reset();
    }

    @Test
    @Transactional
    public void stitchBundle() throws Exception {
        BDDMockito.given(authTokenGenerator.generate()).willReturn("s2s");
        BDDMockito.given(userResolver.getTokenDetails(documentTask.getJwt())).willReturn(new User("id", null));

        MockInterceptor mockInterceptor = (MockInterceptor) okHttpClient.interceptors().get(0);

        ClassLoader classLoader = Application.class.getClassLoader();

        mockInterceptor.addRule(new Rule.Builder().get().url(dmBaseUrl + "/documents/AAAAAAAAAA/binary")
            .respond(classLoader.getResourceAsStream("annotationTemplate.pdf")));

        mockInterceptor.addRule(new Rule.Builder().post().url(dmBaseUrl + "/documents")
            .respond("{\"_embedded\": {\"documents\": [{\"_links\":{\"self\":{\"href\":\"http://aa.bvv.com/new-doc_url\"}}}]}}"));


        // Create the DocumentTask
        DocumentTaskDTO documentTaskDTO = documentTaskMapper.toDto(documentTask);
        documentTaskDTO.getBundle().setStitchedDocId(null);

        int databaseSizeBeforeCreate = documentTaskRepository.findAll().size();

        restDocumentTaskMockMvc.perform(post("/api/stitched-bundle")
            .header("Authorization", documentTask.getJwt())
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(documentTaskDTO.getBundle())))
            .andExpect(status().isCreated());

        // Validate the DocumentTask in the database
        List<DocumentTask> documentTaskList = documentTaskRepository.findAll();
        assertThat(documentTaskList).hasSize(databaseSizeBeforeCreate + 1);
        DocumentTask testDocumentTask = documentTaskList.get(documentTaskList.size() - 1);
        assertThat(testDocumentTask.getBundle().getDescription()).isEqualTo(testBundle.getDescription());
        assertThat(testDocumentTask.getTaskState()).isEqualTo(TaskState.DONE);
        assertNotNull(testDocumentTask.getBundle().getStitchedDocId());
    }


    @Test
    @Transactional
    public void createDocumentTask() throws Exception {
        BDDMockito.given(authTokenGenerator.generate()).willReturn("s2s");
        BDDMockito.given(userResolver.getTokenDetails(documentTask.getJwt())).willReturn(new User("id", null));

        int databaseSizeBeforeCreate = documentTaskRepository.findAll().size();

        // Create the DocumentTask
        DocumentTaskDTO documentTaskDTO = documentTaskMapper.toDto(documentTask);
        documentTaskDTO.getBundle().setStitchedDocId(null);

        restDocumentTaskMockMvc.perform(post("/api/document-tasks")
            .header("Authorization", documentTask.getJwt())
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(documentTaskDTO)))
            .andExpect(status().isCreated());

        // Validate the DocumentTask in the database
        List<DocumentTask> documentTaskList = documentTaskRepository.findAll();
        assertThat(documentTaskList).hasSize(databaseSizeBeforeCreate + 1);
        DocumentTask testDocumentTask = documentTaskList.get(documentTaskList.size() - 1);
        assertThat(testDocumentTask.getBundle().getDescription()).isEqualTo(testBundle.getDescription());
        assertThat(testDocumentTask.getTaskState()).isEqualTo(TaskState.NEW);
    }

    @Test
    @Transactional
    public void createDocumentTaskWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = documentTaskRepository.findAll().size();

        // Create the DocumentTask with an existing ID
        documentTask.setId(1L);
        DocumentTaskDTO documentTaskDTO = documentTaskMapper.toDto(documentTask);

        // An entity with an existing ID cannot be created, so this API call must fail
        restDocumentTaskMockMvc.perform(post("/api/document-tasks")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(documentTaskDTO)))
            .andExpect(status().isBadRequest());

        // Validate the DocumentTask in the database
        List<DocumentTask> documentTaskList = documentTaskRepository.findAll();
        assertThat(documentTaskList).hasSize(databaseSizeBeforeCreate);
    }
    
    @Test
    @Transactional
    public void getDocumentTask() throws Exception {
        // Initialize the database
        documentTaskRepository.saveAndFlush(documentTask);

        // Get the documentTask
        restDocumentTaskMockMvc.perform(get("/api/document-tasks/{id}", documentTask.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(documentTask.getId().intValue()))
            .andExpect(jsonPath("$.bundle.description").value(documentTask.getBundle().getDescription()))
            .andExpect(jsonPath("$.taskState").value(DEFAULT_TASK_STATE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingDocumentTask() throws Exception {
        // Get the documentTask
        restDocumentTaskMockMvc.perform(get("/api/document-tasks/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DocumentTask.class);
        DocumentTask documentTask1 = new DocumentTask();
        documentTask1.setId(1L);
        DocumentTask documentTask2 = new DocumentTask();
        documentTask2.setId(documentTask1.getId());
        assertThat(documentTask1).isEqualTo(documentTask2);
        documentTask2.setId(2L);
        assertThat(documentTask1).isNotEqualTo(documentTask2);
        documentTask1.setId(null);
        assertThat(documentTask1).isNotEqualTo(documentTask2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DocumentTaskDTO.class);
        DocumentTaskDTO documentTaskDTO1 = new DocumentTaskDTO();
        documentTaskDTO1.setId(1L);
        DocumentTaskDTO documentTaskDTO2 = new DocumentTaskDTO();
        assertThat(documentTaskDTO1).isNotEqualTo(documentTaskDTO2);
        documentTaskDTO2.setId(documentTaskDTO1.getId());
        assertThat(documentTaskDTO1).isEqualTo(documentTaskDTO2);
        documentTaskDTO2.setId(2L);
        assertThat(documentTaskDTO1).isNotEqualTo(documentTaskDTO2);
        documentTaskDTO1.setId(null);
        assertThat(documentTaskDTO1).isNotEqualTo(documentTaskDTO2);
    }

}
