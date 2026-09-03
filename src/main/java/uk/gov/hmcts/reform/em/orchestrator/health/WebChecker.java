package uk.gov.hmcts.reform.em.orchestrator.health;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.em.orchestrator.health.model.HealthCheckResponse;

import java.util.Objects;


public class WebChecker {

    private final String name;
    private final String url;
    private final RestTemplate restTemplate;
    private static final Logger log = LoggerFactory.getLogger(WebChecker.class);

    public WebChecker(String name, String url, RestTemplate restTemplate) {
        this.name = name;
        this.restTemplate = restTemplate;
        this.url = url;
    }

    public Health health() {
        return Health.up()
            .withDetail(name + " status", getStatus() ? "UP" : "DOWN")
            .build();
    }

    private boolean getStatus() {
        try {
            final HealthCheckResponse healthCheckResponse =
                restTemplate.getForObject(url + "/health", HealthCheckResponse.class);
            if (Objects.nonNull(healthCheckResponse) && StringUtils.isNotBlank(healthCheckResponse.status())) {
                return "UP".equalsIgnoreCase(healthCheckResponse.status());
            }
            return false;
        } catch (Exception ex) {
            log.error(name + " " + url + "/health Failed", ex);
            return false;
        }
    }
}
