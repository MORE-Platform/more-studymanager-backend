package io.redlink.more.studymanager.model.transformer;

import io.redlink.more.studymanager.api.v1.model.EventDTO;
import io.redlink.more.studymanager.api.v1.model.FrequencyDTO;
import io.redlink.more.studymanager.api.v1.model.RecurrenceRuleDTO;
import io.redlink.more.studymanager.api.v1.model.WeekdayDTO;
import io.redlink.more.studymanager.model.Event;
import io.redlink.more.studymanager.model.RRule;

import java.time.ZoneOffset;

public class EventTransformer {
    public static Event fromEventDTO_V1(EventDTO dto) {
        return new Event()
                .setDateStart(dto.getDtstart().toInstant())
                .setDateEnd(dto.getDtend().toInstant())
                .setRRule(fromRecurrenceRuleDTO(dto.getRrule()));
    }

    public static EventDTO toEventDTO_V1(Event event) {
        return new EventDTO()
                .dtstart(event.getDateStart().atOffset(ZoneOffset.UTC))
                .dtend(event.getDateEnd().atOffset(ZoneOffset.UTC))
                .rrule(toRecurrenceRuleDTO(event.getRRule()));
    }

    private static RRule fromRecurrenceRuleDTO(RecurrenceRuleDTO dto) {
        return new RRule()
                .setFreq(dto.getFreq().getValue())
                .setInterval(dto.getInterval())
                .setCount(dto.getCount())
                .setUntil(dto.getUntil().toInstant())
                .setByDay(dto.getByday().getValue())
                .setByMonth(dto.getBymonth())
                .setByMonthDay(dto.getBymonthday())
                .setBySetPos(dto.getBysetpos());
    }

    private static RecurrenceRuleDTO toRecurrenceRuleDTO(RRule rRule) {
        return new RecurrenceRuleDTO()
                .freq(rRule.getFreq() != null ? FrequencyDTO.fromValue(rRule.getFreq()) : null)
                .interval(rRule.getInterval())
                .count(rRule.getCount())
                .until(rRule.getUntil().atOffset(ZoneOffset.UTC))
                .byday(rRule.getByDay() != null ? WeekdayDTO.fromValue(rRule.getByDay()) : null)
                .bymonth(rRule.getByMonth())
                .bymonthday(rRule.getByMonthDay())
                .bysetpos(rRule.getBySetPos());
    }
}
