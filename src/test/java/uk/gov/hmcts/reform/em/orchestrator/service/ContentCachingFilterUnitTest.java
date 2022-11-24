package uk.gov.hmcts.reform.em.orchestrator.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
public class ContentCachingFilterUnitTest {

    @InjectMocks
    private ContentCachingFilter filterToTest;

    @Test
    public void testGivenHttpRequest_WhenDoFilter_thenCreatesRequestWrapperObject() throws IOException,
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
