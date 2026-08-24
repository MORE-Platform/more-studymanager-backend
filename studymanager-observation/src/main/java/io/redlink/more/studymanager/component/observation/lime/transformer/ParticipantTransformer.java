/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.observation.lime.transformer;

import io.redlink.more.studymanager.component.observation.lime.model.ParticipantCreationData;
import io.redlink.more.studymanager.component.observation.lime.model.ParticipantData;

public class ParticipantTransformer {
    public static ParticipantData transformToData(ParticipantCreationData requestData) {
        return new ParticipantData(
                new ParticipantData.ParticipantInfo(requestData.firstname(), requestData.lastname()),
                requestData.token(),
                null
        );
    }
}
