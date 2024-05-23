/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.factory;

import io.redlink.more.studymanager.core.component.Observation;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.io.Visibility;
import io.redlink.more.studymanager.core.measurement.MeasurementSet;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import io.redlink.more.studymanager.core.ui.DataPreview;

public abstract class ObservationFactory<C extends Observation<P>, P extends ObservationProperties> extends ComponentFactory<C, P> {
    public abstract C create(MoreObservationSDK sdk, P properties) throws ConfigurationValidationException;

    @Override
    public Class<ObservationProperties> getPropertyClass() {
        return ObservationProperties.class;
    }

    public abstract MeasurementSet getMeasurementSet();

    public abstract DataPreview getDataPreview();

    @Deprecated
    public Boolean getHidden() {
        return true;
    }

    public Visibility getVisibility() {
        return Visibility.DEFAULT;
    }
}
