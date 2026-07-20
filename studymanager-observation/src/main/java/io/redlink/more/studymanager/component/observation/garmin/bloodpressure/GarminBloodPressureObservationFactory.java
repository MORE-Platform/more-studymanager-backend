package io.redlink.more.studymanager.component.observation.garmin.bloodpressure;

import io.redlink.more.studymanager.component.observation.measurement.GarminMeasurementSets;
import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.io.Visibility;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class GarminBloodPressureObservationFactory<C extends Observation<P>, P extends ObservationProperties> extends ObservationFactory<C, P> {

    private static final Visibility visibility = new Visibility(true, false);

    @Override
    public GarminBloodPressureObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new GarminBloodPressureObservation(sdk, validate((P) properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return GarminMeasurementSets.BLOOD_PRESSURE;
    }

    @Override
    public String getId() {
        return "garmin-blood-pressure-observation";
    }

    @Override
    public String getTitle() {
        return "observation.factory.garmin.blood-pressure.title";
    }

    @Override
    public String getDescription() {
        return "observation.factory.garmin.blood-pressure.description";
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }
}
