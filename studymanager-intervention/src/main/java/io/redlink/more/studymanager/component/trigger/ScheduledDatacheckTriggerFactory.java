package io.redlink.more.studymanager.component.trigger;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.core.properties.model.IntegerValue;
import io.redlink.more.studymanager.core.properties.model.StringValue;
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
                .setName("Cron Schedule")
                .setDefaultValue("0 0 12 * * ?")
                .setDescription("Triggers and action based on a <a target=\"_blank\" href=\"http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html\">cron trigger.</a>\n"));

        properties.add(new StringValue("query")
                .setName("Query")
                .setDescription("The query for values in <a target=\"_blank\" href=\"https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-simple-query-string-query.html#simple-query-string-syntax\">simple query syntax.</a>.")
        );

        properties.add(new IntegerValue("window")
                .setName("Timewindow in seconds from querytime")
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
        return "Scheduled Datacheck";
    }

    @Override
    public String getDescription() {
        return "Checks if certain data occurs in a specific timeframe.";
    }

    @Override
    public List<Value> getProperties() {
        return properties;
    }

    @Override
    public ScheduledDatacheckTrigger create(MoreTriggerSDK sdk, TriggerProperties properties) throws ConfigurationValidationException {
        return new ScheduledDatacheckTrigger(sdk, (ScheduledDatacheckTriggerProperties) validate(properties));
    }
}
