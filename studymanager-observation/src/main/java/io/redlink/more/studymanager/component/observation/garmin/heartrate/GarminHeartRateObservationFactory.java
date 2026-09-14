/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.studymanager.component.observation.garmin.heartrate;

import io.redlink.more.studymanager.component.observation.measurement.GenericMeasurementSets;
import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.io.Visibility;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class GarminHeartRateObservationFactory<C extends Observation<P>, P extends ObservationProperties> extends ObservationFactory<C, P> {

    private static final Visibility visibility = new Visibility(true, false);

    @Override
    public GarminHeartRateObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new GarminHeartRateObservation(sdk, validate((P) properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return GenericMeasurementSets.HEART_RATE;
    }

    @Override
    public String getId() {
        return "garmin-heart-rate-observation";
    }

    @Override
    public String getTitle() {
        return "observation.factory.garmin.heart-rate.title";
    }

    @Override
    public String getDescription() {
        return "observation.factory.garmin.heart-rate.description";
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }
}
