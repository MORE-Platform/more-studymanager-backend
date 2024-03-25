/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.DataConstraintException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.repository.StudyAclRepository;
import io.redlink.more.studymanager.repository.StudyRepository;
import io.redlink.more.studymanager.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StudyService {

    private static final Logger log = LoggerFactory.getLogger(StudyService.class);
    private final StudyRepository studyRepository;
    private final StudyAclRepository aclRepository;
    private final UserRepository userRepo;
    private final InterventionService interventionService;
    private final ObservationService observationService;
    private final ParticipantService participantService;
    private final StudyStateService studyStateService;
    private final IntegrationService integrationService;
    private final ElasticService elasticService;

    private final PushNotificationService pushNotificationService;


    public StudyService(StudyRepository studyRepository, StudyAclRepository aclRepository, UserRepository userRepo,
                        StudyStateService studyStateService, InterventionService interventionService, ObservationService observationService,
                        ParticipantService participantService, IntegrationService integrationService, ElasticService elasticService, PushNotificationService pushNotificationService) {
        this.studyRepository = studyRepository;
        this.aclRepository = aclRepository;
        this.userRepo = userRepo;
        this.studyStateService = studyStateService;
        this.interventionService = interventionService;
        this.observationService = observationService;
        this.participantService = participantService;
        this.integrationService = integrationService;
        this.elasticService = elasticService;
        this.pushNotificationService = pushNotificationService;
    }

    public Study createStudy(Study study, User currentUser) {
        // TODO: Workaround until proper auth is available
        var user = userRepo.save(currentUser);
        var savedStudy = studyRepository.insert(study);
        aclRepository.setRoles(savedStudy.getStudyId(), user.id(), EnumSet.allOf(StudyRole.class), null);
        return getStudy(savedStudy.getStudyId(), user)
                .orElse(savedStudy);
    }

    public List<Study> listStudies(User user) {
        return listStudies(user, EnumSet.allOf(StudyRole.class));
    }

    public List<Study> listStudies(User user, Set<StudyRole> allowedRoles) {
        return studyRepository.listStudiesByAclOrderByModifiedDesc(user, allowedRoles);
    }

    public Optional<Study> getStudy(Long studyId, User user) {
        return studyRepository.getById(studyId, user);
    }

    public Optional<Boolean> existsStudy(Long studyId) {
        return studyRepository.exists(studyId);
    }

    public Optional<Study> updateStudy(Study study, User user) {
        studyStateService.assertStudyNotInState(study, Study.Status.CLOSED);
        return studyRepository.update(study, user);
    }

    public void deleteStudy(Long studyId) {
        studyStateService.assertStudyState(studyId, Study.Status.DRAFT, Study.Status.CLOSED);
        studyRepository.deleteById(studyId);
        elasticService.deleteIndex(studyId);
    }

    public void setStatus(Long studyId, Study.Status status, User user) {
        Study study = getStudy(studyId, user)
                .orElseThrow(() -> NotFoundException.Study(studyId));
        if (status.equals(Study.Status.DRAFT)) {
            throw BadRequestException.StateChange(study.getStudyState(), Study.Status.DRAFT);
        }
        if (study.getStudyState().equals(Study.Status.CLOSED)) {
            throw BadRequestException.StateChange(Study.Status.CLOSED, status);
        }
        if (study.getStudyState().equals(status)) {
            throw BadRequestException.StateChange(study.getStudyState(), status);
        }

        Study.Status oldState = study.getStudyState();

        studyRepository.setStateById(studyId, status);
        studyRepository.getById(studyId).ifPresent(s -> {
            try {
                alignWithStudyState(s);
                participantService.listParticipants(studyId).forEach(participant -> {
                    pushNotificationService.sendPushNotification(
                            studyId,
                            participant.getParticipantId(),
                            "Your Study has a new update",
                            "Your study was updated. For more information, please launch the app!",
                            Map.of("key", "STUDY_STATE_CHANGED",
                                    "oldState", oldState.getValue(),
                                    "newState", s.getStudyState().getValue())
                    );
                });
                participantService.alignParticipantsWithStudyState(s);
            } catch (Exception e) {
                log.warn("Could not set new state for study id {}; old state: {}; new state: {}", studyId, oldState.getValue(), s.getStudyState().getValue());
                //ROLLBACK
                studyRepository.setStateById(studyId, oldState);
                studyRepository.getById(studyId).ifPresent(this::alignWithStudyState);
                throw new BadRequestException("Study cannot be initialized",e);
            }
        });
    }

    // every minute
    @Scheduled(cron = "0 * * * * ?")
    public void closeParticipationsForStudiesWithDurations() {
        List<Participant> participantsToClose = participantService.listParticipantsForClosing();
        log.debug("Selected {} paticipants to close", participantsToClose.size());
        participantsToClose.forEach(participant -> {
            pushNotificationService.sendPushNotification(
                    participant.getStudyId(),
                    participant.getParticipantId(),
                    "Your Study has been closed",
                    "Your study was updated. For more information, please launch the app!",
                    Map.of("key", "STUDY_STATE_CHANGED",
                            "oldState", Study.Status.ACTIVE.getValue(),
                            "newState", Study.Status.CLOSED.getValue())
            );
            participantService.setStatus(
                    participant.getStudyId(), participant.getParticipantId(), Participant.Status.LOCKED
            );
        });
    }

    private void alignWithStudyState(Study s) {
        interventionService.alignInterventionsWithStudyState(s);
        observationService.alignObservationsWithStudyState(s);
        integrationService.alignIntegrationsWithStudyState(s);
    }

    public Map<MoreUser, Set<StudyRole>> getACL(Long studyId) {
        return aclRepository.getACL(studyId);
    }

    public Optional<StudyUserRoles> setRolesForStudy(Long studyId, String userId, Set<StudyRole> roles,
                                                     User currentUser) {
        studyStateService.assertStudyNotInState(studyId, Study.Status.CLOSED);
        //MORE-218: One must not remove oneself as ADMIN
        if (StringUtils.equals(currentUser.id(), userId)
                && !roles.contains(StudyRole.STUDY_ADMIN)) {
            throw DataConstraintException.createNoSelfAdminRemoval(studyId, userId);
        }

        if (roles.isEmpty()) {
            aclRepository.clearRoles(studyId, userId);
            return Optional.empty();
        }

        aclRepository.setRoles(studyId, userId, roles, currentUser.id());
        return getRolesForStudy(studyId, userId);
    }

    public Optional<StudyUserRoles> getRolesForStudy(Long studyId, String userId) {
        return userRepo.getById(userId).map(user ->
                new StudyUserRoles(
                        user,
                        aclRepository.getRoleDetails(studyId, userId)
                )
        );
    }
}
