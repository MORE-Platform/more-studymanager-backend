/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.sdk;

import io.redlink.more.studymanager.core.io.SimpleParticipant;
import io.redlink.more.studymanager.core.io.TimeRange;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.data.ElasticActionDataPoint;
import io.redlink.more.studymanager.model.data.ElasticDataPoint;
import io.redlink.more.studymanager.model.data.ElasticObservationDataPoint;
import io.redlink.more.studymanager.repository.NameValuePairRepository;
import io.redlink.more.studymanager.repository.ObservationRepository;
import io.redlink.more.studymanager.scheduling.SchedulingService;
import io.redlink.more.studymanager.scheduling.TriggerJob;
import io.redlink.more.studymanager.sdk.scoped.MoreActionSDKImpl;
import io.redlink.more.studymanager.sdk.scoped.MoreObservationSDKImpl;
import io.redlink.more.studymanager.sdk.scoped.MoreTriggerSDKImpl;
import io.redlink.more.studymanager.service.ElasticService;
import io.redlink.more.studymanager.service.ParticipantService;
import io.redlink.more.studymanager.service.PushNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MoreSDK {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoreSDK.class);

    public final NameValuePairRepository nvpairs;

    private final SchedulingService schedulingService;

    private final ParticipantService participantService;

    private final ElasticService elasticService;

    private final PushNotificationService pushNotificationService;

    private final ObservationRepository observationRepository;

    public MoreSDK(
            NameValuePairRepository nvpairs,
            SchedulingService schedulingService,
            ParticipantService participantService,
            ElasticService elasticService,
            PushNotificationService pushNotificationService, ObservationRepository observationRepository) {
        this.nvpairs = nvpairs;
        this.schedulingService = schedulingService;
        this.participantService = participantService;
        this.elasticService = elasticService;
        this.pushNotificationService = pushNotificationService;
        this.observationRepository = observationRepository;
    }

    public MoreActionSDK scopedActionSDK(Long studyId, Integer studyGroupId, int interventionId, int actionId, String actionType, int participantId) {
        return new MoreActionSDKImpl(this, studyId, studyGroupId, interventionId, actionId, actionType, participantId);
    }

    public MoreObservationSDK scopedObservationSDK(Long studyId, Integer studyGroupId, int observationId) {
        return new MoreObservationSDKImpl(this, studyId, studyGroupId, observationId);
    }

    public MoreTriggerSDK scopedTriggerSDK(Long studyId, Integer studyGroupId, int interventionId) {
        return new MoreTriggerSDKImpl(this, studyId, studyGroupId, interventionId);
    }

    public String addSchedule(String issuer, long studyId, Integer studyGroupId, int interventionId, Schedule schedule) {

        //build data map
        Map<String, Object> data = new HashMap<>(Map.of("studyId", studyId, "interventionId", interventionId));
        Optional.ofNullable(studyGroupId).ifPresent(v -> data.put("studyGroupId", studyGroupId));

        return schedulingService.scheduleJob(
                issuer,
                data,
                schedule,
                TriggerJob.class
        );
    }

    public void removeSchedule(String issuer, String id) {
        schedulingService.unscheduleJob(issuer, id, TriggerJob.class);
    }

    public Set<SimpleParticipant> listParticipants(long studyId, Integer studyGroupId, Set<Participant.Status> status) {
        return participantService.listParticipants(studyId).stream()
                .filter(p -> studyGroupId == null || studyGroupId.equals(p.getStudyGroupId()))
                .filter(p -> status == null || status.contains(p.getStatus()))
                .map(p -> new SimpleParticipant(p.getParticipantId(), p.getStart()))
                .collect(Collectors.toSet());
    }

    public Set<Integer> listActiveParticipantsByQuery(long studyId, Integer studyGroupId, String query, TimeRange timerange) {
        Set<Integer> participants = listParticipants(studyId, studyGroupId, Set.of(Participant.Status.ACTIVE))
                .stream().map(SimpleParticipant::getId).collect(Collectors.toSet());
        Set<Integer> allThatMatchQuery = new HashSet<>(elasticService.participantsThatMapQuery(studyId, studyGroupId, query, timerange));
        participants.retainAll(allThatMatchQuery);
        return participants;
    }

    public boolean sendPushNotification(long studyId, int participantId, String title, String message, Map<String, String> data) {
        LOGGER.debug("Sending message to participant (sid:{}, pid:{}): {} -- {}", studyId, participantId, title, message);
        return pushNotificationService.sendPushNotification(studyId, participantId, title, message, data);
    }

    public void storeDatapoint(
            ElasticDataPoint.Type type,
            long studyId,
            Integer studyGroupId,
            int participantId,
            int extendedComponentId,
            String componentType,
            Instant time,
            Map<String,Object> data
    ) {
        switch (type) {
            case action -> elasticService.setDataPoint(studyId, new ElasticActionDataPoint(
                    "SYS-" + UUID.randomUUID(),
                    "participant_" + participantId,
                    "study_" + studyId,
                    studyGroupId != null ? "study_group_" + studyGroupId : null,
                    String.valueOf(extendedComponentId),
                    componentType,
                    componentType,
                    time,
                    Instant.now(),
                    data
            ));
            case observation -> elasticService.setDataPoint(studyId, new ElasticObservationDataPoint(
                    "SYS-" + UUID.randomUUID(),
                    "participant_" + participantId,
                    "study_" + studyId,
                    studyGroupId != null ? "study_group_" + studyGroupId : null,
                    String.valueOf(extendedComponentId),
                    componentType,
                    componentType,
                    time,
                    Instant.now(),
                    data
            ));
        }
    }

    public void setPropertiesForParticipant(long studyId, Integer participantId, int observationId, ObservationProperties properties) {
        observationRepository.setParticipantProperties(studyId, participantId, observationId, properties);
    }

    public Optional<ObservationProperties> getPropertiesForParticipant(long studyId, Integer participantId, int observationId) {
        return observationRepository.getParticipantProperties(studyId, participantId, observationId);
    }

    public void removePropertiesForParticipant(long studyId, Integer participantId, int observationId) {
        observationRepository.removeParticipantProperties(studyId, participantId, observationId);
    }
}
