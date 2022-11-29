package io.redlink.more.studymanager.component.trigger;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.properties.TriggerProperties;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;

public class ScheduledTriggerFactory extends TriggerFactory<ScheduledTrigger, TriggerProperties> {

    public static final String CRON_SCHEDULE = "cronSchedule";

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
        return """
Triggers and action based on a cron trigger. Example: <code>
{
   "cronSchedule": "0 0 12 * * ?",
}
</code>
<a target="_blank" href="http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html">Further info on cron values</a>
""";
    }

    @Override
    public TriggerProperties validate(TriggerProperties properties) {
        ConfigurationValidationReport report = ConfigurationValidationReport.init();

        try {
            if(properties.getString(CRON_SCHEDULE) == null) {
                report.missingProperty(CRON_SCHEDULE);
            } else {
                if(!QuartzCronExpressionValidator.validate(properties.getString(CRON_SCHEDULE))) {
                    report.error("cronSchedule is not a valid cronExpression");
                }
            }
        } catch (ClassCastException e) {
            report.error("cronSchedule must a valid string");
        }


        if(report.isValid()) {
            return properties;
        } else {
            throw new ConfigurationValidationException(report);
        }
    }

    @Override
    public ScheduledTrigger create(MoreTriggerSDK sdk, TriggerProperties properties) throws ConfigurationValidationException {
        return new ScheduledTrigger(sdk, validate(properties));
    }
}
