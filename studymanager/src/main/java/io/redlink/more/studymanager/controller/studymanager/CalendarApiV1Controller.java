package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.StudyTimelineDTO;
import io.redlink.more.studymanager.api.v1.webservices.CalendarApi;
import io.redlink.more.studymanager.model.transformer.TimelineTransformer;
import io.redlink.more.studymanager.properties.GatewayProperties;
import io.redlink.more.studymanager.service.CalendarService;
import io.redlink.more.studymanager.service.OAuth2AuthenticationService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableConfigurationProperties(GatewayProperties.class)
public class CalendarApiV1Controller implements CalendarApi {

    private final CalendarService service;
    OAuth2AuthenticationService auth2AuthenticationService;
    private final GatewayProperties properties;

    public CalendarApiV1Controller(CalendarService service, OAuth2AuthenticationService auth2AuthenticationService, GatewayProperties properties) {
        this.service = service;
        this.auth2AuthenticationService = auth2AuthenticationService;
        this.properties = properties;
    }

    @Override
    public ResponseEntity<String> getStudyCalendar(Long studyId) {
        return ResponseEntity
                .status(301)
                .header("Location", properties.getBaseUrl() + "/api/v1/calendar/studies/" + studyId + "/calendar.ics")
                .build();
    }

    @Override
    public ResponseEntity<StudyTimelineDTO> getStudyTimeline(Long studyId, Integer participant, Integer studyGroup, OffsetDateTime referenceDate, LocalDate from, LocalDate to) {
        final var currentUser = auth2AuthenticationService.getCurrentUser();
        return ResponseEntity.ok(
                TimelineTransformer.toStudyTimelineDTO(
                        service.getTimeline(studyId, participant, studyGroup, referenceDate, from, to, currentUser)
                )
        );
    }
}
