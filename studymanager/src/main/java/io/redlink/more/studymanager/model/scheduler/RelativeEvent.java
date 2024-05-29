package io.redlink.more.studymanager.model.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RelativeEvent implements ScheduleEvent {

    public static final String TYPE = "RelativeEvent";

    private RelativeDate dtstart;

    private RelativeDate dtend;

    private RelativeRecurrenceRule rrrule;

    public RelativeEvent() {
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public RelativeDate getDtstart() {
        return dtstart;
    }

    public RelativeEvent setDtstart(RelativeDate dtstart) {
        this.dtstart = dtstart;
        return this;
    }

    public RelativeDate getDtend() {
        return dtend;
    }

    public RelativeEvent setDtend(RelativeDate dtend) {
        this.dtend = dtend;
        return this;
    }

    public RelativeRecurrenceRule getRrrule() {
        return rrrule;
    }

    public RelativeEvent setRrrule(RelativeRecurrenceRule rrrule) {
        this.rrrule = rrrule;
        return this;
    }
}
