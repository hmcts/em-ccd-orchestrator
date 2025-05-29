package uk.gov.hmcts.reform.em.orchestrator.service;

import jakarta.servlet.ReadListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
class CachedBodyServletInputStreamUnitTest {

    private CachedBodyServletInputStream servletInputStream;

    @AfterEach
    void cleanUp() throws IOException {
        if (Objects.nonNull(servletInputStream)) {
            servletInputStream.close();
        }
    }

    @Test
    void testGivenServletInputStreamCreated_whenCalledisFinished_Thenfalse() {
        // Given
        byte[] cachedBody = "{\"firstName\" :\"abc\",\"lastName\" : \"xyz\",\"age\" : 30\"}".getBytes();
        servletInputStream = new CachedBodyServletInputStream(cachedBody);

        // when
        boolean finished = servletInputStream.isFinished();

        // then
        assertFalse(finished);
    }

    @Test
    void testGivenServletInputStreamCreatedAndBodyRead_whenCalledisFinished_ThenTrue() throws IOException {
        // Given
        byte[] cachedBody = "{\"firstName\" :\"abc\",\"lastName\" : \"xyz\",\"age\" : 30\"}".getBytes();
        servletInputStream = new CachedBodyServletInputStream(cachedBody);
        StreamUtils.copyToByteArray(servletInputStream);

        // when
        boolean finished = servletInputStream.isFinished();

        // then
        assertTrue(finished);
    }

    @Test
    void testGivenServletInputStreamCreatedAndBodyRead_whenCalledIsReady_ThenTrue() {
        // Given
        byte[] cachedBody = "{\"firstName\" :\"abc\",\"lastName\" : \"xyz\",\"age\" : 30\"}".getBytes();
        servletInputStream = new CachedBodyServletInputStream(cachedBody);

        // when
        boolean ready = servletInputStream.isReady();

        // then
        assertTrue(ready);
    }

    @Test
    void testGivenServletInputStreamCreated_whenCalledIsRead_ThenReturnsBody() throws IOException {
        // Given
        byte[] cachedBody = "{\"firstName\" :\"abc\",\"lastName\" : \"xyz\",\"age\" : 30\"}".getBytes();
        servletInputStream = new CachedBodyServletInputStream(cachedBody);

        // when
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int len;
        byte[] buffer = new byte[1024];
        while ((len = servletInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }

        // then
        assertEquals(new String(cachedBody), byteArrayOutputStream.toString());
    }

    @Test
    void testGivenServletInputStreamCreated_whenCalledIsRead_ThenThrowsException() {
        // Given
        byte[] cachedBody = "{\"firstName\" :\"abc\",\"lastName\" : \"xyz\",\"age\" : 30\"}".getBytes();
        servletInputStream = new CachedBodyServletInputStream(cachedBody);

        // when & then
        assertThrows(UnsupportedOperationException.class,
            () -> servletInputStream.setReadListener(Mockito.mock(ReadListener.class)));
    }

    @Test
    void testIsFinishedWhenAvailableThrowsIOException() throws Exception {
        byte[] cachedBody = "test data".getBytes();
        servletInputStream = new CachedBodyServletInputStream(cachedBody);

        InputStream mockInputStream = Mockito.mock(InputStream.class);
        Mockito.when(mockInputStream.available()).thenThrow(new IOException("Simulated IOException"));

        ReflectionTestUtils.setField(servletInputStream, "cachedBodyInputStream", mockInputStream);

        boolean finished = servletInputStream.isFinished();

        assertFalse(finished);
    }

}