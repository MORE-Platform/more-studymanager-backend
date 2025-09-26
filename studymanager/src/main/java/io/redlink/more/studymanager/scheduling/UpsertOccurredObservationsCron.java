/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.scheduling;

import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.timeline.ObservationTimelineEvent;
import io.redlink.more.studymanager.service.*;
import io.redlink.more.studymanager.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class UpsertOccurredObservationsCron {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpsertOccurredObservationsCron.class);

    private final StudyService studyService;
    private final ParticipantService participantService;
    private final OccurredObservationService occurredObservationService;
    private final CalendarService calendarService;


    UpsertOccurredObservationsCron(
            StudyService studyService,
            ParticipantService participantService,
            OccurredObservationService occurredObservationService,
            CalendarService calendarService
    ) {
        this.studyService = studyService;
        this.participantService = participantService;
        this.occurredObservationService = occurredObservationService;
        this.calendarService = calendarService;
    }

    @Scheduled(cron="0 */5 * * * ?") //every 5min
    protected void upsertOccurredObservations(){
        //list all active studies
        studyService.getStudiesByStates(Study.Status.ACTIVE_STATES)
                .forEach(this::upsertOccurredObservations);
    }

    private void upsertOccurredObservations(Study study) {
        try (var ctx = LoggingUtils.createContext()) {
            if(study == null || !Study.Status.ACTIVE_STATES.contains(study.getStudyState())) {
                return; //nothing to do
            }
            ctx.putStudy(study);
            List<Participant> participants = participantService.listParticipants(study.getStudyId());
            Instant lastOccurredObservation = occurredObservationService.getLatestStartTime(study.getStudyId());
            //NOTE: use now + 1min for current because:
            //  1. we truncatedTo(ChronoUnit.MINUTES) all Instants and
            //  2. we check with start.isBefore(current)
            // doing so will include all Observations that start in the current minute
            Instant current = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES);
            LOGGER.debug("Upsert Occurred Observations for Study(id: {}) and timeperiode(start:{}, end:{})", study.getStudyId(), lastOccurredObservation, current);

            for(Participant participant : participants) {
                ctx.putParticipant(participant);
                //NOTE: from, to are currently not supported
                var timeline = calendarService.getTimeline(study, participant, null, null, null, null);
                for(ObservationTimelineEvent event : timeline.observationTimelineEvents()){
                    var start = event.start().truncatedTo(ChronoUnit.MINUTES);
                    if((lastOccurredObservation == null || start.isAfter(lastOccurredObservation)) && start.isBefore(current)){
                        LOGGER.trace("upsert occurred observation for study: {}, observation: {}, participant: {}, start: {}, end: {}",
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
