package io.redlink.more.studymanager.sdk;

import io.redlink.more.studymanager.core.sdk.MoreActionSDK;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.sdk.schedule.Schedule;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.core.io.Timeframe;
import io.redlink.more.studymanager.repository.NameValuePairRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MoreSDK {

    private final Logger LOGGER = LoggerFactory.getLogger(MoreSDK.class);
    private final NameValuePairRepository nvpairs;

    private final SchedulingService schedulingService;

    private final ParticipantService participantService;

    private final ElasticService elasticService;

    private final PushNotificationService pushNotificationService;

    public MoreSDK(
            NameValuePairRepository nvpairs,
            SchedulingService schedulingService,
            ParticipantService participantService,
            ElasticService elasticService,
            PushNotificationService pushNotificationService) {
        this.nvpairs = nvpairs;
        this.schedulingService = schedulingService;
        this.participantService = participantService;
        this.elasticService = elasticService;
        this.pushNotificationService = pushNotificationService;
    }

    public <T extends Serializable> void setValue(String issuer, String name, T value) {
        nvpairs.setValue(issuer, name, value);
    }

    public <T extends Serializable> Optional<T> getValue(String issuer, String name, Class<T> tClass) {
        return nvpairs.getValue(issuer, name, tClass);
    }

    public void removeValue(String issuer, String name) {
        nvpairs.removeValue(issuer, name);
    }

    public MoreActionSDK scopedActionSDK(Long studyId, Integer studyGroupId, int interventionId, int actionId, int participantId) {
        return new MoreActionSDKImpl(this, studyId, studyGroupId, interventionId, actionId, participantId);
    }

    public MorePlatformSDK scopedPlatformSDK(Long studyId, Integer studyGroupId, int observationId) {
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

    public Set<Integer> listParticipants(long studyId, Integer studyGroupId) {
        return participantService.listParticipants(studyId).stream()
                .filter(p -> studyGroupId == null || studyGroupId.equals(p.getStudyGroupId()))
                .map(Participant::getParticipantId)
                .collect(Collectors.toSet());
    }

    public Set<Integer> listParticipantsByQuery(long studyId, Integer studyGroupId, String query, Timeframe timeframe, boolean inverse) {
        if(inverse) {
            Set<Integer> participants = listParticipants(studyId, studyGroupId);
            Set<Integer> allThatMatchQuery = new HashSet(elasticService.participantsThatMapQuery(studyId, studyGroupId, query, timeframe));
            participants.removeAll(allThatMatchQuery);
            return participants;
        } else {
            return new HashSet(elasticService.participantsThatMapQuery(studyId, studyGroupId, query, timeframe));
        }
    }

    public void sendPushNotification(long studyId, int participantId, String title, String message) {
        LOGGER.info("Send message to participant (sid:{}, pid:{}): {} -- {}", studyId, participantId, title, message);
        pushNotificationService.sendPushNotification(studyId, participantId, title, message);
    }
}
