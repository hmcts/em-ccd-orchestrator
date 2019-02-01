package uk.gov.hmcts.reform.em.stitching.config.security;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtilsTest {

    @Test
    public void getCurrentUserLoginNoAuthentication() {
        Assert.assertFalse(SecurityUtils.getCurrentUserLogin().isPresent());
    }

    @Test
    public void getCurrentUserLogin() {
        Authentication authentication = Mockito.mock(Authentication.class);
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
        Mockito.when(userDetails.getUsername()).thenReturn("aabb");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Assert.assertEquals("aabb",  SecurityUtils.getCurrentUserLogin().get());
    }
}