/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.action;

import io.redlink.more.studymanager.core.properties.model.ObjectValue;

public class TriggerObservationValue extends ObjectValue {
    public TriggerObservationValue(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return "OBSERVATION";
    }
}
