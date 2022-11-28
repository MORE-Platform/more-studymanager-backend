package io.redlink.more.studymanager.component.trigger;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;

public class ScheduledDatacheckTriggerFactory extends TriggerFactory<ScheduledDatacheckTrigger, TriggerProperties> {
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
        return
"""
Checks if certain data occurs in a specific timeframe". Example: <code>
{
   "cronSchedule": "0 0 12 * * ?",
   "query": "field:*",
   "window": 100
}
</code>
<a target="_blank" href="http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html">Further info on cron values</a>
""";
    }

    @Override
    public TriggerProperties validate(TriggerProperties triggerProperties) {
        ConfigurationValidationReport report = ConfigurationValidationReport.init();
        ScheduledDatacheckTriggerProperties properties = new ScheduledDatacheckTriggerProperties(triggerProperties);

        try {
            if(properties.getCronSchedule().isEmpty()) {
                report.missingProperty("cronSchedule");
            } else {
                if(!properties.getCronSchedule().map(QuartzCronExpressionValidator::validate).orElse(false)) {
                    report.error("cronSchedule is not a valid cronExpression");
                }
            }
        } catch (ClassCastException e) {
            report.error("cronSchedule must a valid string");
        }

        try {
            if(properties.getQuery().isEmpty()) {
                report.missingProperty("query");
            }
        } catch (ClassCastException e) {
            report.error("query must a valid string");
        }

        try {
            if(properties.getWindow().isEmpty()) {
                report.missingProperty("window");
            }
        } catch (ClassCastException e) {
            report.error("window must a valid long");
        }

        if(report.isValid()) {
            return properties;
        } else {
            throw new ConfigurationValidationException(report);
        }
    }

    @Override
    public ScheduledDatacheckTrigger create(MoreTriggerSDK sdk, TriggerProperties properties) throws ConfigurationValidationException {
        return new ScheduledDatacheckTrigger(sdk, (ScheduledDatacheckTriggerProperties) validate(properties));
    }
}
