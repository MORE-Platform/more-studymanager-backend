/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.BuildInfoDTO;
import io.redlink.more.studymanager.api.v1.model.FrontendConfigurationDTO;
import io.redlink.more.studymanager.api.v1.model.KeycloakSettingsDTO;
import io.redlink.more.studymanager.api.v1.webservices.ConfigurationApi;
import io.redlink.more.studymanager.properties.FrontendConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableConfigurationProperties(FrontendConfigurationProperties.class)
public class ConfigurationApiV1Controller implements ConfigurationApi {

    private final FrontendConfigurationProperties uiConfig;

    public ConfigurationApiV1Controller(FrontendConfigurationProperties uiConfig) {
        this.uiConfig = uiConfig;
    }

    @Override
    public ResponseEntity<BuildInfoDTO> getBuildInfo() {
        return null;
    }

    @Override
    public ResponseEntity<FrontendConfigurationDTO> getFrontendConfig() {
        return ResponseEntity.ok(
                transform(uiConfig)
        );
    }

    private static FrontendConfigurationDTO transform(FrontendConfigurationProperties uiConfig) {
        return new FrontendConfigurationDTO()
                .title(uiConfig.title())
                .auth(new KeycloakSettingsDTO()
                        .server(uiConfig.keycloak().server())
                        .realm(uiConfig.keycloak().realm())
                        .clientId(uiConfig.keycloak().clientId())
                )
                ;
    }
}
