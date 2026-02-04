package io.redlink.more.studymanager.component.observation.garmin.activity;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.io.Visibility;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class GarminActivityObservationFactory<C extends Observation<P>, P extends ObservationProperties> extends ObservationFactory<C, P> {

    private static final Visibility visibility = new Visibility(true, false);

    @Override
    public GarminActivityObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new GarminActivityObservation(sdk, validate((P) properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return MeasurementSet.None;
    }

    @Override
    public String getId() {
        return "garmin-activity-observation";
    }

    @Override
    public String getTitle() {
        return "observation.factory.garmin.activity.title";
    }

    @Override
    public String getDescription() {
        return "observation.factory.garmin.activity.description";
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }
}
