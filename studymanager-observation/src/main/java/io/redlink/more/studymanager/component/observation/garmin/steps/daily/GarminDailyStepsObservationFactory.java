package io.redlink.more.studymanager.component.observation.garmin.steps.daily;

import io.redlink.more.studymanager.component.observation.measurement.GarminMeasurementSets;
import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.io.Visibility;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class GarminDailyStepsObservationFactory<C extends Observation<P>, P extends ObservationProperties> extends ObservationFactory<C, P> {

    private static final Visibility visibility = new Visibility(true, false);

    @Override
    public GarminDailyStepsObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new GarminDailyStepsObservation(sdk, validate((P) properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return GarminMeasurementSets.DAILY_STEPS;
    }

    @Override
    public String getId() {
        return "garmin-daily-steps-observation";
    }

    @Override
    public String getTitle() {
        return "observation.factory.garmin.steps.daily.title";
    }

    @Override
    public String getDescription() {
        return "observation.factory.garmin.steps.daily.description";
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }
}
