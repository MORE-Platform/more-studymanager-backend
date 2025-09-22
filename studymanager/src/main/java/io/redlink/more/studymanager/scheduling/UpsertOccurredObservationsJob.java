/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.scheduling;

import io.redlink.more.studymanager.action.ActionService;
import io.redlink.more.studymanager.core.exception.SchedulingException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.io.Parameters;
import io.redlink.more.studymanager.core.io.TriggerResult;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.model.timeline.ObservationTimelineEvent;
import io.redlink.more.studymanager.sdk.MoreSDK;
import io.redlink.more.studymanager.service.*;
import io.redlink.more.studymanager.utils.LoggingUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UpsertOccurredObservationsJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpsertOccurredObservationsJob.class);

    @Autowired
    private StudyService studyService;
    @Autowired
    private ParticipantService participantService;
    @Autowired
    private OccurredObservationService occurredObservationService;
    @Autowired
    private CalendarService calendarService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try (var ctx = LoggingUtils.createContext()) {
            Study study = studyService.getStudy(
                    context.getJobDetail().getJobDataMap().getLong("studyId"),
                    null
            ).orElse(null);
            if(study == null) {
                return; //nothing to do
            }
            ctx.putStudy(study);
            List<Participant> participants = participantService.listParticipants(study.getStudyId());
            Instant lastOccurredObservation = occurredObservationService.getLatestStartTime(study.getStudyId());
            //NOTE: use now + 2min for current because:
            //  1. we truncatedTo(ChronoUnit.MINUTES) all Instants
            //  2. we check with start.isBefore(current)
            // doing so will include all Observations that start in the current minute
            Instant current = Instant.now().plus(2, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES);

            for(Participant participant : participants) {
                ctx.putParticipant(participant);
                //NOTE: from, to are currently not supported
                var timeline = calendarService.getTimeline(study, participant, null, null, null, null);
                for(ObservationTimelineEvent event : timeline.observationTimelineEvents()){
                    var start = event.start().truncatedTo(ChronoUnit.MINUTES);
                    if((lastOccurredObservation == null || start.isAfter(lastOccurredObservation)) && start.isBefore(current)){
                        LOGGER.debug("upsert occurred observation for study: {}, observation: {}, participant: {}, start: {}, end: {}",
                                study.getStudyId(), event.observationId(), participant.getParticipantId(), event.start(), event.end());
                        occurredObservationService.upsert(study.getStudyId(), event.observationId(), participant.getParticipantId(), event.start(), event.end());
                    } //else outside of time range ... ignore
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot execute Trigger-Job: {}", e.getMessage(), e);
        }
    }
}
