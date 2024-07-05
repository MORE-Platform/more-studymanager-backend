/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.component;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.io.TimeRange;
import io.redlink.more.studymanager.core.properties.ObservationProperties;
import io.redlink.more.studymanager.core.sdk.MoreObservationSDK;
import io.redlink.more.studymanager.core.ui.DataView;
import io.redlink.more.studymanager.core.ui.DataViewInfo;

import java.util.Set;

public abstract class Observation<C extends ObservationProperties> extends Component<C> {

    protected final MoreObservationSDK sdk;
    protected Observation(MoreObservationSDK sdk, C properties) throws ConfigurationValidationException {
        super(properties);
        this.sdk = sdk;
    }

    public Set<DataViewInfo> listViews() {
        return Set.of();
    }

    public DataView getView(String viewId, Integer studyGroupId, Integer participantId, TimeRange timerange) {
        return null;
    }


    @Override
    public void activate() {
        // no action
    }

    @Override
    public void deactivate() {
        // no action
    }
}
