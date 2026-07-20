package io.redlink.more.studymanager.component.observation;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.ObservationFactory;
import io.redlink.more.studymanager.core.io.Visibility;
import io.redlink.more.studymanager.core.measurement.Measurement;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;

import java.util.Set;

public class AppUsageObservationFactory<C extends Observation<P>, P extends ObservationProperties> extends ObservationFactory<C, P> {
    public static final String DATA_FIELD = "user_action";

    private static final MeasurementSet measurements = new MeasurementSet(
            "USER_ACTION", Set.of(new Measurement(DATA_FIELD, Measurement.Type.OBJECT))
    );

    @Override
    public AppUsageObservation create(MoreObservationSDK sdk, ObservationProperties properties) throws ConfigurationValidationException {
        return new AppUsageObservation(sdk, validate((P) properties));
    }

    @Override
    public MeasurementSet getMeasurementSet() {
        return measurements;
    }

    @Override
    public String getId() {
        return "app-usage-observation";
    }

    @Override
    public String getTitle() {
        return "observation.factory.appUsage.title";
    }

    @Override
    public String getDescription() {
        return "observation.factory.appUsage.description";
    }

    @Override
    public Visibility getVisibility() {
        return new Visibility(false, true);
    }
}
