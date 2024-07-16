package io.redlink.more.studymanager.controller.studymanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.more.studymanager.model.AuthenticatedUser;
import io.redlink.more.studymanager.model.Observation;
import io.redlink.more.studymanager.model.PlatformRole;
import io.redlink.more.studymanager.model.scheduler.Event;
import io.redlink.more.studymanager.model.scheduler.RelativeEvent;
import io.redlink.more.studymanager.model.timeline.ObservationTimelineEvent;
import io.redlink.more.studymanager.model.timeline.StudyTimeline;
import io.redlink.more.studymanager.service.CalendarService;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(CalendarApiV1Controller.class)
@AutoConfigureMockMvc(addFilters = false)
public class CalendarControllerTest {
    @MockBean
    CalendarService service;

    @MockBean
    OAuth2AuthenticationService authService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mvc;

    @BeforeEach
    void setup() {
        when(authService.getCurrentUser()).thenReturn(
                new AuthenticatedUser(
                        UUID.randomUUID().toString(),
                        "Testname", "Test@mail", "Test Institution",
                        EnumSet.allOf(PlatformRole.class)
                )
        );
    }

    @Test
    @DisplayName("getStudyTimeline should return the timeline of a study's observations")
    void testGetStudyTimeline() throws Exception {
        Integer studyGroup1 = null;
        Integer studyGroup2 = 1;
        Instant referenceDate = Instant.now();
        LocalDate from = LocalDate.of(2024, 2, 1);
        LocalDate to = LocalDate.of(2024, 5, 1);

        when(service.getTimeline(any(), any(), any(), any(Instant.class), any(LocalDate.class), any(LocalDate.class)))
                .thenAnswer(invocationOnMock -> {
                    return new StudyTimeline(
                            referenceDate,
                            Range.between(from, to, LocalDate::compareTo),
                            List.of(
                                    ObservationTimelineEvent.fromObservation(
                                            new Observation()
                                                    .setObservationId(1)
                                                    .setStudyId(invocationOnMock.getArgument(0))
                                                    .setStudyGroupId(studyGroup1)
                                                    .setTitle("title 1")
                                                    .setPurpose("purpose 1")
                                                    .setType("type 1")
                                                    .setHidden(Boolean.FALSE)
                                                    .setSchedule(new Event()),
                                            ((LocalDate)invocationOnMock.getArgument(4)).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                                            ((LocalDate)invocationOnMock.getArgument(5)).atStartOfDay(ZoneId.systemDefault()).toInstant()
                                    ),
                                    ObservationTimelineEvent.fromObservation(
                                            new Observation()
                                                    .setObservationId(2)
                                                    .setStudyId(invocationOnMock.getArgument(0))
                                                    .setStudyGroupId(studyGroup2)
                                                    .setTitle("title 2")
                                                    .setPurpose("purpose 2")
                                                    .setType("type 2")
                                                    .setHidden(Boolean.TRUE)
                                                    .setSchedule(new RelativeEvent()),
                                            ((LocalDate)invocationOnMock.getArgument(4)).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                                            ((LocalDate)invocationOnMock.getArgument(5)).atStartOfDay(ZoneId.systemDefault()).toInstant()
                                    )
                            ),
                            List.of()
                    );
                });

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX").withZone(ZoneId.systemDefault());

        mvc.perform(get("/api/v1/studies/3/timeline")
                        .param("participant", String.valueOf(2))
                        .param("referenceDate", referenceDate.toString())
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.observations.length()").value(2))
                .andExpect(jsonPath("$.observations[0].observationId").value(1))
                .andExpect(jsonPath("$.observations[0].studyGroupId").value(studyGroup1))
                .andExpect(jsonPath("$.observations[0].title").value("title 1"))
                .andExpect(jsonPath("$.observations[0].purpose").value("purpose 1"))
                .andExpect(jsonPath("$.observations[0].type").value("type 1"))
                .andExpect(jsonPath("$.observations[0].start").exists())
                .andExpect(jsonPath("$.observations[0].end").exists())
                .andExpect(jsonPath("$.observations[0].hidden").value(Boolean.FALSE))
                .andExpect(jsonPath("$.observations[0].scheduleType").value(Event.TYPE))

                .andExpect(jsonPath("$.observations[1].observationId").value(2))
                .andExpect(jsonPath("$.observations[1].studyGroupId").value(studyGroup2))
                .andExpect(jsonPath("$.observations[1].title").value("title 2"))
                .andExpect(jsonPath("$.observations[1].purpose").value("purpose 2"))
                .andExpect(jsonPath("$.observations[1].type").value("type 2"))
                .andExpect(jsonPath("$.observations[1].start").exists())
                .andExpect(jsonPath("$.observations[1].end").exists())
                .andExpect(jsonPath("$.observations[1].hidden").value(Boolean.TRUE))
                .andExpect(jsonPath("$.observations[1].scheduleType").value(RelativeEvent.TYPE));

    }
}
