package uk.gov.hmcts.reform.em.orchestrator.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class ContentCachingFilterUnitTest {

    @InjectMocks
    private ContentCachingFilter filterToTest;

    @Test
    void testGivenHttpRequest_WhenDoFilter_thenCreatesRequestWrapperObject() throws IOException,
            ServletException {
        // Given
        MockHttpServletRequest mockedRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockedResponse = new MockHttpServletResponse();
        FilterChain mockedFilterChain = Mockito.mock(FilterChain.class);

        // when
        filterToTest.doFilter(mockedRequest, mockedResponse, mockedFilterChain);

        // then
        Mockito.verify(mockedFilterChain, Mockito.times(1))
                .doFilter(Mockito.any(CachedBodyHttpServletRequest.class), Mockito.any(MockHttpServletResponse.class));
    }

}
