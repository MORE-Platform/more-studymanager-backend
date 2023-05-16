package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.EndpointTokenDTO;
import io.redlink.more.studymanager.api.v1.webservices.IntegrationsApi;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.EndpointTokenTransformer;
import io.redlink.more.studymanager.service.IntegrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class IntegrationsApiV1Controller implements IntegrationsApi {

    private final IntegrationService service;

    public IntegrationsApiV1Controller(IntegrationService service) { this.service = service; }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<EndpointTokenDTO> addToken(Long studyId, Integer observationId, String tokenLabel) {
        if(tokenLabel.isBlank()) { return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                EndpointTokenTransformer.toEndpointTokenDTO(
                        service.addToken(studyId, observationId, tokenLabel)
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<EndpointTokenDTO> getToken(Long studyId, Integer observationId, Integer tokenId) {
        return ResponseEntity.ok().body(
                EndpointTokenTransformer.toEndpointTokenDTO(
                        service.getToken(studyId, observationId, tokenId)
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<List<EndpointTokenDTO>> getTokens(Long studyId, Integer observationId) {
        return ResponseEntity.ok().body(
                EndpointTokenTransformer.toEndpointTokensDTO(
                        service.getTokens(studyId, observationId)
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<Void> deleteToken(Long studyId, Integer observationId, Integer tokenId) {
        service.deleteToken(studyId, observationId, tokenId);
        return ResponseEntity.noContent().build();
    }
}
