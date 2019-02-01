package uk.gov.hmcts.reform.em.stitching.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

public class BundleTest {
    private static final String DEFAULT_DOCUMENT_ID = "AAAAAAAAAA";

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Test
    public void serializesToJson() throws JsonProcessingException {
        Bundle bundle = BundleTest.getTestBundle();

        String result = mapper.writeValueAsString(bundle);

        assertThat(result, containsString("My bundle"));
        assertThat(result, containsString("2019-01-09T14:00:00Z"));
    }

    public static Bundle getTestBundle() {
        BundleDocument bundleDocument = new BundleDocument();
        bundleDocument.setDocumentId(DEFAULT_DOCUMENT_ID);

        Bundle bundle = new Bundle();
        bundle.setBundleTitle("My bundle");
        bundle.setVersion(1);
        bundle.setDescription("Bundle description");
        bundle.setCreatedDate(Instant.parse("2019-01-09T14:00:00Z"));
        bundle.setCreatedBy("Billy Bob");
        bundle.setDocuments(Collections.singletonList(bundleDocument));
        bundle.setFolders(new ArrayList<>());

        return bundle;

    }

    /**
     * Create Bundle structure:
     *
     * Bundle:
     *  - document1
     *  - folder1
     *    - folder1document1
     *    - folder1document2
     *  - folder2
     *    - folder2document1
     *    - folder3
     *      - folder3document1
     *  - document2
     *
     * And expect the documents to be sorted as:
     *
     * [document1, folder1document1, folder1document2, folder2document1, folder3document1, document2]
     *
     * Note that in the test they are deliberately added out of order.
     */
    @Test
    public void sortsItems() {
        Bundle bundle = BundleTest.getTestBundle();
        bundle.setDocuments(new ArrayList<>());
        bundle.setFolders(new ArrayList<>());
        BundleDocument document1 = getBundleDocument(1);

        BundleFolder folder1 = getBundleFolder(2);
        BundleDocument folder1document1 = getBundleDocument(1);
        BundleDocument folder1document2 = getBundleDocument(2);

        BundleFolder folder2 = getBundleFolder(3);
        BundleDocument folder2document1 = getBundleDocument(1);

        BundleFolder folder3 = getBundleFolder(2);
        BundleDocument folder3document1 = getBundleDocument(1);

        BundleDocument document2 = getBundleDocument(4);

        bundle.getDocuments().add(document2);
        bundle.getDocuments().add(document1);
        bundle.getFolders().add(folder1);
        bundle.getFolders().add(folder2);

        folder1.getDocuments().add(folder1document2);
        folder1.getDocuments().add(folder1document1);

        folder2.getFolders().add(folder3);
        folder2.getDocuments().add(folder2document1);

        folder3.getDocuments().add(folder3document1);

        List<BundleDocument> result = bundle.getSortedItems().collect(Collectors.toList());
        List<BundleDocument> expected = Stream.of(
            document1,
            folder1document1,
            folder1document2,
            folder2document1,
            folder3document1,
            document2
        ).collect(Collectors.toList());

        assertEquals(expected.size(), result.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), result.get(i));
        }
    }

    private static BundleDocument getBundleDocument(int index) {
        BundleDocument doc = new BundleDocument();
        doc.setSortIndex(index);

        return doc;
    }

    private static BundleFolder getBundleFolder(int index) {
        BundleFolder doc = new BundleFolder();
        doc.setDocuments(new ArrayList<>());
        doc.setFolders(new ArrayList<>());
        doc.setSortIndex(index);

        return doc;
    }

}