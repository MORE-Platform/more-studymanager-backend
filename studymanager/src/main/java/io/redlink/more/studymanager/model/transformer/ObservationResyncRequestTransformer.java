/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache License, Version 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.ObservationResyncRequestDTO;
import io.redlink.more.studymanager.model.ObservationResyncRequest;

public final class ObservationResyncRequestTransformer {

    private ObservationResyncRequestTransformer() {
    }

    public static ObservationResyncRequestDTO toObservationResyncRequestDTO_V1(ObservationResyncRequest request, boolean resyncable) {
        return new ObservationResyncRequestDTO()
                .studyId(request.studyId())
                .participantId(request.participantId())
                .observationId(request.observationId())
                .resyncable(resyncable)
                .pending(true)
                .created(request.created());
    }

    /**
     * The DTO for an observation with no pending resync request.
     */
    public static ObservationResyncRequestDTO toPendingFalseDTO_V1(Long studyId, Integer participantId, Integer observationId, boolean resyncable) {
        return new ObservationResyncRequestDTO()
                .studyId(studyId)
                .participantId(participantId)
                .observationId(observationId)
                .resyncable(resyncable)
                .pending(false);
    }
}
