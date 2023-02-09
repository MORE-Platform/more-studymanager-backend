package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.EventDTO;
import io.redlink.more.studymanager.api.v1.model.FrequencyDTO;
import io.redlink.more.studymanager.api.v1.model.RecurrenceRuleDTO;
import io.redlink.more.studymanager.api.v1.model.WeekdayDTO;
import io.redlink.more.studymanager.model.Event;
import io.redlink.more.studymanager.model.RecurrenceRule;

public final class EventTransformer {

    private EventTransformer() {
    }

    public static Event fromEventDTO_V1(EventDTO dto) {
        if (dto != null)
            return new Event()
                    .setDateStart(Transformers.toInstant(dto.getDtstart()))
                    .setDateEnd(Transformers.toInstant(dto.getDtend()))
                    .setRRule(fromRecurrenceRuleDTO(dto.getRrule()));
        else return null;
    }

    public static EventDTO toEventDTO_V1(Event event) {
        if (event != null)
            return new EventDTO()
                    .dtstart(Transformers.toOffsetDateTime(event.getDateStart()))
                    .dtend(Transformers.toOffsetDateTime(event.getDateEnd()))
                    .rrule(toRecurrenceRuleDTO(event.getRRule()));
        else return null;
    }

    private static RecurrenceRule fromRecurrenceRuleDTO(RecurrenceRuleDTO dto) {
        if (dto != null)
            return new RecurrenceRule()
                    .setFreq(dto.getFreq().getValue())
                    .setInterval(dto.getInterval())
                    .setCount(dto.getCount())
                    .setUntil(Transformers.toInstant(dto.getUntil()))
                    .setByDay(dto.getByday() != null ? dto.getByday().stream().map(WeekdayDTO::getValue).toList() : null)
                    .setByMonth(dto.getBymonth())
                    .setByMonthDay(dto.getBymonthday())
                    .setBySetPos(dto.getBysetpos());
        else return null;
    }

    private static RecurrenceRuleDTO toRecurrenceRuleDTO(RecurrenceRule recurrenceRule) {
        if (recurrenceRule != null)
            return new RecurrenceRuleDTO()
                    .freq(recurrenceRule.getFreq() != null ? FrequencyDTO.fromValue(recurrenceRule.getFreq()) : null)
                    .interval(recurrenceRule.getInterval())
                    .count(recurrenceRule.getCount())
                    .until(Transformers.toOffsetDateTime(recurrenceRule.getUntil()))
                    .byday(recurrenceRule.getByDay() != null ? recurrenceRule.getByDay().stream().map(WeekdayDTO::fromValue).toList() : null)
                    .bymonth(recurrenceRule.getByMonth())
                    .bymonthday(recurrenceRule.getByMonthDay())
                    .bysetpos(recurrenceRule.getBySetPos());
        else return null;
    }
}
