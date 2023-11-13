/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
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
                .setName("intervention.factory.trigger.scheduledDatacheck.configProps.cronName")
                .setDefaultValue("0 0 12 * * ?")
                .setDescription("intervention.factory.trigger.scheduledDatacheck.configProps.cronDesc"));

        properties.add(new IntegerValue("window")
                .setName("intervention.factory.trigger.scheduledDatacheck.configProps.timeWindowName")
                .setRequired(true)
                .setDefaultValue(100)
        );

        properties.add(new BooleanValue("onlyOnce")
                .setName("intervention.factory.trigger.scheduledDatacheck.configProps.onlyOnceName")
                .setDefaultValue(false)
                .setDescription("intervention.factory.trigger.scheduledDatacheck.configProps.onlyOnceDesc")
        );

        properties.add(new DatacheckQueryValue("queryObject")
                .setName("intervention.factory.trigger.scheduled.configProps.queryObjName")
                .setDescription("intervention.factory.trigger.scheduled.configProps.queryObjDesc")
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
        return "intervention.factory.trigger.scheduledDatacheck.description";
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
