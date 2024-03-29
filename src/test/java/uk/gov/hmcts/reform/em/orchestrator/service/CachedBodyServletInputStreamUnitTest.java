package uk.gov.hmcts.reform.em.orchestrator.service;

import jakarta.servlet.ReadListener;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@RunWith(SpringRunner.class)
public class CachedBodyServletInputStreamUnitTest {

    private CachedBodyServletInputStream servletInputStream;

    @After
    public void cleanUp() throws IOException {
        if (Objects.nonNull(servletInputStream)) {
            servletInputStream.close();
        }
    }

    @Test
    public void testGivenServletInputStreamCreated_whenCalledisFinished_Thenfalse() {
        // Given
        byte[] cachedBody = "{\"firstName\" :\"abc\",\"lastName\" : \"xyz\",\"age\" : 30\"}".getBytes();
        servletInputStream = new CachedBodyServletInputStream(cachedBody);

        // when
        boolean finished = servletInputStream.isFinished();

        // then
        assertFalse(finished);
    }

    @Test
    public void testGivenServletInputStreamCreatedAndBodyRead_whenCalledisFinished_ThenTrue() throws IOException {
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
    public void testGivenServletInputStreamCreatedAndBodyRead_whenCalledIsReady_ThenTrue() throws IOException {
        // Given
        byte[] cachedBody = "{\"firstName\" :\"abc\",\"lastName\" : \"xyz\",\"age\" : 30\"}".getBytes();
        servletInputStream = new CachedBodyServletInputStream(cachedBody);

        // when
        boolean ready = servletInputStream.isReady();

        // then
        assertTrue(ready);
    }

    @Test
    public void testGivenServletInputStreamCreated_whenCalledIsRead_ThenReturnsBody() throws IOException {
        // Given
        byte[] cachedBody = "{\"firstName\" :\"abc\",\"lastName\" : \"xyz\",\"age\" : 30\"}".getBytes();
        servletInputStream = new CachedBodyServletInputStream(cachedBody);

        // when
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int len = 0;
        byte[] buffer = new byte[1024];
        while ((len = servletInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }

        // then
        assertEquals(new String(cachedBody), new String(byteArrayOutputStream.toByteArray()));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGivenServletInputStreamCreated_whenCalledIsRead_ThenThrowsException() throws IOException {
        // Given
        byte[] cachedBody = "{\"firstName\" :\"abc\",\"lastName\" : \"xyz\",\"age\" : 30\"}".getBytes();
        servletInputStream = new CachedBodyServletInputStream(cachedBody);

        // when
        servletInputStream.setReadListener(Mockito.mock(ReadListener.class));

    }

}
