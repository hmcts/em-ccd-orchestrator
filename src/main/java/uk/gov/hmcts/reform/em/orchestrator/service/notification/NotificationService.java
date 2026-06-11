package uk.gov.hmcts.reform.em.orchestrator.service.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;
import uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    @Value("${auth.idam.client.baseUrl}")
    private String idamBaseUrl;

    private NotificationClient notificationClient;
    private IdamClient idamClient;

    private final ObjectMapper jsonMapper = new ObjectMapper();

    private static final String IDAM_USER_DETAILS_ENDPOINT = "/details";

    private final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(NotificationClient notificationClient, IdamClient idamClient) {
        this.notificationClient = notificationClient;
        this.idamClient = idamClient;
    }

    public void sendEmailNotification(String templateId, String jwt,
                                      String caseId, String bundleTitle, String failureMessage) {
        try {
            notificationClient.sendEmail(
                    templateId,
                    getUserEmail(jwt),
                    createPersonalisation(caseId, bundleTitle, failureMessage),
                    "Email Notification: " + caseId);

            if (log.isInfoEnabled()) {
                log.info("Notification email sent for caseId: {}", StringUtilities.convertValidLog(caseId));
            }

        } catch (NotificationClientException e) {
            throw new CallbackException(500, null, String.format("NotificationClientException: %s", e.getMessage()));
        }
    }

    private Map<String, String> createPersonalisation(String caseId, String bundleTitle, String failureMessage) {
        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put("case_reference", caseId);
        personalisation.put("bundle_name", bundleTitle);
        personalisation.put("system_error_message", failureMessage);
        return personalisation;
    }

    private String getUserEmail(String jwt) {
        return idamClient.getUserInfo(jwt).getSub();
    }
}
