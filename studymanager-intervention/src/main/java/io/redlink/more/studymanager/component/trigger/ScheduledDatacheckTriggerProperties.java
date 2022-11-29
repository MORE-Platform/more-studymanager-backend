package io.redlink.more.studymanager.component.trigger;

import io.redlink.more.studymanager.core.properties.TriggerProperties;

import java.util.Optional;

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

    public Optional<Long> getWindow() {
        return Optional.ofNullable(this.getLong("window"));
    }

    public Optional<Boolean> isInverse() {
        return Optional.ofNullable(this.getBoolean("inverse"));
    }
}
