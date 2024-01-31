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
                    .setName("Day")
                    .setDescription("Day of participation")
                    .setRequired(true),
            new IntegerValue("hour")
                    .setMin(0)
                    .setMax(23)
                    .setDefaultValue(1)
                    .setName("Hour")
                    .setDescription("The hour of the given day")
                    .setRequired(true)
    );

    @Override
    public String getId() {
        return "relative-time-trigger";
    }

    @Override
    public String getTitle() {
        return "Relative Time Trigger";
    }

    @Override
    public String getDescription() {
        return "Triggers an action on a given relative time (day and hour-of-day)";
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
