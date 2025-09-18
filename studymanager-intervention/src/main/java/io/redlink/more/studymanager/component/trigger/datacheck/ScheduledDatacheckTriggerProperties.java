/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.trigger.datacheck;

import com.fasterxml.jackson.core.type.TypeReference;
import io.redlink.more.studymanager.core.properties.TriggerProperties;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ScheduledDatacheckTriggerProperties extends TriggerProperties {
    public ScheduledDatacheckTriggerProperties(TriggerProperties triggerProperties) {
        super(triggerProperties);
    }

    public Optional<String> getCronSchedule() {
        return Optional.ofNullable(this.getString("cronSchedule"));
    }

    public Optional<List<QueryObject>> getQueryObject() {return this.getObject("queryObject", new TypeReference<>() {});}

    public Optional<Long> getWindow() {
        return Optional.ofNullable(this.getLong("window"));
    }

    public Optional<Boolean> getOnlyOnce() {
        return Optional.ofNullable(this.getBoolean("onlyOnce"));
    }

    public Optional<String> getElasticQueryString() {
        return this.getQueryObject().map(o -> o.stream()
                .map(qo ->
                        qo.toQueryString() + (qo.nextGroupCondition != null ? " " + qo.nextGroupCondition.value() : ""))
                .collect(Collectors.joining(" ")));
    }
}
