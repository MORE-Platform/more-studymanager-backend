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

    private final LimeSurveyRequestService limeSurveyRequestService;

    public LimeSurveyObservation(MoreObservationSDK sdk, C properties, LimeSurveyRequestService limeSurveyRequestService) throws ConfigurationValidationException {
        super(sdk, properties);
        this.limeSurveyRequestService = limeSurveyRequestService;
    }

    @Override
    public void activate(){
        Set<Integer> participantIds = sdk.participantIds(MorePlatformSDK.ParticipantFilter.ALL);
        String surveyId = properties.getString("limeSurveyId");
        //TODO disable keys fromm removed?
        participantIds.removeIf(id -> sdk.getPropertiesForParticipant(id).isPresent());
        limeSurveyRequestService.activateParticipants(participantIds, surveyId)
                .forEach(data -> {
                    sdk.setPropertiesForParticipant(
                            Integer.parseInt(data.firstname()),
                            new ObservationProperties(Map.of("token", data.token()))
                    );
                });
        limeSurveyRequestService.setSurveyEndUrl(surveyId, sdk.getStudyId(), sdk.getObservationId());
        limeSurveyRequestService.activateSurvey(surveyId);
    }

    public boolean writeDataPoints(String token, int surveyId, int savedId) {
        //check if token exists, get participant and answer and store as datapoint
        getParticipantForToken(token).ifPresent(participantId -> {
            limeSurveyRequestService.getAnswer(token, surveyId, savedId).ifPresent(m -> {
                sdk.storeDataPoint(participantId, "lime-survey-observation", m);
            });
        });
        return true;
    }

    public Optional<Integer> getParticipantForToken(String token) {
        return sdk.participantIds(MorePlatformSDK.ParticipantFilter.ALL).stream().filter(id -> {
            return sdk.getPropertiesForParticipant(id)
                    .map(o -> o.getString("token"))
                    .map(t -> t.equals(token))
                    .orElse(false);
        }).findFirst();

    }
}
