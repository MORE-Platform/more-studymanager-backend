package io.redlink.more.studymanager.component.observation.healthconnect.steps;

import io.redlink.more.studymanager.component.observation.measurement.HealthConnectMeasurementSets;
import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.io.Visibility;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class HealthConnectStepsObservationFactory<C extends Observation<P>, P extends ObservationProperties> extends ObservationFactory<C, P> {

    private static final Visibility visibility = new Visibility(true, false);

    @Override
    public HealthConnectStepsObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new HealthConnectStepsObservation(sdk, validate((P) properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return HealthConnectMeasurementSets.STEPS;
    }

    @Override
    public String getId() {
        return "health-connect-steps-observation";
    }

    @Override
    public String getTitle() {
        return "observation.factory.health-connect.steps.title";
    }

    @Override
    public String getDescription() {
        return "observation.factory.health-connect.steps.description";
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }
}
