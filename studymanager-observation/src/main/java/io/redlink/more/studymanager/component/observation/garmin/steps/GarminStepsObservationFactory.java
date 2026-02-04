package io.redlink.more.studymanager.component.observation.garmin.steps;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.io.Visibility;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class GarminStepsObservationFactory<C extends Observation<P>, P extends ObservationProperties> extends ObservationFactory<C, P> {

    private static final Visibility visibility = new Visibility(true, false);

    @Override
    public GarminStepsObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new GarminStepsObservation(sdk, validate((P) properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return MeasurementSet.None;
    }

    @Override
    public String getId() {
        return "garmin-steps-observation";
    }

    @Override
    public String getTitle() {
        return "observation.factory.garmin.steps.title";
    }

    @Override
    public String getDescription() {
        return "observation.factory.garmin.steps.description";
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }
}
