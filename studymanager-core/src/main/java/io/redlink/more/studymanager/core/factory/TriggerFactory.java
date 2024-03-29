/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.core.factory;

import io.redlink.more.studymanager.core.component.Trigger;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.core.sdk.MorePlatformSDK;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;

public abstract class TriggerFactory<C extends Trigger, P extends TriggerProperties> extends ComponentFactory<C, P> {
    public abstract C create(MoreTriggerSDK sdk, P properties) throws ConfigurationValidationException;

    @Override
    public Class<TriggerProperties> getPropertyClass() {
        return TriggerProperties.class;
    }
}
