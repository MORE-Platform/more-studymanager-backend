package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;

import java.util.Set;

public class LimeSurveyObservation<C extends ObservationProperties> extends Observation<C> {

    private final LimeSurveyRequestService limeSurveyRequestService;

    public LimeSurveyObservation(MorePlatformSDK sdk, C properties, LimeSurveyRequestService limeSurveyRequestService) throws ConfigurationValidationException {
        super(sdk, properties);
        this.limeSurveyRequestService = limeSurveyRequestService;
    }

    @Override
    public void activate(){
        Set<Integer> participantIds = sdk.participantIds();
        participantIds.removeIf(id -> sdk.getValue("participant" + id + "_token", String.class).isPresent());
        limeSurveyRequestService.activateParticipants(participantIds, properties.getString("limeSurveyId"))
                .forEach(data -> sdk.setValue("participant" + data.firstname() + "_token", data.token()));
    }
}
