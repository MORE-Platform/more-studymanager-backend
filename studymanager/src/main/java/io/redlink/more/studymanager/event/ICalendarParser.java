package io.redlink.more.studymanager.event;

import biweekly.component.VEvent;
import biweekly.util.DayOfWeek;
import biweekly.util.Frequency;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;
import io.redlink.more.studymanager.model.Event;
import io.redlink.more.studymanager.model.RecurrenceRule;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class ICalendarParser {

    public static List<Instant> parseToTimeRange(Event event) {
        List<Instant> dates = new ArrayList<>();
        VEvent iCalEvent = parseToICalEvent(event);
        long eventDuration = getEventTime(event);
        DateIterator it = iCalEvent.getDateIterator(TimeZone.getDefault());
        while (it.hasNext()) {
            Instant start = it.next().toInstant();
            Instant end = start.plus(eventDuration, ChronoUnit.SECONDS);
            dates.add(start);
            dates.add(end);
        }
        return dates;
    }

    private static long getEventTime(Event event) {
        return Duration.between(event.getDateStart(), event.getDateEnd()).getSeconds();
    }

    private static VEvent parseToICalEvent(Event event) {
        VEvent iCalEvent = new VEvent();
        iCalEvent.setDateStart(Date.from(event.getDateStart()));
        iCalEvent.setDateEnd(Date.from(event.getDateEnd()));

        RecurrenceRule eventRecurrence = event.getRRule();
        if (event.getRRule() != null) {
            Recurrence.Builder recurBuilder = new Recurrence.Builder(Frequency.valueOf(eventRecurrence.getFreq()));
            setUntil(recurBuilder, eventRecurrence.getUntil());
            setCount(recurBuilder, eventRecurrence.getCount());
            setInterval(recurBuilder, eventRecurrence.getInterval());
            setByDay(recurBuilder, eventRecurrence.getByDay());
            setByMonth(recurBuilder, eventRecurrence.getByMonth());
            setByMonthDay(recurBuilder, eventRecurrence.getByMonthDay());
            setBySetPos(recurBuilder, eventRecurrence.getBySetPos());

            iCalEvent.setRecurrenceRule(new biweekly.property.RecurrenceRule(recurBuilder.build()));
        }
        return iCalEvent;
    }

    private static void setUntil(Recurrence.Builder builder, Instant until) {
        if(until != null) builder.until(Date.from(until));
    }

    private static void setCount(Recurrence.Builder builder, Integer count) {
        if(count != null) builder.count(count);
    }

    private static void setInterval(Recurrence.Builder builder, Integer interval) {
        if(interval != null) builder.interval(interval);
    }

    private static void setByDay(Recurrence.Builder builder, List<String> byDay) {
        if(byDay != null) builder.byDay(byDay.stream().map(DayOfWeek::valueOfAbbr).toList());
    }

    private static void setByMonth(Recurrence.Builder builder, Integer byMonth) {
        if(byMonth != null) builder.byMonth(byMonth);
    }

    private static void setByMonthDay(Recurrence.Builder builder, Integer byMonthDay) {
        if(byMonthDay != null) builder.byMonthDay(byMonthDay);
    }

    private static void setBySetPos(Recurrence.Builder builder, Integer bySetPos) {
        if(bySetPos != null) builder.bySetPos(bySetPos);
    }

}
