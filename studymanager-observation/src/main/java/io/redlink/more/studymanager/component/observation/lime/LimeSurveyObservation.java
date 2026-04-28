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
import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LimeSurveyObservation<C extends ObservationProperties> extends Observation<C> {

    private static final String LIME_SURVEY_USER_TEMPLATE = "study_%s-observation_%s-participant_%s";

    public static final String LIME_SURVEY_ID = "limeSurveyId";
    private static final String LIME_SURVEY_TOKEN_KEY = "token";
    private static final String LIME_SURVEY_URL_KEY = "limeUrl";
    private final LimeSurveyRequestService limeSurveyRequestService;
    private static final Logger LOGGER = LoggerFactory.getLogger(LimeSurveyObservation.class);

    public LimeSurveyObservation(MoreObservationSDK sdk, C properties, LimeSurveyRequestService limeSurveyRequestService) throws ConfigurationValidationException {
        super(sdk, properties);
        this.limeSurveyRequestService = limeSurveyRequestService;
    }

    private String getLimeSurveyUser(int participantId){
        return String.format(LIME_SURVEY_ID,
                sdk.getStudyId(),
                sdk.getObservationId(),
                participantId);
    }

    private Integer getParticipantId(ParticipantData limeSurveyParticipant){
        if(limeSurveyParticipant == null || limeSurveyParticipant.firstname() == null){
            return null;
        }
        //backward compatibility: in older Versions this used just the participantId as first name
        try {
            return Integer.parseInt(limeSurveyParticipant.firstname());
        } catch (NumberFormatException e){/* ignore*/}
        //the new syntax is
        //    firstname: `study_<study_id>-observation_<observation_id>-participant_<participant_id>`
        //    lastname: `more`
        String participantPrefix = String.format(LIME_SURVEY_USER_TEMPLATE, sdk.getStudyId(), sdk.getObservationId(), "");
        if("more".equals(limeSurveyParticipant.lastname()) &&
                limeSurveyParticipant.firstname().startsWith(participantPrefix)){
            try {
                return Integer.parseInt(limeSurveyParticipant.firstname().substring(participantPrefix.length()));
            } catch (NumberFormatException e) {
                /*ignroe*/
            }
        }
        return null;
    }

    private ParticipantData.ParticipantInfo toParticipantInfo(Integer participantId){
        return new ParticipantData.ParticipantInfo(
                String.format(LIME_SURVEY_USER_TEMPLATE, sdk.getStudyId(), sdk.getObservationId(), participantId),
                "more"
        );
    }


    @Override
    public void activate() {
        String surveyId = checkAndGetSurveyId()
                .orElseThrow(() -> {
                    LOGGER.error("Lime Survey ID not present (study: {}, Observation: {})!", sdk.getStudyId(), sdk.getObservationId());
                    return new IllegalStateException(String.format("No survey id provided for study %s and Observation %s",
                            sdk.getStudyId(), sdk.getObservationId()));
                });

        Set<Integer> participantIds = sdk.participantIds(MorePlatformSDK.ParticipantFilter.ALL);
        //NOTE: This should use paging and just return all participants, as one survey can be used by multiple
        //      studies/observations one can not really say how many participants are present
        //      If it selects not all this will create duplicate participants on every activation of the study!!
        Integer limit = Math.max(1000, participantIds.size()*2);
        List<ParticipantData> limeParticipants = limeSurveyRequestService.listParticipants(surveyId, 0, limit);

        //look for Limesurvey survey participants that where created for this observation/study
        Map<Integer, ParticipantData> existingParticipantIds = limeParticipants.stream()
                .map(it -> new AbstractMap.SimpleEntry<>(getParticipantId(it), it))
                .filter(entry -> entry.getKey() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        //filter existingParticipants that are no longer present in the study
        Map<Integer, ParticipantData> participantsToDelete = existingParticipantIds.entrySet().stream()
                .filter(entry -> !participantIds.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Set<Integer> participantTokenIdsToDelete = participantsToDelete.values().stream()
                .map(ParticipantData::tid)
                .collect(Collectors.toSet());

        limeSurveyRequestService.deleteParticipants(surveyId, participantTokenIdsToDelete);

        Set<ParticipantData.ParticipantInfo> participantsToActivate = participantIds
                .stream()
                .filter(id -> !existingParticipantIds.containsKey(id))
                .map(this::toParticipantInfo)
                .collect(Collectors.toSet());
        List<ParticipantData> activatedParticipants = limeSurveyRequestService.activateParticipants(participantsToActivate, surveyId);

        Stream<ParticipantData> participantsToUpdate = existingParticipantIds.entrySet()
                .stream()
                .filter(entry -> !participantsToDelete.containsKey(entry.getKey()))
                .map(Map.Entry::getValue);

        Stream.concat(participantsToUpdate, activatedParticipants.stream())
                .toList()
                .forEach(this::updateParticipants);

        //FIXME:With #476 the setSurveyEndUrl MUST point to the gateway!
        limeSurveyRequestService.setSurveyEndUrl(surveyId, sdk.getStudyId(), sdk.getObservationId());
        limeSurveyRequestService.activateSurvey(surveyId);
        sdk.setValue(LIME_SURVEY_ID, surveyId);
    }

    protected Optional<String> checkAndGetSurveyId() {
        Optional<String> newSurveyId = Optional.ofNullable(properties.getString(LIME_SURVEY_ID));
        return newSurveyId.isPresent() ? newSurveyId : sdk.getValue(LIME_SURVEY_ID, String.class);
    }

    @Override
    public void deactivate() {

        String newSurveyId = properties.getString(LIME_SURVEY_ID);
        String activeSurveyId = sdk.getValue(LIME_SURVEY_ID, String.class).orElse(null);

        if (activeSurveyId == null || activeSurveyId.equals(newSurveyId)) {
// NOTE: We can no longer deactivate surveys as those might be used by different studies. We do not want to
//       deactivate a survey used by an other study that is still active!
//            String surveyIdToDeactivate = activeSurveyId != null ? activeSurveyId : newSurveyId;
//            if (surveyIdToDeactivate != null && !surveyIdToDeactivate.isBlank()) {
//                boolean paused = limeSurveyRequestService.deactivateSurvey(surveyIdToDeactivate);
//                if (paused) {
//                    LOGGER.info("Paused LimeSurvey survey {} during observation deactivation", surveyIdToDeactivate);
//                } else {
//                    LOGGER.info("LimeSurvey survey {} could not be paused during observation deactivation; keeping stored survey id for compatibility", surveyIdToDeactivate);
//                }
//            }
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
        Integer pid = getParticipantId(participant);
        if (pid == null) {
            LOGGER.warn("Skipping LimeSurvey participant update for null or incomplete data: {}", participant);
            return;
        }
        if (participant.token() == null || participant.token().isBlank()) {
            LOGGER.warn("Skipping LimeSurvey participant update because token is missing: {}", participant);
            return;
        }
        sdk.mergePropertiesForParticipant(
                pid,
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