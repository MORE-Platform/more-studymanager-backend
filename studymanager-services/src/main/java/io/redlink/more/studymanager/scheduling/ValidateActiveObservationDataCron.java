package io.redlink.more.studymanager.scheduling;

import io.redlink.more.studymanager.core.datavalidity.ObservationDataState;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.io.TimeRange;
import io.redlink.more.studymanager.core.io.Timeframe;
import io.redlink.more.studymanager.model.*;
import io.redlink.more.studymanager.sdk.MoreSDK;
import io.redlink.more.studymanager.service.ObservationService;
import io.redlink.more.studymanager.service.OccurredObservationService;
import io.redlink.more.studymanager.service.ParticipantService;
import io.redlink.more.studymanager.service.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class ValidateActiveObservationDataCron {

    private static final Logger log = LoggerFactory.getLogger(ValidateActiveObservationDataCron.class);
    private final StudyService studyService;
    private final ParticipantService participantService;
    private final OccurredObservationService occurredObservationService;
    private final ObservationService observationService;
    private final MoreSDK sdk;


    public ValidateActiveObservationDataCron(
            StudyService studyService,
            ParticipantService participantService,
            OccurredObservationService occurredObservationService,
            ObservationService observationService,
            MoreSDK sdk
    ){
        this.studyService = studyService;
        this.participantService = participantService;
        this.occurredObservationService = occurredObservationService;
        this.observationService = observationService;
        this.sdk = sdk;
    }



    @Scheduled(cron="30 */5 * * * ?") //every 5min, 30sec after updating active observation data
    public void validateActiveObservationData(){
        Map<String, ObservationFactory>  observationFactories = new HashMap<>();
        studyService.getStudiesByStates(Study.Status.ACTIVE_STATES).forEach(study -> {
            //NOTE: OccurredObservations will refer the same observations and participants several times, A single lookup
            //      per run is sufficient, so we keep them in a cache
            Map<Integer, Observation> observationCache = new HashMap<>();
            Map<Integer, Participant> participantCache = new HashMap<>();
            Map<Integer, io.redlink.more.studymanager.core.component.Observation> observationComponentCache = new HashMap<>();
            try (var ooStrem = occurredObservationService.streamActiveOccurredObservations(study.getStudyId(), false)) {
                ooStrem.forEach(occuredObservation ->
                    validateOccurrence(
                            study,
                            occuredObservation,
                            observationCache,
                            observationFactories,
                            observationComponentCache,
                            participantCache));
            }
        });
    }

    private void validateOccurrence(Study study, OccurredObservation occuredObservation, Map<Integer, Observation> observationCache, Map<String, ObservationFactory> observationFactories, Map<Integer, io.redlink.more.studymanager.core.component.Observation> observationComponentCache, Map<Integer, Participant> participantCache) {
        var observation = observationCache.computeIfAbsent(
                occuredObservation.observationId(),
                key -> observationService.getObservation(study.getStudyId(), key).orElse(null));
        var observationFactory = observationFactories.computeIfAbsent(
                observation.getType(),
                key -> observationService.getObservationFactory(observation).orElse(null));
        io.redlink.more.studymanager.core.component.Observation observationComponent;
        if (observationFactory != null) {
            observationComponent = observationComponentCache.computeIfAbsent(
                    observation.getObservationId(),
                    key -> observationFactory.create(
                            sdk.scopedObservationSDK(study.getStudyId(), observation.getStudyGroupId(), key),
                            observation.getProperties()));
        } else {
            observationComponent = null;
        }
        validate(new ValidateionContext(
                study,
                occuredObservation,
                participantCache.computeIfAbsent(
                        occuredObservation.participantId(),
                        key -> participantService.getParticipant(study.getStudyId(), key)),
                observation,
                observationFactory,
                observationComponent));
    }


    private void validate(ValidateionContext ctx) {
        if(Instant.now().isBefore(ctx.occurrence.start())) {
            //do not try to validate occurrences that are in the future
            log.warn("Unexpected call to validate {} with a future start timestamp", ctx.occurrence);
            return;
        }
        var validationResults = sdk.validateData(
            ctx.getStudyId(), ctx.getStudyGroupId(),ctx.getObservationId(),
            ctx.participant.getParticipantId(), ctx.getTimeRage(),
            ctx.observationFactory.getMeasurementSet());
        final OccurredObservation updatedOccurredObservation;
        if(validationResults != null) {
            var validationResult = ctx.observationComponent.validateData(ctx.occurrence.start(), ctx.occurrence.end(), validationResults);
            updatedOccurredObservation = new OccurredObservation(
                    ctx.occurrence.studyId(),
                    ctx.occurrence.observationId(),
                    ctx.occurrence.participantId(),
                    ctx.occurrence.start(),
                    ctx.occurrence.end(),
                    !validationResult.invalid(),
                    validationResult.state(),
                    ctx.occurrence.properties()
            );
        } else { //validation failed
            updatedOccurredObservation = new OccurredObservation(
                    ctx.occurrence.studyId(),
                    ctx.occurrence.observationId(),
                    ctx.occurrence.participantId(),
                    ctx.occurrence.start(),
                    ctx.occurrence.end(),
                    ctx.occurrence.dataValid(),
                    ObservationDataState.MISSING,
                    ctx.occurrence.properties()
            );
        }
        occurredObservationService.update(updatedOccurredObservation);
    }


    private record ValidateionContext(
        Study study,
        OccurredObservation occurrence,
        Participant participant,
        Observation observation,
        ObservationFactory observationFactory,
        io.redlink.more.studymanager.core.component.Observation observationComponent
    ) {

        public Long getStudyId(){
            return study.getStudyId();
        }

        public Integer getStudyGroupId(){
            return observation.getStudyGroupId();
        }

        public Integer getObservationId(){
            return observation.getObservationId();
        }

        public Integer getParticipantId(){
            return participant.getParticipantId();
        }

        public TimeRange getTimeRage() {
            return new Timeframe(occurrence.start(), occurrence.end());
        }
    }

}
