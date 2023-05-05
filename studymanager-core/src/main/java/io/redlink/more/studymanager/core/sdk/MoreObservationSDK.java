package io.redlink.more.studymanager.core.sdk;

import io.redlink.more.studymanager.core.properties.ObservationProperties;

import java.util.Optional;

public interface MoreObservationSDK extends MorePlatformSDK {

    void setPropertiesForParticipant(Integer participantId, ObservationProperties properties);
    Optional<ObservationProperties> getPropertiesForParticipant(Integer participantId);

    void removePropertiesForParticipant(Integer participantId);
}
