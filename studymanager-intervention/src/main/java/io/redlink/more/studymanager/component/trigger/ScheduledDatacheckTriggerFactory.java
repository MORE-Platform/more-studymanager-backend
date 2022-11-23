package io.redlink.more.studymanager.component.trigger;

import io.redlink.more.studymanager.core.exception.ConfigurationValidationException;
import io.redlink.more.studymanager.core.factory.TriggerFactory;
import io.redlink.more.studymanager.core.sdk.MoreTriggerSDK;
import io.redlink.more.studymanager.core.validation.ConfigurationValidationReport;

public class ScheduledDatacheckTriggerFactory extends TriggerFactory<ScheduledDatacheckTrigger, ScheduledDatacheckTriggerProperties> {
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
        return "Checks if certain data occurs in a specific timeframe";
    }

    @Override
    public ScheduledDatacheckTriggerProperties validate(ScheduledDatacheckTriggerProperties properties) {
        ConfigurationValidationReport report = ConfigurationValidationReport.init();
        if(properties.getCronSchedule().isEmpty()) {
            report.missingProperty("cronSchedule");
        }
        if(properties.getQuery().isEmpty()) {
            report.missingProperty("query");
        }
        if(properties.getWindow().isEmpty()) {
            report.missingProperty("window");
        }

        if(report.isValid()) {
            return properties;
        } else {
            throw new ConfigurationValidationException(report);
        }
    }

    @Override
    public ScheduledDatacheckTrigger create(MoreTriggerSDK sdk, ScheduledDatacheckTriggerProperties properties) throws ConfigurationValidationException {
        //TODO
        return null;
    }
}
