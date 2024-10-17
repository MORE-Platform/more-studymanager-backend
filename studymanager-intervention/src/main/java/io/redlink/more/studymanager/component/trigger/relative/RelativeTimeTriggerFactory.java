/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.trigger.relative;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.core.properties.model.IntegerValue;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import java.util.List;

public class RelativeTimeTriggerFactory extends TriggerFactory<RelativeTimeTrigger, TriggerProperties> {

    private static List<Value> properties = List.of(
            new IntegerValue("day")
                    .setMin(1)
                    .setDefaultValue(1)
                    .setName("intervention.factory.trigger.relativeTime.configProps.day")
                    .setDescription("intervention.factory.trigger.relativeTime.configProps.dayDesc")
                    .setRequired(true),
            new IntegerValue("hour")
                    .setMin(0)
                    .setMax(23)
                    .setDefaultValue(1)
                    .setName("intervention.factory.trigger.relativeTime.configProps.hour")
                    .setDescription("intervention.factory.trigger.relativeTime.configProps.hourDesc")
                    .setRequired(true)
    );

    @Override
    public String getId() {
        return "relative-time-trigger";
    }

    @Override
    public String getTitle() {
        return "intervention.factory.trigger.relativeTime.title";
    }

    @Override
    public String getDescription() {
        return "intervention.factory.trigger.relativeTime.description";
    }

    @Override
    public List<Value> getProperties() {
        return properties;
    }

    @Override
    public RelativeTimeTrigger create(MoreTriggerSDK sdk, TriggerProperties properties) throws ConfigurationValidationException {
        return new RelativeTimeTrigger(sdk, properties);
    }
}
