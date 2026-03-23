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
import io.redlink.more.studymanager.exception.BadRequestException;
import io.redlink.more.studymanager.exception.DataConstraintException;
import io.redlink.more.studymanager.exception.NotFoundException;
import io.redlink.more.studymanager.model.MoreUser;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.StudyGroup;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.StudyUserRoles;
import io.redlink.more.studymanager.model.User;
import io.redlink.more.studymanager.model.scheduler.Duration;
import io.redlink.more.studymanager.repository.StudyAclRepository;
import io.redlink.more.studymanager.repository.StudyGroupRepository;
import io.redlink.more.studymanager.repository.StudyRepository;
import io.redlink.more.studymanager.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class StudyService {

    private static final Logger log = LoggerFactory.getLogger(StudyService.class);

    static final Map<Study.Status, Set<Study.Status>> VALID_STUDY_TRANSITIONS = Map.of(
            Study.Status.DRAFT, EnumSet.of(Study.Status.PREVIEW, Study.Status.ACTIVE),
            Study.Status.PREVIEW, EnumSet.of(Study.Status.PAUSED_PREVIEW, Study.Status.DRAFT),
            Study.Status.PAUSED_PREVIEW, EnumSet.of(Study.Status.PREVIEW, Study.Status.DRAFT),
            Study.Status.ACTIVE, EnumSet.of(Study.Status.PAUSED, Study.Status.CLOSED),
            Study.Status.PAUSED, EnumSet.of(Study.Status.ACTIVE, Study.Status.CLOSED)
    );

    private final StudyRepository studyRepository;
    private final StudyAclRepository aclRepository;
    private final UserRepository userRepo;
    private final StudyStateService studyStateService;
    private final ElasticService elasticService;
    private final OccurredObservationService occurredObservationService;

    private final StudyGroupRepository studyGroupRepository;
    private final LoginTokenService loginTokenService;

    private ApplicationEventPublisher applicationEventPublisher;


    public StudyService(StudyRepository studyRepository, StudyAclRepository aclRepository, UserRepository userRepo,
                        StudyStateService studyStateService, OccurredObservationService occurredObservationService,
                        ElasticService elasticService, StudyGroupRepository studyGroupRepository,
                        LoginTokenService loginTokenService,
                        ApplicationEventPublisher applicationEventPublisher) {
        this.studyRepository = studyRepository;
        this.aclRepository = aclRepository;
        this.userRepo = userRepo;
        this.studyStateService = studyStateService;
        this.elasticService = elasticService;
        this.studyGroupRepository = studyGroupRepository;
        this.occurredObservationService = occurredObservationService;
        this.loginTokenService = loginTokenService;
        this.applicationEventPublisher = applicationEventPublisher;
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
        loginTokenService.deleteStudyTokens(studyId);
        elasticService.deleteIndex(studyId);
        occurredObservationService.deleteOccurredObservations(studyId);
    }

    @Transactional
    public Optional<Study> setStatus(Long studyId, Study.Status newState, User user) {
        final Study study = getStudy(studyId, user)
                .orElseThrow(() -> NotFoundException.Study(studyId));
        final Study.Status oldState = study.getStudyState();

        /* Validate the transition */
        if (!VALID_STUDY_TRANSITIONS.getOrDefault(oldState, EnumSet.noneOf(Study.Status.class)).contains(newState)) {
            throw BadRequestException.StateChange(oldState, newState);
        }

        return studyRepository.setStateById(studyId, newState)
                .map(s -> {
                    try {
                        publishStudyStateChangedEvent(s, oldState);
                        if (s.getStudyState() == Study.Status.DRAFT) {
                            log.info("Study {} transitioned back to {}, dropping collected observation and data health information", study.getStudyId(), s.getStudyState());
                            elasticService.deleteIndex(s.getStudyId());
                            occurredObservationService.deleteOccurredObservations(studyId);
                        }
                    } catch (Exception e) {
                        log.warn("Could not set new state for study id {}; old state: {}; new state: {}", studyId, oldState.getValue(), s.getStudyState().getValue());
                        //ROLLBACK
                        studyRepository.setStateById(studyId, oldState);
                        studyRepository.getById(studyId).ifPresent(rollbackStudy -> {
                            publishStudyStateChangedEvent(rollbackStudy, newState);
                        });
                        throw new BadRequestException("Study cannot be initialized", e);
                    }
                    return s;
                });
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

    public Set<StudyRole> getStudyRoles(Long studyId, String userId){
        return aclRepository.getRoles(studyId, userId);
    }

    public Optional<Duration> getStudyDuration(Long studyId) {
        return studyRepository.getById(studyId)
                .map(Study::getDuration);
    }

    public Optional<Duration> getStudyDuration(Long studyId, Integer studyGroupId) {
        final Optional<StudyGroup> group = Optional.ofNullable(studyGroupRepository.getByIds(studyId, studyGroupId));
        // If there's no such group, return empty() here...
        if (group.isEmpty()) return Optional.empty();

        return group
                // Get the groups duration...
                .map(StudyGroup::getDuration)
                // ... of fallback to the study-duration if not set.
                .or(() -> getStudyDuration(studyId));
    }

    /**
     * Provides a stream over all studies with the parsed states
     * @param states the states
     * @return the studies with the parsed states. An empty Stream is <code>null</code> was
     * parsed or no Studies with the requested states are present
     */
    public Stream<Study> getStudiesByStates(Iterable<Study.Status> states) {
        return states == null ? Stream.empty() : studyRepository.listStudiesByStates(states);
    }

    protected void publishStudyStateChangedEvent(final Study study, final Study.Status previousState) {
        applicationEventPublisher.publishEvent(new StudyStateChangedEvent(this, study, previousState));
    }
}
