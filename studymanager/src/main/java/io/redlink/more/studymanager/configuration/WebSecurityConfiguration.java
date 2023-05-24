package io.redlink.more.studymanager.configuration;

import io.redlink.more.studymanager.properties.MoreAuthProperties;
import io.redlink.more.studymanager.repository.UserRepository;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(MoreAuthProperties.class)
public class WebSecurityConfiguration {

    final ClientRegistrationRepository clientRegistrationRepository;

    final MoreAuthProperties moreAuthProperties;

    public WebSecurityConfiguration(ClientRegistrationRepository clientRegistrationRepository, MoreAuthProperties moreAuthProperties) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.moreAuthProperties = moreAuthProperties;
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http,
                                              OAuth2AuthenticationService oAuth2AuthenticationService,
                                              OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
                                              UserRepository userRepository) throws Exception {
        // Basics
        http.csrf()
                .ignoringRequestMatchers("/kibana/**")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        http.cors().disable();

        // Restricted Paths
        http.authorizeHttpRequests()
                .requestMatchers("/api", "/api/v1/me").permitAll()
                // Allow unauthenticated access to the ui-/auth-settings
                .requestMatchers(HttpMethod.GET, "/api/v1/config/ui").permitAll()
                //TODO specific handling of temporary sidecar
                .requestMatchers("/api/v1/components/observation/lime-survey-observation/end.html").permitAll()
                .requestMatchers("/api/v1/**").authenticated()
                .requestMatchers("/kibana/**").authenticated()
                .requestMatchers("/login/init").authenticated()
                // actuator only from localhost
                .requestMatchers(
                        new AndRequestMatcher(
                                new AntPathRequestMatcher("/actuator/**"),
                                new IpAddressMatcher("127.0.0.1/8")
                        )
                ).permitAll()
                .requestMatchers("/error").authenticated()
                .anyRequest().denyAll();

        // API-Calls should not be redirected to the login page, but answered with a 401
        http.exceptionHandling()
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), new AntPathRequestMatcher("/api/**"));

        // Logout Config
        http.logout()
                .logoutSuccessHandler(oidcLogoutSuccessHandler())
                .logoutSuccessUrl("/");

        // Enable OAuth2
        http.oauth2Login()
                // register oauth2-provider under this baseurl to simplify routing
                .authorizationEndpoint().baseUri("/login/oauth").and()
                .authorizedClientService(
                        new UserSyncingOAuth2AuthorizedClientService(oAuth2AuthorizedClientService, oAuth2AuthenticationService, userRepository)
                );

        // Enable OAuth2 client_credentials flow (insomnia)
        http.oauth2ResourceServer().jwt();

        //TODO maybe disable in production
        http.headers().frameOptions().disable();

        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/");
        return oidcLogoutSuccessHandler;
    }

    @Bean
    protected GrantedAuthoritiesMapper userAuthoritiesMapper(OAuth2AuthenticationService oAuth2AuthenticationService) {
        return authorities -> {
            final Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    final OidcIdToken idToken = oidcUserAuthority.getIdToken();
                    final OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

                    // Is the profile complete?
                    if (oAuth2AuthenticationService.validateProfile(userInfo)) {
                        mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_FULL_PROFILE"));
                    }

                    // Has the mail been validated?
                    if (Boolean.TRUE.equals(idToken.getEmailVerified())) {
                        mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_EMAIL"));
                    }

                    oAuth2AuthenticationService.extractRoles(idToken)
                            .forEach(role -> mappedAuthorities.add(
                                    role.authority()
                            ));

                    // Keep the original Granted Authority
                    mappedAuthorities.add(oidcUserAuthority);
                } else if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {
                    final Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();

                    // Map the attributes found in userAttributes
                    oAuth2AuthenticationService.extractRoles(userAttributes)
                            .forEach(role -> mappedAuthorities.add(
                                    role.authority()
                            ));

                    // to one or more GrantedAuthority's and add it to mappedAuthorities
                    mappedAuthorities.add(oauth2UserAuthority);
                }
            });

            return Set.copyOf(mappedAuthorities);
        };
    }

    @Bean
    protected OAuth2AuthenticationService oAuth2AuthenticationService() {
        return new OAuth2AuthenticationService(moreAuthProperties);
    }

    @Bean
    protected RequestRejectedHandler requestRejectedHandler() {
        // Use a specific status-code for the Firewall to identify denied requests
        return new HttpStatusRequestRejectedHandler(HttpStatus.I_AM_A_TEAPOT.value());
    }

    static class UserSyncingOAuth2AuthorizedClientService implements OAuth2AuthorizedClientService {

        private final OAuth2AuthorizedClientService delegate;
        private final OAuth2AuthenticationService oAuth2AuthenticationService;
        private final UserRepository userRepository;

        UserSyncingOAuth2AuthorizedClientService(OAuth2AuthorizedClientService delegate,
                                                 OAuth2AuthenticationService oAuth2AuthenticationService,
                                                 UserRepository userRepository) {
            this.delegate = delegate;
            this.oAuth2AuthenticationService = oAuth2AuthenticationService;
            this.userRepository = userRepository;
        }

        @Override
        public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
            return delegate.loadAuthorizedClient(clientRegistrationId, principalName);
        }

        @Override
        public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication authentication) {
            var user = oAuth2AuthenticationService.getAuthenticatedUser(authentication);
            if (user.id() != null) {
                userRepository.save(user);
            }
            delegate.saveAuthorizedClient(authorizedClient, authentication);
        }

        @Override
        public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
            delegate.removeAuthorizedClient(clientRegistrationId, principalName);
        }
    }

}
