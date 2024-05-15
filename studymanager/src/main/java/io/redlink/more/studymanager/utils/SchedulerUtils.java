package io.redlink.more.studymanager.utils;

import biweekly.component.VEvent;
import biweekly.util.DayOfWeek;
import biweekly.util.Frequency;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.Trigger;
import io.redlink.more.studymanager.model.scheduler.*;
import java.time.LocalDate;
import org.apache.commons.lang3.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public final class SchedulerUtils {

    public static List<Range<Instant>> parseToObservationSchedulesForRelativeEvent(
            RelativeEvent event, Instant start, Instant maxEnd) {

        final List<Range<Instant>> events = new ArrayList<>();

        Range<Instant> currentEvt = Range.between(
                toInstantFrom(event.getDtstart(), start),
                toInstantFrom(event.getDtend(), start)
        );

        if (event.getRrrule() != null) {
            RelativeRecurrenceRule rrule = event.getRrrule();
            Instant maxEndOfRule = currentEvt.getMaximum().plus(rrule.getEndAfter().getValue(), rrule.getEndAfter().getUnit().toChronoUnit());
            maxEnd = maxEnd.isBefore(maxEndOfRule) ? maxEnd : maxEndOfRule;
            long durationInMs = currentEvt.getMaximum().toEpochMilli() - currentEvt.getMinimum().toEpochMilli();

            while (currentEvt.getMaximum().isBefore(maxEnd)) {
                events.add(currentEvt);
                Instant estart = currentEvt.getMinimum().plus(rrule.getFrequency().getValue(), rrule.getFrequency().getUnit().toChronoUnit());
                currentEvt = Range.between(estart, estart.plusMillis(durationInMs));
            }
        } else {
            events.add(currentEvt);
        }

        return List.copyOf(events);
    }

    private static Instant toInstantFrom(RelativeDate date, Instant start) {
        return start.atZone(ZoneId.systemDefault())
                // FIXME: Hidden Offset-Correction
                // Offset is 1-based, therefor we must "-1" here
                // (fist day: 1, second day: 2, ... )
                .plus(date.getOffset().getValue() - 1, date.getOffset().getUnit().toChronoUnit())
                .with(date.getTime())
                .toInstant();
    }

    public static List<Range<Instant>> parseToObservationSchedulesForEvent(Event event, Instant start, Instant end) {
        List<Range<Instant>> observationSchedules = new ArrayList<>();
        if (event.getDateStart() != null && event.getDateEnd() != null) {
            VEvent iCalEvent = parseToICalEvent(event);
            long eventDuration = getEventTime(event);
            DateIterator it = iCalEvent.getDateIterator(TimeZone.getDefault());
            while (it.hasNext()) {
                Instant ostart = it.next().toInstant();
                Instant oend = ostart.plus(eventDuration, ChronoUnit.SECONDS);
                if (ostart.isBefore(end) && oend.isAfter(start)) {
                    observationSchedules.add(Range.between(ostart, oend));
                }
            }
        }
        // TODO edge cases if calculated days are not consecutive (e.g. first weekend -> first of month is a sunday)
        return List.copyOf(observationSchedules);
    }

    public static List<Range<Instant>> parseToObservationSchedules(ScheduleEvent scheduleEvent, Instant start, Instant end) {
        if (scheduleEvent == null) return Collections.emptyList();
        if (scheduleEvent instanceof Event event) {
            return parseToObservationSchedulesForEvent(event, start, end);
        } else if (scheduleEvent instanceof RelativeEvent relativeEvent) {
            return parseToObservationSchedulesForRelativeEvent(relativeEvent, start, end);
        } else {
            return Collections.emptyList();
        }
    }

    public static LocalDate alignStartDateToSignupInstant(final Instant signup, List<Observation> observations) {
        return LocalDate.ofInstant(observations.stream()
                // All the observation-schedules
                .map(Observation::getSchedule)
                // ... the relative ones
                .filter(e -> RelativeEvent.TYPE.equals(e.getType()))
                .map(e -> (RelativeEvent) e)
                // all the relative schedules END
                .map(RelativeEvent::getDtend)
                // Get the EARLIEST relative schedule end
                .min(Comparator.comparing(RelativeDate::getOffset, io.redlink.more.studymanager.model.scheduler.Duration.DURATION_COMPARATOR))
                // Convert the relative schedule end to the actual instant
                .map(rd -> LocalDate.ofInstant(
                                toInstantFrom(rd, signup), ZoneId.systemDefault()
                        )
                        .atTime(rd.getTime())
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                )
                // has the first relative schedule end already passed?
                .filter(end -> end.isBefore(signup))
                // then we start "tomorrow"
                .map(i -> signup.atZone(ZoneId.systemDefault()).plusDays(1).toInstant())
                // otherwise we start "now"
                .orElse(signup),
            ZoneId.systemDefault()
        );
    }

    public static List<Instant> parseToInterventionSchedules(Trigger trigger, Instant start, Instant end) {
        if(trigger == null) return Collections.emptyList();
        if(Objects.equals(trigger.getType(), "relative-time-trigger")) {
            return parseToInterventionSchedulesForRelativeTrigger(trigger, start);
        } else if(Objects.equals(trigger.getType(), "scheduled-trigger")) {
            return parseToInterventionSchedulesForScheduledTrigger(trigger, start, end);
        } else {
            return Collections.emptyList();
        }
    }

    private static List<Instant> parseToInterventionSchedulesForRelativeTrigger(Trigger trigger, Instant start) {
        List<Instant> events = new ArrayList<>();
        Instant currentEvent = toInstant(
                new RelativeDate()
                        .setTime(trigger.getProperties().getString("hour") + ":00")
                        .setOffset(new io.redlink.more.studymanager.model.scheduler.Duration().setValue(trigger.getProperties().getInt("day")).setUnit(io.redlink.more.studymanager.model.scheduler.Duration.Unit.DAY)),
                start);
        events.add(currentEvent);
        return events;
    }

    private static List<Instant> parseToInterventionSchedulesForScheduledTrigger(Trigger trigger, Instant start, Instant end) {
        List<Instant> events = new ArrayList<>();
        //TODO parse cron schedule
        return events;
    }

    public static Instant shiftStartIfObservationAlreadyEnded(Instant start, List<Observation> observations) {
        // returns start date, if now event ends before, otherwise start date + 1 day
        return observations.stream()
                .map(Observation::getSchedule)
                .filter(scheduleEvent -> scheduleEvent.getType().equals(RelativeEvent.TYPE))
                .map(r -> ((RelativeEvent) r).getDtend())
                .filter(relativeDate -> relativeDate.getOffset().getValue() == 1)
                .map(relativeDate -> start.atZone(ZoneId.systemDefault()).withHour(relativeDate.getHours()).withMinute(relativeDate.getMinutes()).withSecond(0).withNano(0).toInstant())
                .filter(instant -> instant.isBefore(start))
                .map(instant -> start.atZone(ZoneId.systemDefault()).withHour(0).withMinute(0).plusDays(1).toInstant())
                .findFirst()
                .orElse(start);
    }

    private static long getEventTime(Event event) {
        return java.time.Duration.between(event.getDateStart(), event.getDateEnd()).getSeconds();
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
            setByDay(recurBuilder, eventRecurrence.getByDay(), eventRecurrence.getBySetPos());
            setByHour(recurBuilder, eventRecurrence.getFreq(), event.getDateStart().atZone(TimeZone.getDefault().toZoneId()).getHour());
            setByMinute(recurBuilder, event.getDateStart().atZone(TimeZone.getDefault().toZoneId()).getMinute());
            setByMonth(recurBuilder, eventRecurrence.getByMonth());
            setByMonthDay(recurBuilder, eventRecurrence.getByMonthDay());

            iCalEvent.setRecurrenceRule(new biweekly.property.RecurrenceRule(recurBuilder.build()));
        }
        return iCalEvent;
    }

    private static void setByMinute(Recurrence.Builder builder, Integer minute) {
        if (minute != null) builder.byMinute(minute);
    }

    private static void setByHour(Recurrence.Builder builder, String freq, Integer hour) {
        if (hour != null && !Objects.equals(freq, "HOURLY")) builder.byHour(hour);
    }

    private static void setUntil(Recurrence.Builder builder, Instant until) {
        if (until != null) builder.until(Date.from(until));
    }

    private static void setCount(Recurrence.Builder builder, Integer count) {
        if (count != null) builder.count(count);
    }

    private static void setInterval(Recurrence.Builder builder, Integer interval) {
        if (interval != null) builder.interval(interval);
    }

    private static void setByDay(Recurrence.Builder builder, List<String> byDay, Integer bySetPos) {
        if (byDay != null && bySetPos == null)
            builder.byDay(byDay.stream().map(DayOfWeek::valueOfAbbr).toList());
        if (byDay != null && bySetPos != null)
            byDay.forEach(day -> builder.byDay(bySetPos, DayOfWeek.valueOfAbbr(day)));

    }

    private static void setByMonth(Recurrence.Builder builder, Integer byMonth) {
        if (byMonth != null) builder.byMonth(byMonth);
    }

    private static void setByMonthDay(Recurrence.Builder builder, Integer byMonthDay) {
        if (byMonthDay != null) builder.byMonthDay(byMonthDay);
    }
}
