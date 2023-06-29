package io.redlink.more.studymanager.component.trigger.datacheck;

import io.redlink.more.studymanager.component.trigger.QuartzCronExpressionValidator;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.core.properties.model.IntegerValue;
import io.redlink.more.studymanager.core.properties.model.StringValue;
import io.redlink.more.studymanager.core.properties.model.StringListValue;
import io.redlink.more.studymanager.core.properties.model.Value;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.validation.ValidationIssue;

import java.util.ArrayList;
import java.util.List;

public class ScheduledDatacheckTriggerFactory extends TriggerFactory<ScheduledDatacheckTrigger, TriggerProperties> {

    private static List<Value> properties = new ArrayList<>();

    static {
        StringValue prop = new StringValue("cronSchedule");

        prop.setValidationFunction((String s) -> {
            if(!QuartzCronExpressionValidator.validate(s)) {
                return ValidationIssue.error(prop, "Value is not a valid cronExpression");
            } else return ValidationIssue.NONE;
        });

        properties.add(prop.setRequired(true)
                .setName("intervention.factory.trigger.scheduledDatacheck.configProps.cronName")
                .setDefaultValue("0 0 12 * * ?")
                .setDescription("intervention.factory.trigger.scheduledDatacheck.configProps.cronDesc"));

        properties.add(new StringValue("query")
                .setName("intervention.factory.trigger.scheduledDatacheck.configProps.queryName")
                .setDescription("intervention.factory.trigger.scheduledDatacheck.configProps.queryDesc")
        );

        properties.add(new StringListValue("queryObject")
        .setName("intervention.factory.trigger.scheduled.configProps.queryObjName")
        .setDescription("intervention.factory.trigger.scheduled.configProps.queryObjDesc")
        );

        properties.add(new IntegerValue("window")
                .setName("intervention.factory.trigger.scheduledDatacheck.configProps.timeWindowName")
                .setRequired(true)
                .setDefaultValue(100)
        );
    }

    @Override
    public String getId() {
        return "scheduled-datacheck-trigger";
    }

    @Override
    public String getTitle() {
        return "intervention.factory.trigger.scheduledDatacheck.title";
    }

    @Override
    public String getDescription() {
        return "intervention.factory.trigger.scheduledDatacheck.decription";
    }

    @Override
    public List<Value> getProperties() {
        return properties;
    }

    @Override
    public ScheduledDatacheckTrigger create(MoreTriggerSDK sdk, TriggerProperties properties) throws ConfigurationValidationException {
        return new ScheduledDatacheckTrigger(sdk, new ScheduledDatacheckTriggerProperties(validate(properties)));
    }
}
