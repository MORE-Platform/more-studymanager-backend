/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.event.StudyStateChangedEvent;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.generator.RandomTokenGenerator;
import io.redlink.more.studymanager.repository.ParticipantRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

@Service
public class ParticipantService {

    private final StudyStateService studyStateService;
    private final ParticipantRepository participantRepository;
    private final ElasticService elasticService;
    private final LoginTokenService loginTokenService;

    public ParticipantService(
            StudyStateService studyStateService, ParticipantRepository repository, ElasticService elasticService, LoginTokenService loginTokenService) {
        this.studyStateService = studyStateService;
        this.participantRepository = repository;
        this.elasticService = elasticService;
        this.loginTokenService = loginTokenService;
    }

    public Participant createParticipant(Participant participant) {
        studyStateService.assertStudyNotInState(participant.getStudyId(), Study.Status.CLOSED);
        participant.setRegistrationToken(RandomTokenGenerator.generate());
        return participantRepository.insert(participant);
    }

    public List<Participant> listParticipants(Long studyId) {
        return participantRepository.listParticipants(studyId);
    }

    public List<Participant> listParticipantsForClosing() {
        return participantRepository.listParticipantsForClosing();
    }

    public Participant getParticipant(Long studyId, Integer participantId) {
        return participantRepository.getByIds(studyId, participantId);
    }

    public void generateLoginToken(Long studyId, Integer participantId, String application) {
        loginTokenService.createMissingToken(studyId, participantId, application);
        participantRepository.setStatusIfCurrentStatusIs(studyId, participantId, Participant.Status.INVITED, Participant.Status.NEW);
    }

    public void deleteParticipant(Long studyId, Integer participantId, Boolean includeData) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        participantRepository.deleteParticipant(studyId, participantId);
        loginTokenService.deleteParticipantTokens(studyId, participantId);
        if (Boolean.TRUE.equals(includeData)) {
            elasticService.removeDataForParticipant(studyId, participantId);
        }
    }

    public Participant updateParticipant(Participant participant) {
        studyStateService.assertStudyNotInState(participant.getStudyId(), Study.Status.CLOSED);
        return participantRepository.update(participant);
    }

    @EventListener
    @Transactional
    public void handleStudyStateChange(StudyStateChangedEvent event) {
        alignParticipantsWithStudyState(event.getStudy());
    }


    private void alignParticipantsWithStudyState(Study study) {
        switch (study.getStudyState()) {
            case CLOSED:
                participantRepository.cleanupParticipants(study.getStudyId());
                loginTokenService.deleteStudyTokens(study.getStudyId());
                break;
            case DRAFT:
                participantRepository.resetParticipants(study.getStudyId(), RandomTokenGenerator::generate);
                loginTokenService.deleteStudyTokens(study.getStudyId());
                break;
            case ACTIVE:
            case PREVIEW:
                alignParticipantsInActiveState(study);
                break;
        }
    }

    private void alignParticipantsInActiveState(Study study) {
        loginTokenService.deleteTokensExcept(study.getStudyId(), study.getApplicationAccess());
    }

    public void setStatus(Long studyId, Integer participantId, Participant.Status status) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        participantRepository.setStatusByIds(studyId, participantId, status);
        if (EnumSet.of(Participant.Status.ABANDONED, Participant.Status.KICKED_OUT, Participant.Status.LOCKED)
                .contains(status)) {
            participantRepository.cleanupParticipant(studyId, participantId);
            loginTokenService.deleteParticipantTokens(studyId, participantId);
        }
    }
}
