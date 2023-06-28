package io.redlink.more.studymanager.component.trigger;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.core.properties.model.StringValue;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;
import io.redlink.more.studymanager.core.validation.ValidationIssue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScheduledTriggerFactory extends TriggerFactory<ScheduledTrigger, TriggerProperties> {

    private static List<Value> properties = new ArrayList<>();

    static {
        StringValue prop = new StringValue("cronSchedule");

        prop.setValidationFunction((String s) -> {
            if(!QuartzCronExpressionValidator.validate(s)) {
                return ValidationIssue.error(prop, "Value is not a valid cronExpression");
            } else return ValidationIssue.NONE;
        });

        properties.add(prop.setRequired(true)
                .setName("intervention.factory.trigger.scheduled.configProps.cronName")
                .setDefaultValue("0 0 12 * * ?")
                .setDescription("intervention.factory.trigger.scheduled.configProps.cronDesc"));
    }

    @Override
    public String getId() {
        return "scheduled-trigger";
    }

    @Override
    public String getTitle() {
        return "intervention.factory.trigger.scheduled.title";
    }

    @Override
    public String getDescription() {
        return "intervention.factory.trigger.scheduled.description";
    }

    @Override
    public List<Value> getProperties() {
        return properties;
    }

    @Override
    public ScheduledTrigger create(MoreTriggerSDK sdk, TriggerProperties properties) throws ConfigurationValidationException {
        return new ScheduledTrigger(sdk, validate(properties));
    }
}
