package io.redlink.more.studymanager.component.trigger;

import io.redlink.more.studymanager.core.properties.TriggerProperties;

import java.util.Optional;

public class ScheduledDatacheckTriggerProperties extends TriggerProperties {
    public Optional<String> getCronSchedule() {
        return Optional.ofNullable(this.getString("cronSchedule"));
    }

    public Optional<String> getQuery() {
        return Optional.ofNullable(this.getString("query"));
    }

    public Optional<Long> getWindow() {
        return Optional.ofNullable(this.getLong("window"));
    }
}
