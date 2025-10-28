package io.redlink.more.studymanager.component.observation.garmin;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.io.Visibility;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class GarminObservationFactory<C extends Observation<P>, P extends ObservationProperties> extends ObservationFactory<C, P> {

    private static final Visibility visibility = new Visibility(false, true);

    @Override
    public GarminObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new GarminObservation(sdk, validate((P) properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return MeasurementSet.None;
    }

    @Override
    public String getId() {
        return "garmin-observation";
    }

    @Override
    public String getTitle() {
        return "observation.factory.garmin.title";
    }

    @Override
    public String getDescription() {
        return "observation.factory.garmin.description";
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }
}
