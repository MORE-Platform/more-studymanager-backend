package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.component.observation.measurement.GenericMeasurementSets;
import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.properties.model.IntegerValue;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

import java.util.List;

public class GpsMobileObservationFactory<C extends Observation<P>, P extends ObservationProperties> extends ObservationFactory<C, P> {

    private static List<Value> properties = List.of(
            new IntegerValue("location_interval_millis")
                    .setMax(600000)
                    .setMin(30000)
                    .setDefaultValue(60000)
                    .setName("Measurement Interval")
                    .setDescription("Measurement Interval in Milliseconds, 30K to 600k")
                    .setRequired(true)
    );

    @Override
    public String getId() {
        return "gps-mobile-observation";
    }

    @Override
    public String getTitle() {
        return "GPS Mobile Sensor";
    }

    @Override
    public String getDescription() {
        return
"""
This observation enables you to collect GPS data via the smartphone sensor.
""";
    }

    @Override
    public List<Value> getProperties() {
        return properties;
    }

    @Override
    public GpsMobileObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new GpsMobileObservation(sdk, validate((P)properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return GenericMeasurementSets.GEOLOCATION;
    }
}
