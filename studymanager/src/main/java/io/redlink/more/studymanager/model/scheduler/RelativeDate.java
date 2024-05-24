package io.redlink.more.studymanager.model.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.time.LocalTime;

public class RelativeDate {

    private Duration offset;
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime time;

    public RelativeDate() {
    }

    @JsonIgnore
    public int getHours() {
        return time.getHour();
    }

    @JsonIgnore
    public int getMinutes() {
        return time.getMinute();
    }


    public Duration getOffset() {
        return offset;
    }

    public RelativeDate setOffset(Duration offset) {
        this.offset = offset;
        return this;
    }

    public LocalTime getTime() {
        return time;
    }

    public RelativeDate setTime(LocalTime time) {
        this.time = time;
        return this;
    }
}
