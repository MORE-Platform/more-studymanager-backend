package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;

import java.util.Map;

public class GpsMobileObservationFactory<C extends Observation, P extends ObservationProperties> extends ObservationFactory<C, P> {

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
Enables hart GPS data collection in mobile.
""";
    }

    @Override
    public Map<String, Object> getDefaultProperties() {
        return Map.of(
                "location_interval_millis", 60000
        );
    }

    @Override
    public ObservationProperties validate(ObservationProperties properties) {
        ConfigurationValidationReport report = ConfigurationValidationReport.init();
        try {
            if(properties.getLong("location_interval_millis") == null) {
                report.missingProperty("location_interval_millis");
            }
        } catch (ClassCastException e) {
            report.error("location_interval_millis must a valid long");
        }
        if(report.isValid()) {
            return properties;
        } else {
            throw new ConfigurationValidationException(report);
        }
    }

    @Override
    public GpsMobileObservation create(MorePlatformSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new GpsMobileObservation(sdk, validate(properties));
    }
}
