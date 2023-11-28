package io.redlink.more.studymanager.model.scheduler;

import io.redlink.more.studymanager.api.v1.model.RelativeDateDTO;
import io.redlink.more.studymanager.api.v1.model.RelativeRecurrenceRuleDTO;

public class RelativeEvent implements ScheduleEvent {

    public static final String TYPE = "RelativeEvent";

    private String type;

    private RelativeDate dtstart;

    private RelativeDate dtend;

    private RelativeRecurrenceRule rrrule;

    public RelativeEvent() {
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public RelativeEvent setType(String type) {
        this.type = type;
        return this;
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
