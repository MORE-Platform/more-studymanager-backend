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

public abstract class Observation<C extends ObservationProperties> extends Component<C> {

    protected final MoreObservationSDK sdk;
    protected Observation(MoreObservationSDK sdk, C properties) throws ConfigurationValidationException {
        super(properties);
        this.sdk = sdk;
    }

    /**
     * Gets an array of DataViewInfo, which represents all possibilities to show this observation data.
     *
     * @return an array of DataViewInfo representing all possible views of the observation data
     */
    public DataViewInfo[] listViews() {
        return new DataViewInfo[0];
    }

    /**
     * Retrieves a specific DataView based on the given parameters.
     *
     * @param viewName the name of the view to retrieve
     * @param studyGroupId the ID of the study group for filter reasons
     * @param participantId the ID of the participant for filter reasons
     * @param timerange the time range for the data view for filter reasons
     * @return the requested DataView, or null if not found
     */
    public DataView getView(String viewName, Integer studyGroupId, Integer participantId, TimeRange timerange) {
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
