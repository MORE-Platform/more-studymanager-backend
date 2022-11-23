package io.redlink.more.studymanager.event;

import biweekly.component.VEvent;
import biweekly.property.RecurrenceRule;
import biweekly.util.DayOfWeek;
import biweekly.util.Frequency;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;
import io.redlink.more.studymanager.model.Event;
import io.redlink.more.studymanager.model.RRule;

import java.sql.Date;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class ICalendarParser {

    public static List<Instant> parseToTimeRange(Event event) {
        List<Instant> startDates = new ArrayList<>();
        VEvent iCalEvent = parseToICalEvent(event);
        try {
            DateIterator it = iCalEvent.getDateIterator(TimeZone.getDefault());
            while (it.hasNext()) {
                startDates.add(it.next().toInstant());
            }

            return startDates;
        } catch (AssertionError e) {
            throw new RuntimeException();
        }
    }

    private static VEvent parseToICalEvent(Event event) {
        VEvent iCalEvent = new VEvent();
        iCalEvent.setDateStart(Date.from(event.getDateStart()));
        iCalEvent.setDateEnd(Date.from(event.getDateEnd()));

        RRule eventRecurrence = event.getRRule();
        if(event.getRRule() != null) {
            Recurrence recur = new Recurrence.Builder(Frequency.valueOf(eventRecurrence.getFreq()))
                    .until(Date.from(eventRecurrence.getUntil()))
                    .count(eventRecurrence.getCount())
                    .interval(eventRecurrence.getInterval())
                    .byDay(DayOfWeek.valueOfAbbr(eventRecurrence.getByDay()))
                    .byMonth(eventRecurrence.getByMonth())
                    .byMonthDay(eventRecurrence.getByMonthDay())
                    .bySetPos(eventRecurrence.getBySetPos())
                    .build();
            iCalEvent.setRecurrenceRule(new RecurrenceRule(recur));
        }
        return iCalEvent;
    }

}
