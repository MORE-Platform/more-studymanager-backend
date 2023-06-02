package io.redlink.more.studymanager.component.trigger.datacheck;

import com.fasterxml.jackson.core.type.TypeReference;
import io.redlink.more.studymanager.core.properties.TriggerProperties;

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

    public Optional<String> getQuery() {
        return Optional.ofNullable(this.getString("query"));
    }

    public Optional<Set<QueryObject>> getQueryObject() {return this.getObject("queryObject", new TypeReference<>() {});}

    public Optional<Long> getWindow() {
        return Optional.ofNullable(this.getLong("window"));
    }

    public Optional<String> getElasticQueryString() {
        return this.getQueryObject().map(o -> o.stream()
                .map(qo ->
                        qo.toQueryString() + (qo.nextGroupCondition != null ? " " + qo.nextGroupCondition.value() : ""))
                .collect(Collectors.joining(" ")));
    }
}
