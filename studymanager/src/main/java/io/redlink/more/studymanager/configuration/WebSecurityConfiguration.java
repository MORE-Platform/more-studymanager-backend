package io.redlink.more.studymanager.configuration;

import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Basics
        http.csrf();
        http.cors().disable();

        // Restricted Paths
        http.authorizeRequests()
                .antMatchers("/api", "/api/v1/me").permitAll()
                .antMatchers("/api/v1/**").authenticated()
                .anyRequest().permitAll();

        // API-Calls should not be redirected to the login page, but answered with a 401
        http.exceptionHandling()
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), new AntPathRequestMatcher("/api/**"));

        // Logout Config
        http.logout()
                .logoutSuccessUrl("/");

        // Enable OAuth2
        http.oauth2Login()
                // register oauth2-provider under this baseurl to simplify routing
                .authorizationEndpoint().baseUri("/login");
        // Enable OAuth2 client_credentials flow (insomnia)
        http.oauth2ResourceServer().jwt();

        return http.build();
    }

    @Bean
    @RequestScope
    protected OAuth2AuthenticationService oAuth2AuthenticationService() {
        return new OAuth2AuthenticationService();
    }

}
