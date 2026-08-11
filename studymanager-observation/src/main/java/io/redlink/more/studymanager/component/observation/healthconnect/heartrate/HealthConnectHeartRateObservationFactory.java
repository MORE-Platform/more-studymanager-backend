package io.redlink.more.studymanager.component.observation.healthconnect.heartrate;

import io.redlink.more.studymanager.component.observation.measurement.GenericMeasurementSets;
import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.io.Visibility;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class HealthConnectHeartRateObservationFactory<C extends Observation<P>, P extends ObservationProperties> extends ObservationFactory<C, P> {

    private static final Visibility visibility = new Visibility(true, false);

    @Override
    public HealthConnectHeartRateObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new HealthConnectHeartRateObservation(sdk, validate((P) properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return GenericMeasurementSets.HEART_RATE;
    }

    @Override
    public String getId() {
        return "health-connect-heart-rate-observation";
    }

    @Override
    public String getTitle() {
        return "observation.factory.health-connect.heart-rate.title";
    }

    @Override
    public String getDescription() {
        return "observation.factory.health-connect.heart-rate.description";
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }
}
