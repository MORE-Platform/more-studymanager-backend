/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.DurationDTO;
import io.redlink.more.studymanager.api.v1.model.EventDTO;
import io.redlink.more.studymanager.api.v1.model.FrequencyDTO;
import io.redlink.more.studymanager.api.v1.model.ObservationScheduleDTO;
import io.redlink.more.studymanager.api.v1.model.RecurrenceRuleDTO;
import io.redlink.more.studymanager.api.v1.model.RelativeDateDTO;
import io.redlink.more.studymanager.api.v1.model.RelativeEventDTO;
import io.redlink.more.studymanager.api.v1.model.RelativeRecurrenceRuleDTO;
import io.redlink.more.studymanager.api.v1.model.WeekdayDTO;
import io.redlink.more.studymanager.model.scheduler.Duration;
import io.redlink.more.studymanager.model.scheduler.Event;
import io.redlink.more.studymanager.model.scheduler.RecurrenceRule;
import io.redlink.more.studymanager.model.scheduler.RelativeDate;
import io.redlink.more.studymanager.model.scheduler.RelativeEvent;
import io.redlink.more.studymanager.model.scheduler.RelativeRecurrenceRule;
import io.redlink.more.studymanager.model.scheduler.ScheduleEvent;
import java.time.Instant;

public final class EventTransformer {

    private EventTransformer() {
    }

    public static ScheduleEvent fromObservationScheduleDTO_V1(ObservationScheduleDTO genericDto) {
        if (genericDto != null) {
            if(genericDto.getType() == null || Event.TYPE.equals(genericDto.getType())) {
                EventDTO dto = (EventDTO) genericDto;
                Instant offsetDateTime = dto.getDtend();
                Instant offsetDateTime1 = dto.getDtstart();
                return new Event()
                        .setDateStart(offsetDateTime1)
                        .setDateEnd(offsetDateTime)
                        .setRRule(fromRecurrenceRuleDTO(dto.getRrule()));
            } else if(RelativeEvent.TYPE.equals(genericDto.getType())) {
                RelativeEventDTO dto = (RelativeEventDTO) genericDto;
                return new RelativeEvent()
                        .setDtstart(new RelativeDate()
                                .setOffset(fromDurationDTO(dto.getDtstart().getOffset()))
                                .setTime(dto.getDtstart().getTime()))
                        .setDtend(new RelativeDate()
                                .setOffset(fromDurationDTO(dto.getDtend().getOffset()))
                                .setTime(dto.getDtend().getTime()))
                        .setRrrule(fromRelativeRecurrenceRuleDTO(dto.getRrrule()));

            } else {
                throw new IllegalArgumentException("Unknown Event Type: " + genericDto.getType());
            }
        }
        else return null;
    }

    public static ObservationScheduleDTO toObservationScheduleDTO_V1(ScheduleEvent event) {
        if (event != null)
            if(event.getType() == null || Event.TYPE.equals(event.getType())) {
                Event e = (Event) event;
                Instant instant = e.getDateEnd();
                Instant instant1 = e.getDateStart();
                return new EventDTO()
                        .type(Event.TYPE)
                        .dtstart(instant1)
                        .dtend(instant)
                        .rrule(toRecurrenceRuleDTO(e.getRRule()));
            } else if(RelativeEvent.TYPE.equals(event.getType())) {
                RelativeEvent e = (RelativeEvent) event;
                return new RelativeEventDTO()
                        .type(RelativeEvent.TYPE)
                        .dtstart(new RelativeDateDTO()
                                .offset(toDurationDTO(e.getDtstart().getOffset()))
                                .time(e.getDtstart().getTime()))
                        .dtend(new RelativeDateDTO()
                                .offset(toDurationDTO(e.getDtend().getOffset()))
                                .time(e.getDtend().getTime()))
                        .rrrule(toRelativeRecurrenceRuleDTO(e.getRrrule()));
            } else {
                throw new IllegalArgumentException("Unknown Event Type: " + event.getType());
            }
        else return null;
    }

    private static RecurrenceRule fromRecurrenceRuleDTO(RecurrenceRuleDTO dto) {
        if (dto != null) {
            Instant offsetDateTime = dto.getUntil();
            return new RecurrenceRule()
                    .setFreq(dto.getFreq().getValue())
                    .setInterval(dto.getInterval())
                    .setCount(dto.getCount())
                    .setUntil(offsetDateTime)
                    .setByDay(dto.getByday() != null ? dto.getByday().stream().map(WeekdayDTO::getValue).toList() : null)
                    .setByMonth(dto.getBymonth())
                    .setByMonthDay(dto.getBymonthday())
                    .setBySetPos(dto.getBysetpos());
        }
        else return null;
    }

    private static RecurrenceRuleDTO toRecurrenceRuleDTO(RecurrenceRule recurrenceRule) {
        if (recurrenceRule != null) {
            Instant instant = recurrenceRule.getUntil();
            return new RecurrenceRuleDTO()
                    .freq(recurrenceRule.getFreq() != null ? FrequencyDTO.fromValue(recurrenceRule.getFreq()) : null)
                    .interval(recurrenceRule.getInterval())
                    .count(recurrenceRule.getCount())
                    .until(instant)
                    .byday(recurrenceRule.getByDay() != null ? recurrenceRule.getByDay().stream().map(WeekdayDTO::fromValue).toList() : null)
                    .bymonth(recurrenceRule.getByMonth())
                    .bymonthday(recurrenceRule.getByMonthDay())
                    .bysetpos(recurrenceRule.getBySetPos());
        }
        else return null;
    }

    private static RelativeRecurrenceRuleDTO toRelativeRecurrenceRuleDTO(RelativeRecurrenceRule relativeRecurrenceRule) {
        if (relativeRecurrenceRule != null)
            return new RelativeRecurrenceRuleDTO()
                    .frequency(toDurationDTO(relativeRecurrenceRule.getFrequency()))
                    .endAfter(toDurationDTO(relativeRecurrenceRule.getEndAfter()));
        else return null;
    }

    private static RelativeRecurrenceRule fromRelativeRecurrenceRuleDTO(RelativeRecurrenceRuleDTO dto) {
        if (dto != null)
            return new RelativeRecurrenceRule()
                    .setFrequency(fromDurationDTO(dto.getFrequency()))
                    .setEndAfter(fromDurationDTO(dto.getEndAfter()));
        else return null;
    }

    private static Duration fromDurationDTO(DurationDTO dto) {
        if (dto != null)
            return new Duration()
                    .setValue(dto.getValue())
                    .setUnit(dto.getUnit() != null ? Duration.Unit.fromDurationDTOUnit(dto.getUnit()) : null);
        else return null;
    }

    private static DurationDTO toDurationDTO(Duration duration) {
        if (duration != null)
            return new DurationDTO()
                    .value(duration.getValue())
                    .unit(duration.getUnit() != null ? Duration.Unit.toDurationDTOUnit(duration.getUnit()) : null);
        else return null;
    }
}
