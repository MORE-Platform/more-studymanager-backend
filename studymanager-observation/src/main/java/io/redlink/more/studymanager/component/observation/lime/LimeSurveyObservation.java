/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.observation.lime;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class LimeSurveyObservation<C extends ObservationProperties> extends Observation<C> {

    public static final String LIME_SURVEY_ID = "limeSurveyId";
    private final LimeSurveyRequestService limeSurveyRequestService;

    public LimeSurveyObservation(MoreObservationSDK sdk, C properties, LimeSurveyRequestService limeSurveyRequestService) throws ConfigurationValidationException {
        super(sdk, properties);
        this.limeSurveyRequestService = limeSurveyRequestService;
    }

    @Override
    public void activate(){
        String surveyId = checkAndGetSurveyId();

        Set<Integer> participantIds = sdk.participantIds(MorePlatformSDK.ParticipantFilter.ALL);
        participantIds.removeIf(id -> sdk.getPropertiesForParticipant(id).isPresent());
        limeSurveyRequestService.activateParticipants(participantIds, surveyId)
                .forEach(data ->
                    sdk.setPropertiesForParticipant(
                            Integer.parseInt(data.firstname()),
                            new ObservationProperties(
                                    Map.of("token", data.token(),
                                            "limeUrl", limeSurveyRequestService.getBaseUrl())
                            )
                    )
                );
        limeSurveyRequestService.setSurveyEndUrl(surveyId, sdk.getStudyId(), sdk.getObservationId());
        limeSurveyRequestService.activateSurvey(surveyId);
        sdk.setValue(LIME_SURVEY_ID, surveyId);
    }

    protected String checkAndGetSurveyId() {
        String newSurveyId = properties.getString(LIME_SURVEY_ID);
        String activeSurveyId = sdk.getValue(LIME_SURVEY_ID, String.class).orElse(null);

        if(activeSurveyId != null && !activeSurveyId.equals(newSurveyId)) {
            // TODO: throw new ConfigurationValidationException(ConfigurationValidationReport.of(ValidationIssue.immutablePropertyChanged(LimeSurveyObservationFactory.limeSurveyId)));

            throw new RuntimeException(String.format(
                    "SurveyId on Observation %s must not be changed: %s -> %s",
                    sdk.getObservationId(),
                    activeSurveyId,
                    newSurveyId
            ));
        } else {
            return newSurveyId;
        }
    }



    @Override
    public void deactivate() {
        // for downwards compatibility (already running studies)
        String newSurveyId = properties.getString(LIME_SURVEY_ID);
        String activeSurveyId = sdk.getValue(LIME_SURVEY_ID, String.class).orElse(null);

        if(activeSurveyId == null || activeSurveyId.equals(newSurveyId)) {
            sdk.setValue(LIME_SURVEY_ID, newSurveyId);
        }
    }

    public boolean writeDataPoints(String token, int surveyId, int savedId) {
        //check if token exists, get participant and answer and store as datapoint
        getParticipantForToken(token).ifPresent(participantId ->
                limeSurveyRequestService.getAnswer(token, surveyId, savedId).ifPresent(m ->
                        sdk.storeDataPoint(participantId, "lime-survey-observation", m)
                )
        );
        return true;
    }

    public Optional<Integer> getParticipantForToken(String token) {
        return sdk.participantIds(MorePlatformSDK.ParticipantFilter.ALL).stream()
                .filter(id ->
                        sdk.getPropertiesForParticipant(id)
                                .map(o -> o.getString("token"))
                                .map(t -> t.equals(token))
                                .orElse(false)
                )
                .findFirst();
    }
}
