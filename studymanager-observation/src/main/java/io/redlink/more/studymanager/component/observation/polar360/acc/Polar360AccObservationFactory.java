package io.redlink.more.studymanager.component.observation.polar360.acc;

import io.redlink.more.studymanager.component.observation.measurement.GenericMeasurementSets;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.properties.model.BooleanValue;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

import java.util.List;

public class Polar360AccObservationFactory
        extends ObservationFactory<Polar360AccObservation<ObservationProperties>, ObservationProperties> {

    private static final List<Value> properties = List.of(
            new BooleanValue("Offline_recording").setDefaultValue(false).setDescription("Offline recording on device or online streaming"),
            new BooleanValue("continuous_recording").setDefaultValue(false).setDescription("Continuous data collection with offline recording, restart recording after fetching records")
    );

    @Override
    public String getId() {
        return "polar360observation:acc";
    }

    @Override
    public String getTitle() {
        return "Polar 360 Accelerometer";
    }

    @Override
    public String getDescription() {
        return "Collect accelerometer (ACC) data from Polar 360 device";
    }

    @Override
    public List<Value> getProperties() {
        return properties;
    }

    @Override
    public Polar360AccObservation<ObservationProperties> create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new Polar360AccObservation<>(sdk, validate(properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return GenericMeasurementSets.NOT_SPECIFIED;
    }
}
