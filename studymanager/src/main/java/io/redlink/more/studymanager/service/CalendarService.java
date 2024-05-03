package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.Participant;
import io.redlink.more.studymanager.model.Study;
import io.redlink.more.studymanager.model.User;
import io.redlink.more.studymanager.model.scheduler.Event;
import io.redlink.more.studymanager.model.scheduler.RelativeEvent;
import io.redlink.more.studymanager.model.scheduler.ScheduleEvent;
import io.redlink.more.studymanager.model.timeline.ObservationTimelineEvent;
import io.redlink.more.studymanager.model.timeline.StudyTimeline;
import io.redlink.more.studymanager.model.transformer.Transformers;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Objects;


@Service
public class CalendarService {

    private final StudyService studyService;
    private final ObservationService observationService;
    private final InterventionService interventionService;
    private final ParticipantService participantService;
    public CalendarService(StudyService studyService, ObservationService observationService, InterventionService interventionService,
                            ParticipantService participantService) {
        this.studyService = studyService;
        this.observationService = observationService;
        this.interventionService = interventionService;
        this.participantService = participantService;
    }

    public StudyTimeline getTimeline(Long studyId, Integer participantId, Integer studyGroupId, OffsetDateTime referenceDate, LocalDate from, LocalDate to, User currentUser) {
        Study study = studyService.getStudy(studyId, currentUser).orElse(null);
        if(study == null)
            return null;

        StudyTimeline studyTimeline = new StudyTimeline();
        Integer actualStudyGroupId;
        Instant relativeDate = null;

        if(participantId != null) {
            Participant participant = participantService.getParticipant(studyId, participantId);
            if(participant == null)
                return null;
            actualStudyGroupId = participant.getStudyGroupId();
            if (participant.getStart() != null) {
                relativeDate = participant.getStart();
            }
        } else {
            actualStudyGroupId = studyGroupId;
        }

        if(relativeDate == null) {
            if (referenceDate != null) {
                relativeDate = referenceDate.toInstant();
            } else if (study.getStartDate() != null) {
                relativeDate = study.getStartDate().atStartOfDay().toInstant(ZoneId.systemDefault().getRules().getOffset(Instant.now()));
            } else {
                relativeDate = study.getPlannedStartDate().atStartOfDay().toInstant(ZoneId.systemDefault().getRules().getOffset(Instant.now()));
            }
        }
        Instant finalRelativeDate = relativeDate;

        studyTimeline.addAllObservations(observationService.listObservations(studyId)
                .stream()
                .map(observation -> {
                    ScheduleEvent e = observation.getSchedule();
                    boolean isWithinTimeframe = true;
                    Instant start = null;
                    Instant end = null;

                    if(Event.TYPE.equals(e.getType())) {
                        Event event = (Event) e;
                        start = event.getDateStart();
                        end = event.getDateEnd();
                        isWithinTimeframe = start.isAfter(Transformers.toInstant(OffsetDateTime.from(from.atStartOfDay())))
                                    && end.isBefore(Transformers.toInstant(OffsetDateTime.from(to.atStartOfDay().plusDays(1))));
                    } else if(RelativeEvent.TYPE.equals(e.getType())) {
                        RelativeEvent event = (RelativeEvent) e;
                        start = finalRelativeDate.plus(
                                event.getDtstart().getOffset().getValue(),
                                ChronoUnit.valueOf(event.getDtstart().getOffset().getUnit().getValue())
                        );
                        end = finalRelativeDate.plus(
                                event.getDtend().getOffset().getValue(),
                                ChronoUnit.valueOf(event.getDtend().getOffset().getUnit().getValue())
                        );

                        isWithinTimeframe = start.isAfter(Transformers.toInstant(OffsetDateTime.from(from.atStartOfDay())))
                                && end.isBefore(Transformers.toInstant(OffsetDateTime.from(to.atStartOfDay().plusDays(1))));
                    }
                    if (Objects.equals(observation.getStudyGroupId(), actualStudyGroupId) && isWithinTimeframe)
                        return new ObservationTimelineEvent(
                                observation.getObservationId(),
                                observation.getStudyGroupId(),
                                observation.getTitle(),
                                observation.getPurpose(),
                                observation.getType(),
                                start,
                                end,
                                observation.getHidden(),
                                observation.getSchedule().getType()
                        );
                    return null;
                }).filter(Objects::nonNull)
                .toList()
        );

        return studyTimeline;
    }


}
