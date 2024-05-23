package io.redlink.more.studymanager.transformer;

import io.redlink.more.studymanager.api.v1.model.EventDTO;
import io.redlink.more.studymanager.api.v1.model.ObservationScheduleDTO;
import io.redlink.more.studymanager.api.v1.model.RelativeEventDTO;
import io.redlink.more.studymanager.model.scheduler.*;
import io.redlink.more.studymanager.model.transformer.EventTransformer;
import io.redlink.more.studymanager.utils.MapperUtils;
import java.time.LocalTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ScheduleEventTransformerTest {

    @Test
    public void testJsonToEventTransformer() {
        String jsonEvent = "{\"rrule\": null, \"dateEnd\": 1683755999.000000000, \"dateStart\": 1683669600.000000000}";

        String jsonRelativeEvent1 = """
{
   "type":"RelativeEvent",
   "dtstart":{
      "offset":{
         "value":1,
         "unit":"DAY"
      },
      "time":"12:00:00"
   },
   "dtend":{
      "offset":{
         "value":2,
         "unit":"DAY"
      },
      "time":"12:00:00"
   }
}
                """;

        String jsonRelativeEvent2 = """
{
   "type":"RelativeEvent",
   "dtstart":{
      "offset":{
         "value":1,
         "unit":"DAY"
      },
      "time":"12:00:00"
   },
   "dtend":{
      "offset":{
         "value":2,
         "unit":"DAY"
      },
      "time":"12:00:00"
   },
   "rrrule":{
      "frequency":{
         "unit":"DAY",
         "value":1
      },
      "endAfter":{
         "unit":"DAY",
         "value":10
      }
   }
}
                """;

        ScheduleEvent event = MapperUtils.readValue(jsonEvent, ScheduleEvent.class);
        Assertions.assertInstanceOf(Event.class, event);

        ScheduleEvent eventRelative1 = MapperUtils.readValue(jsonRelativeEvent1, ScheduleEvent.class);
        Assertions.assertInstanceOf(RelativeEvent.class, eventRelative1);

        ScheduleEvent eventRelative2 = MapperUtils.readValue(jsonRelativeEvent2, ScheduleEvent.class);
        Assertions.assertInstanceOf(RelativeEvent.class, eventRelative2);
    }

    @Test
    public void testDTOTransformer() {
        Event event = new Event()
                .setDateStart(Instant.now().plus(1, ChronoUnit.DAYS))
                .setDateEnd(Instant.now().plus(2, ChronoUnit.DAYS));

        RelativeEvent relativeEvent = new RelativeEvent()
                .setDtstart(new RelativeDate()
                        .setTime(LocalTime.parse("12:00:00"))
                        .setOffset(new Duration().setUnit(Duration.Unit.DAY).setValue(3)))
                .setDtend(new RelativeDate()
                        .setTime(LocalTime.parse("12:00:00"))
                        .setOffset(new Duration().setUnit(Duration.Unit.DAY).setValue(4)))
                .setRrrule(new RelativeRecurrenceRule()
                        .setFrequency(new Duration().setUnit(Duration.Unit.DAY).setValue(1))
                        .setEndAfter(new Duration().setUnit(Duration.Unit.DAY).setValue(10)));

        ObservationScheduleDTO eventDTO = EventTransformer.toObservationScheduleDTO_V1(event);
        ObservationScheduleDTO relativeEventDTO = EventTransformer.toObservationScheduleDTO_V1(relativeEvent);

        Assertions.assertInstanceOf(EventDTO.class, eventDTO);
        Assertions.assertInstanceOf(RelativeEventDTO.class, relativeEventDTO);

    }
}
