package io.redlink.more.studymanager.configuration;

import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Basics
        http
                .csrf().and()
                .cors().disable();
        // Restricted Paths
        http
                .authorizeRequests()
                .antMatchers("/api", "/api/v1/me").permitAll()
                .antMatchers("/api/v1/**").authenticated()
                .anyRequest().permitAll();

        // Logout Config
        http.logout()
                .logoutSuccessUrl("/");

        // Enable OAuth2
        http.oauth2Login();
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
