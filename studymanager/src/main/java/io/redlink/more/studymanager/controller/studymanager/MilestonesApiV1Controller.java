/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.CreateMilestoneRequestDTO;
import io.redlink.more.studymanager.api.v1.model.CreateParticipantMilestoneRequestDTO;
import io.redlink.more.studymanager.api.v1.model.MilestoneDTO;
import io.redlink.more.studymanager.api.v1.model.ParticipantMilestoneDTO;
import io.redlink.more.studymanager.api.v1.webservices.MilestonesApi;
import io.redlink.more.studymanager.audit.Audited;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.MilestoneTransformer;
import io.redlink.more.studymanager.model.transformer.ParticipantMilestoneTransformer;
import io.redlink.more.studymanager.service.MilestoneService;
import io.redlink.more.studymanager.service.ParticipantMilestoneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class MilestonesApiV1Controller implements MilestonesApi {

    private final MilestoneService milestoneService;
    private final ParticipantMilestoneService participantMilestoneService;

    public MilestonesApiV1Controller(MilestoneService milestoneService, ParticipantMilestoneService participantMilestoneService) {
        this.milestoneService = milestoneService;
        this.participantMilestoneService = participantMilestoneService;
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<List<MilestoneDTO>> listMilestones(Long studyId) {
        return ResponseEntity.ok(
                milestoneService.listMilestones(studyId).stream()
                        .map(MilestoneTransformer::toMilestoneDTO_V1)
                        .toList()
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<MilestoneDTO> createMilestone(Long studyId, CreateMilestoneRequestDTO createMilestoneRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                MilestoneTransformer.toMilestoneDTO_V1(
                        milestoneService.addMilestone(studyId, createMilestoneRequestDTO.getName())
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<MilestoneDTO> getMilestone(Long studyId, Integer milestoneId) {
        return ResponseEntity.ok(
                MilestoneTransformer.toMilestoneDTO_V1(
                        milestoneService.getMilestone(studyId, milestoneId)
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<MilestoneDTO> updateMilestone(Long studyId, Integer milestoneId, MilestoneDTO milestoneDTO) {
        return ResponseEntity.ok(
                MilestoneTransformer.toMilestoneDTO_V1(
                        milestoneService.updateMilestone(studyId, milestoneId, milestoneDTO.getName(), milestoneDTO.getOrderIndex())
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<Void> deleteMilestone(Long studyId, Integer milestoneId) {
        milestoneService.deleteMilestone(studyId, milestoneId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<List<ParticipantMilestoneDTO>> listParticipantMilestones(Long studyId, Integer participantId) {
        return ResponseEntity.ok(
                participantMilestoneService.listParticipantMilestones(studyId, participantId).stream()
                        .map(ParticipantMilestoneTransformer::toParticipantMilestoneDTO_V1)
                        .toList()
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<ParticipantMilestoneDTO> createParticipantMilestone(Long studyId, Integer participantId, CreateParticipantMilestoneRequestDTO createParticipantMilestoneRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ParticipantMilestoneTransformer.toParticipantMilestoneDTO_V1(
                        participantMilestoneService.createParticipantMilestone(
                                studyId,
                                participantId,
                                createParticipantMilestoneRequestDTO.getMilestoneId(),
                                createParticipantMilestoneRequestDTO.getDateTime()
                        )
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<ParticipantMilestoneDTO> getParticipantMilestone(Long studyId, Integer participantId, Integer milestoneId) {
        return ResponseEntity.ok(
                ParticipantMilestoneTransformer.toParticipantMilestoneDTO_V1(
                        participantMilestoneService.getParticipantMilestone(studyId, participantId, milestoneId)
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<ParticipantMilestoneDTO> updateParticipantMilestone(Long studyId, Integer participantId, Integer milestoneId, ParticipantMilestoneDTO participantMilestoneDTO) {
        return ResponseEntity.ok(
                ParticipantMilestoneTransformer.toParticipantMilestoneDTO_V1(
                        participantMilestoneService.updateParticipantMilestone(
                                studyId, participantId, milestoneId, participantMilestoneDTO.getDateTime()
                        )
                )
        );
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    @Audited
    public ResponseEntity<Void> deleteParticipantMilestone(Long studyId, Integer participantId, Integer milestoneId) {
        participantMilestoneService.deleteParticipantMilestone(studyId, participantId, milestoneId);
        return ResponseEntity.noContent().build();
    }
}
