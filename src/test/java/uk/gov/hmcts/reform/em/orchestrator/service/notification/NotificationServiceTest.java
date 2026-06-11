package uk.gov.hmcts.reform.em.orchestrator.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<Map<String, String>> personalizationCaptor;

    private static final String JWT = "Bearer test-token";
    private static final String TEMPLATE_ID = "template-id-123";
    private static final String CASE_ID = "case-123";
    private static final String BUNDLE_TITLE = "My Bundle";
    private static final String FAILURE_MSG = "Something went wrong";
    private static final String EMAIL = "email@email.com";

    @Test
    void sendEmailNotificationSuccessful() throws Exception {
        UserInfo userInfo = mock(UserInfo.class);
        when(userInfo.getSub()).thenReturn(EMAIL);
        when(idamClient.getUserInfo(JWT)).thenReturn(userInfo);

        notificationService.sendEmailNotification(TEMPLATE_ID, JWT, CASE_ID, BUNDLE_TITLE, FAILURE_MSG);

        verify(notificationClient).sendEmail(
            eq(TEMPLATE_ID),
            eq(EMAIL),
            personalizationCaptor.capture(),
            eq("Email Notification: " + CASE_ID)
        );

        Map<String, String> p = personalizationCaptor.getValue();
        assertEquals(CASE_ID, p.get("case_reference"));
        assertEquals(BUNDLE_TITLE, p.get("bundle_name"));
        assertEquals(FAILURE_MSG, p.get("system_error_message"));
    }

    @Test
    void sendEmailNotificationNotifyClientException() throws Exception {
        UserInfo userInfo = mock(UserInfo.class);
        when(userInfo.getSub()).thenReturn(EMAIL);
        when(idamClient.getUserInfo(JWT)).thenReturn(userInfo);

        doThrow(new NotificationClientException("GovNotify Error"))
            .when(notificationClient)
            .sendEmail(anyString(), anyString(), any(), anyString());

        CallbackException ex = assertThrows(CallbackException.class, () ->
            notificationService.sendEmailNotification(TEMPLATE_ID, JWT, CASE_ID, BUNDLE_TITLE, FAILURE_MSG)
        );

        assertTrue(ex.getMessage().contains("NotificationClientException"));
    }
}