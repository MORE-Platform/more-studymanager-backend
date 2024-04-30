/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.generator.RandomTokenGenerator;
import io.redlink.more.studymanager.repository.ParticipantRepository;

import java.util.EnumSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.redlink.more.studymanager.model.Participant.Status.*;

@Service
public class ParticipantService {

    private final StudyStateService studyStateService;
    private final ParticipantRepository participantRepository;
    private final ElasticService elasticService;

    public ParticipantService(
            StudyStateService studyStateService, ParticipantRepository repository, ElasticService elasticService) {
        this.studyStateService = studyStateService;
        this.participantRepository = repository;
        this.elasticService = elasticService;
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

    public void deleteParticipant(Long studyId, Integer participantId, Boolean includeData) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        participantRepository.deleteParticipant(studyId, participantId);
        if(Boolean.TRUE.equals(includeData)) {
               elasticService.removeDataForParticipant(studyId, participantId);
        }
    }

    public Participant updateParticipant(Participant participant) {
        studyStateService.assertStudyNotInState(participant.getStudyId(), Study.Status.CLOSED);
        return participantRepository.update(participant);
    }

    @Transactional
    public void alignParticipantsWithStudyState(Study study) {
        if (EnumSet.of(Study.Status.CLOSED, Study.Status.DRAFT).contains(study.getStudyState())) {
            participantRepository.cleanupParticipants(study.getStudyId());
        }
        if (EnumSet.of(Study.Status.PREVIEW).contains(study.getStudyState())) {
            participantRepository.listParticipants(study.getStudyId())
                    .forEach(p -> participantRepository.updateRegistrationToken(
                            p.getStudyId(), p.getParticipantId(), RandomTokenGenerator.generate()
                    ));
        }
    }

    public void setStatus(Long studyId, Integer participantId, Participant.Status status) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        participantRepository.setStatusByIds(studyId, participantId, status);
        if (EnumSet.of(ABANDONED, KICKED_OUT, LOCKED).contains(status)) {
            participantRepository.cleanupParticipant(studyId, participantId);
        }
    }
}
