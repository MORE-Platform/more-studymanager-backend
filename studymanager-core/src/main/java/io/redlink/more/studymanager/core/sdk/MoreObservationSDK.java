/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.sdk;

import io.redlink.more.studymanager.core.properties.ObservationProperties;

import java.util.Map;
import java.util.Optional;

public interface MoreObservationSDK extends MorePlatformSDK {

    void setPropertiesForParticipant(Integer participantId, ObservationProperties properties);
    Optional<ObservationProperties> getPropertiesForParticipant(Integer participantId);

    void removePropertiesForParticipant(Integer participantId);

    void storeDataPoint(Integer participantId, String observationType, Map data);

    int getObservationId();
}
