package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class TestSecurityConfiguration {

    private final ClientRegistration clientRegistration;

    public TestSecurityConfiguration() {
        this.clientRegistration = clientRegistration().build();
    }

    @MockitoBean
    ServiceAuthFilter serviceAuthFilter;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(clientRegistration);
    }

    private ClientRegistration.Builder clientRegistration() {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("end_session_endpoint", "https://jhipster.org/logout");

        return ClientRegistration.withRegistrationId("oidc")
                .redirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("read:user")
                .authorizationUri("https://jhipster.org/login/oauth/authorize")
                .tokenUri("https://jhipster.org/login/oauth/access_token")
                .jwkSetUri("https://jhipster.org/oauth/jwk")
                .userInfoUri("https://api.jhipster.org/user")
                .providerConfigurationMetadata(metadata)
                .userNameAttributeName("id")
                .clientName("Client Name")
                .clientId("client-id")
                .clientSecret("client-secret");
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
                ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(
                    OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }

}
