package uk.gov.hmcts.reform.em.orchestrator.config.security;

import feign.Client;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JwtAuthorityExtractor extends JwtAuthenticationConverter {

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final Client httpClient;

    public JwtAuthorityExtractor(ClientRegistrationRepository clientRegistrationRepository, Client httpClient) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.httpClient = httpClient;
    }

    public Map<String, Object> getUserInfo(String authorization) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("oidc");
        String userInfoEndpointUri = registration.getProviderDetails()
                .getUserInfoEndpoint().getUri();
        return buildFeignClient(userInfoEndpointUri.replace("/userinfo", ""))
                .userInfo("Bearer " + authorization, null);
    }

    private UserInfoClient buildFeignClient(String target) {
        return Feign.builder()
                .client(httpClient)
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logger(new Slf4jLogger(UserInfoClient.class))
                .target(UserInfoClient.class, target);
    }
}
