package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.component.observation.measurement.GenericMeasurementSets;
import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

public class ExternalObservationFactory<C extends Observation<P>, P extends ObservationProperties>
        extends ObservationFactory<C, P> {
    @Override
    public String getId() {
        return "external-observation";
    }

    @Override
    public String getTitle() {
        return "observation.factory.external.title";
    }

    @Override
    public String getDescription() {
        return "observation.factory.external.description";
    }

    @Override
    public ExternalObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new ExternalObservation(sdk, validate((P)properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return GenericMeasurementSets.NOT_SPECIFIED;
    }

    @Override
    public Boolean getHidden() {
        return false;
    }
}
