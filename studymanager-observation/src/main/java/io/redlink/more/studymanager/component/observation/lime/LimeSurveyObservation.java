/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.observation.lime;

import io.redlink.more.studymanager.component.observation.QuestionObservationFactory;
import io.redlink.more.studymanager.component.observation.lime.model.ParticipantData;
import io.redlink.more.studymanager.component.observation.utils.QuestionObservationUtils;
import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.datavalidity.MeasurementSummary;
import io.redlink.more.studymanager.core.datavalidity.ObservationDataState;
import io.redlink.more.studymanager.core.datavalidity.ObservationDataSummary;
import io.redlink.more.studymanager.core.datavalidity.ObservationValidationResult;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LimeSurveyObservation<C extends ObservationProperties> extends Observation<C> {

    public static final String LIME_SURVEY_ID = "limeSurveyId";
    private static final String LIME_SURVEY_TOKEN_KEY = "token";
    private static final String LIME_SURVEY_URL_KEY = "limeUrl";
    private final LimeSurveyRequestService limeSurveyRequestService;
    private static final Logger LOGGER = LoggerFactory.getLogger(LimeSurveyObservation.class);

    public LimeSurveyObservation(MoreObservationSDK sdk, C properties, LimeSurveyRequestService limeSurveyRequestService) throws ConfigurationValidationException {
        super(sdk, properties);
        this.limeSurveyRequestService = limeSurveyRequestService;
    }

    @Override
    public void activate() {
        Optional<String> surveyId = checkAndGetSurveyId();
        if (surveyId.isEmpty()) {
            LOGGER.error("Lime Survey ID not present!");
            throw new IllegalStateException("Lime Survey ID not present!");
        }

        Set<Integer> participantIds = sdk.participantIds(MorePlatformSDK.ParticipantFilter.ALL);
        List<ParticipantData> limeParticipants = limeSurveyRequestService.listParticipants(surveyId.get(), 0, Math.max(1, participantIds.size()));

        Set<String> existingParticipantIds = limeParticipants.stream()
                .map(ParticipantData::firstname)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Integer> participantTokenIdsToDelete = limeParticipants.stream()
                .filter(participant ->
                        participant.tid() != null
                                && isNumeric(participant.firstname())
                                && !participantIds.contains(Integer.parseInt(participant.firstname())))
                .map(ParticipantData::tid)
                .collect(Collectors.toSet());

        limeSurveyRequestService.deleteParticipants(surveyId.get(), participantTokenIdsToDelete);

        Set<Integer> participantsToActivate = participantIds
                .stream()
                .filter(id -> !existingParticipantIds.contains(String.valueOf(id)))
                .collect(Collectors.toSet());
        List<ParticipantData> activatedParticipants = limeSurveyRequestService.activateParticipants(participantsToActivate, surveyId.get());

        var participantsToUpdate = limeParticipants.stream()
                .filter(p -> p.firstname() != null)
                .filter(p -> isNumeric(p.firstname()))
                .filter(p -> participantIds.contains(Integer.parseInt(p.firstname())));

        Stream.concat(participantsToUpdate, activatedParticipants.stream())
                .toList()
                .forEach(this::updateParticipants);

        //FIXME:With #476 the setSurveyEndUrl MUST point to the gateway!
        limeSurveyRequestService.setSurveyEndUrl(surveyId.get(), sdk.getStudyId(), sdk.getObservationId());
        limeSurveyRequestService.activateSurvey(surveyId.get());
        sdk.setValue(LIME_SURVEY_ID, surveyId.get());
    }

    protected Optional<String> checkAndGetSurveyId() {
        Optional<String> newSurveyId = Optional.ofNullable(properties.getString(LIME_SURVEY_ID));
        return newSurveyId.isPresent() ? newSurveyId : sdk.getValue(LIME_SURVEY_ID, String.class);
    }

    @Override
    public void deactivate() {
        // for downwards compatibility (already running studies)
        String newSurveyId = properties.getString(LIME_SURVEY_ID);
        String activeSurveyId = sdk.getValue(LIME_SURVEY_ID, String.class).orElse(null);

        if (activeSurveyId == null || activeSurveyId.equals(newSurveyId)) {
            String surveyIdToDeactivate = activeSurveyId != null ? activeSurveyId : newSurveyId;
            if (surveyIdToDeactivate != null && !surveyIdToDeactivate.isBlank()) {
                boolean paused = limeSurveyRequestService.deactivateSurvey(surveyIdToDeactivate);
                if (paused) {
                    LOGGER.info("Paused LimeSurvey survey {} during observation deactivation", surveyIdToDeactivate);
                } else {
                    LOGGER.info("LimeSurvey survey {} could not be paused during observation deactivation; keeping stored survey id for compatibility", surveyIdToDeactivate);
                }
            }
            sdk.setValue(LIME_SURVEY_ID, newSurveyId);
        }
    }

    public boolean writeDataPoints(String token, int surveyId, int savedId) {
        if (token == null || token.isBlank() || surveyId <= 0 || savedId <= 0) {
            LOGGER.warn("Skipping datapoint write because of invalid input: surveyId={}, savedId={}", surveyId, savedId);
            return false;
        }

        Optional<Integer> participantId = getParticipantForToken(token);
        if (participantId.isEmpty()) {
            LOGGER.warn("No participant found for token while writing datapoints for survey {} and savedId {}", surveyId, savedId);
            return false;
        }

        Optional<Map<String, Object>> answer = limeSurveyRequestService.getAnswer(token, surveyId, savedId);
        if (answer.isEmpty() || answer.get().isEmpty()) {
            LOGGER.warn("No answer found for token while writing datapoints for survey {} and savedId {} (participantId: {})", surveyId, savedId, participantId.get());
            return false;
        }

        sdk.storeDataPoint(participantId.get(), "lime-survey-observation", answer.get());
        return true;
    }

    public Optional<Integer> getParticipantForToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        return sdk.participantIds(MorePlatformSDK.ParticipantFilter.ALL).stream()
                .filter(id ->
                        sdk.getPropertiesForParticipant(id)
                                .map(o -> o.getString(LIME_SURVEY_TOKEN_KEY))
                                .map(token::equals)
                                .orElse(false)
                )
                .findFirst();
    }

    @Override
    public ObservationValidationResult validateData(Instant start, Instant end, ObservationDataSummary observationDataSummary) {
        if(observationDataSummary == null || observationDataSummary.measurements() == null) {
            return  new ObservationValidationResult(false, ObservationDataState.MISSING);
        }
        //(1) Use the default single Answer utility method
        var validationResult = QuestionObservationUtils.validateSingleAnswerObservation(
                observationDataSummary,
                LimeSurveyObservationFactory.MEASUREMENT_SEED //NOTE: This utility method only works with STRING fields!!
        );
        //(2) Check of the ID property is present
        MeasurementSummary answerMeasurementSummary = observationDataSummary.measurements().stream()
                .filter(it -> LimeSurveyObservationFactory.MEASUREMENT_ID.equals(it.getMeasurement().getId()))
                .findFirst()
                .orElse(null);
        //check that the field is present on all documents
        boolean hasId = answerMeasurementSummary != null
                && answerMeasurementSummary.getNumericResult() != null
                && answerMeasurementSummary.getNumericResult().missing() == 0;

        //(3) Adapt the validation result where necessary
        if(!validationResult.invalid() && validationResult.state() == ObservationDataState.COMPLETE && !hasId) {
            //The required field seed is missing in the results!
            return new ObservationValidationResult(
                    true,
                    ObservationDataState.INCOMPLETE
            );
        } else if(validationResult.state() == ObservationDataState.MISSING && hasId){
            //if seed is missing, but ID is present ... return INCOMPLETE instead of MISSING as result
            return new ObservationValidationResult(validationResult.invalid(), ObservationDataState.INCOMPLETE);
        } else { //just return the original validation format
            return validationResult;
        }
    }

    private void updateParticipants(ParticipantData participant) {
        if (participant == null || participant.firstname() == null || !isNumeric(participant.firstname())) {
            LOGGER.warn("Skipping LimeSurvey participant update for null or incomplete data: {}", participant);
            return;
        }
        if (participant.token() == null || participant.token().isBlank()) {
            LOGGER.warn("Skipping LimeSurvey participant update because token is missing: {}", participant);
            return;
        }
        sdk.mergePropertiesForParticipant(
                Integer.parseInt(participant.firstname()),
                new ObservationProperties(
                        Map.of(
                                LIME_SURVEY_TOKEN_KEY, participant.token(),
                                LIME_SURVEY_URL_KEY, limeSurveyRequestService.getBaseUrl()
                        )
                )
        );
    }

    private boolean isNumeric(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}