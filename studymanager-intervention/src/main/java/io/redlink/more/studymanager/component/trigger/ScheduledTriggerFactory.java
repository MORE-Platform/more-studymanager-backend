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
                .setName("Cron Schedule")
                .setDescription("Triggers and action based on a cron trigger.<a target=\"_blank\" href=\"http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html\">Further info on cron values!</a>\n"));
    }

    @Override
    public String getId() {
        return "scheduled-trigger";
    }

    @Override
    public String getTitle() {
        return "Scheduled Trigger";
    }

    @Override
    public String getDescription() {
        return "Triggers and action based on a cron trigger.";
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
