package uk.gov.hmcts.reform.em.orchestrator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CachedBodyHttpServletRequestUnitTest {

    @Test
    void testGivenHttpServletRequestWithBody_whenCalledGetInputStream_ThenGetsServletInputStreamWithSameBody()
            throws IOException {
        // Given
        byte[] cachedBody = "{\"firstName\" :\"abc\",\"lastName\" : \"xyz\",\"age\" : 30\"}".getBytes();
        MockHttpServletRequest mockedHttpServletRequest = new MockHttpServletRequest();
        mockedHttpServletRequest.setContent(cachedBody);
        CachedBodyHttpServletRequest request = new CachedBodyHttpServletRequest(mockedHttpServletRequest);

        // when
        InputStream inputStream = request.getInputStream();

        // then
        assertEquals(new String(cachedBody), new String(StreamUtils.copyToByteArray(inputStream)));
    }

    @Test
    void testGivenHttpServletRequestWithBody_whenCalledGetReader_ThenGetBufferedReaderWithSameBody()
            throws IOException {
        // Given
        byte[] cachedBody = "{\"firstName\" :\"abc\",\"lastName\" : \"xyz\",\"age\" : 30\"}".getBytes();
        MockHttpServletRequest mockeddHttpServletRequest = new MockHttpServletRequest();
        mockeddHttpServletRequest.setContent(cachedBody);
        CachedBodyHttpServletRequest request = new CachedBodyHttpServletRequest(mockeddHttpServletRequest);

        // when
        BufferedReader bufferedReader = request.getReader();

        // then
        String line;
        StringBuilder builder = new StringBuilder();
        while (Objects.nonNull(line = bufferedReader.readLine())) {
            builder.append(line);
        }
        assertEquals(new String(cachedBody), builder.toString());
    }
}
