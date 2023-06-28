package io.redlink.more.studymanager.component.trigger.datacheck;

import io.redlink.more.studymanager.component.trigger.CronValue;
import io.redlink.more.studymanager.component.trigger.QuartzCronExpressionValidator;
import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.core.properties.model.*;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.validation.ValidationIssue;

import java.util.ArrayList;
import java.util.List;

public class ScheduledDatacheckTriggerFactory extends TriggerFactory<ScheduledDatacheckTrigger, TriggerProperties> {

    private static List<Value> properties = new ArrayList<>();

    static {
        CronValue cronProp = new CronValue("cronSchedule");

        cronProp.setValidationFunction((String s) -> {
            if(!QuartzCronExpressionValidator.validate(s)) {
                return ValidationIssue.error(cronProp, "Value is not a valid cronExpression");
            } else return ValidationIssue.NONE;
        });

        properties.add(cronProp.setRequired(true)
                .setName("Cron Schedule")
                .setDefaultValue("0 0 12 * * ?")
                .setDescription("Triggers and action based on a <a target=\"_blank\" href=\"http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html\">cron trigger.</a>\n"));

        properties.add(new StringValue("query")
                .setName("Query (deprecated)")
                .setDescription("The query for values in <a target=\"_blank\" href=\"https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-simple-query-string-query.html#simple-query-string-syntax\">simple query syntax.</a>.")
        );

        properties.add(new DatacheckQueryValue("queryObject")
                .setName("Query")
                .setDescription("The query that is executed on each trigger-time. If it matches, the actions are issued once. Actions are only triggered again, when query does not match in the meantime.")
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
        return new ScheduledDatacheckTrigger(sdk, new ScheduledDatacheckTriggerProperties(validate(properties)));
    }
}
