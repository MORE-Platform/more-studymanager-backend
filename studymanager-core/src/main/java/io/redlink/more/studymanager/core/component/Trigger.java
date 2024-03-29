/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.component;

import io.redlink.more.studymanager.core.io.Parameters;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.io.TriggerResult;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;

public abstract class Trigger<C extends TriggerProperties> extends Component<C> {

    protected final MoreTriggerSDK sdk;

    protected Trigger(MoreTriggerSDK sdk, C properties) throws ConfigurationValidationException {
        super(properties);
        this.sdk = sdk;
    }

    public abstract TriggerResult execute(Parameters parameters);


}
