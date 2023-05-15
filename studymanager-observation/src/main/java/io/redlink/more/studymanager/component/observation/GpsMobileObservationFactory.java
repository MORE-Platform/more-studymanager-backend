package io.redlink.more.studymanager.component.observation;

import com.fasterxml.jackson.databind.JsonNode;
import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.exception.ValueCastException;
import io.redlink.more.studymanager.core.exception.ValueNonNullException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.properties.model.IntegerValue;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;
import io.redlink.more.studymanager.core.validation.ValidationIssue;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GpsMobileObservationFactory<C extends Observation<P>, P extends ObservationProperties> extends ObservationFactory<C, P> {

    private static List<Value> properties = List.of(
            new IntegerValue("location_interval_millis")
                    .setMax(600000)
                    .setName("Measurement Interval")
                    .setDescription("Measurement Interval in Milliseconds, 0 to 600k")
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
    public Map<String, Object> getDefaultProperties() {
        return Map.of(
                "location_interval_millis", 60000
        );
    }

    @Override
    public ObservationProperties validate(ObservationProperties values) {
        try {
            ConfigurationValidationReport report = ConfigurationValidationReport.of(
                    properties.stream()
                            .map(p -> p.validate(p.getValue(values)))
                            .filter(ValidationIssue::nonNone)
                            .collect(Collectors.toList())
            );

            if(report.isValid()) {
                return values;
            } else {
                throw new ConfigurationValidationException(report);
            }
        } catch (ValueCastException | ValueNonNullException e) {
            throw new ConfigurationValidationException(
                    ConfigurationValidationReport.of(ValidationIssue.error(
                            e.getValue(),
                            e.getMessage()
                    ))
            );
        }
    }

    @Override
    public GpsMobileObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new GpsMobileObservation(sdk, validate(properties));
    }
}
