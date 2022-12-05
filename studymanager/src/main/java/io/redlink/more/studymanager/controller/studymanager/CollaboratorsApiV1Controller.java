/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.CollaboratorDTO;
import io.redlink.more.studymanager.api.v1.model.CollaboratorDetailsDTO;
import io.redlink.more.studymanager.api.v1.model.StudyRoleDTO;
import io.redlink.more.studymanager.api.v1.webservices.CollaboratorsApi;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.RoleTransformer;
import io.redlink.more.studymanager.model.transformer.UserInfoTransformer;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import io.redlink.more.studymanager.service.StudyService;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class CollaboratorsApiV1Controller implements CollaboratorsApi {

    private final OAuth2AuthenticationService authService;

    private final StudyService studyService;

    public CollaboratorsApiV1Controller(OAuth2AuthenticationService authService, StudyService studyService) {
        this.authService = authService;
        this.studyService = studyService;
    }

    @Override
    public ResponseEntity<List<CollaboratorDTO>> listStudyCollaborators(Long studyId) {
        return ResponseEntity.ok(
                studyService.getACL(studyId).entrySet().stream()
                        .map(e -> UserInfoTransformer.toCollaboratorDTO(e.getKey(), e.getValue()))
                        .toList()
        );
    }

    @Override
    public ResponseEntity<Void> clearStudyCollaboratorRoles(Long studyId, String uid) {
        var currentUser = authService.getCurrentUser();
        if (StringUtils.equals(currentUser.id(), uid)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        studyService.setRolesForStudy(studyId, uid, EnumSet.noneOf(StudyRole.class), currentUser);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CollaboratorDetailsDTO> getStudyCollaboratorRoles(Long studyId, String uid) {
        return ResponseEntity.of(
                studyService.getRolesForStudy(studyId, uid)
                        .map(UserInfoTransformer::toCollaboratorDetailsDTO)
        );
    }

    @Override
    public ResponseEntity<CollaboratorDetailsDTO> setStudyCollaboratorRoles(Long studyId, String uid, Set<StudyRoleDTO> studyRoleDTO) {
        var currentUser = authService.getCurrentUser();
        if (studyRoleDTO.isEmpty() && StringUtils.equals(currentUser.id(), uid)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.of(
                studyService.setRolesForStudy(studyId, uid, RoleTransformer.toStudyRoles(studyRoleDTO), currentUser)
                        .map(UserInfoTransformer::toCollaboratorDetailsDTO)
        );
    }

}
