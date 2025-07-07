/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.model.StudyTimelineDTO;
import io.redlink.more.studymanager.api.v1.webservices.CalendarApi;
import io.redlink.more.studymanager.controller.RequiresStudyRole;
import io.redlink.more.studymanager.model.StudyRole;
import io.redlink.more.studymanager.model.transformer.TimelineTransformer;
import io.redlink.more.studymanager.properties.GatewayProperties;
import io.redlink.more.studymanager.service.CalendarService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableConfigurationProperties(GatewayProperties.class)
public class CalendarApiV1Controller implements CalendarApi {

    private final CalendarService service;
    private final GatewayProperties properties;

    public CalendarApiV1Controller(CalendarService service, GatewayProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @Override
    public ResponseEntity<String> getStudyCalendar(Long studyId) {
        return ResponseEntity
                .status(301)
                .header("Location", properties.baseUrl() + "/api/v1/calendar/studies/" + studyId + "/calendar.ics")
                .build();
    }

    @Override
    @RequiresStudyRole({StudyRole.STUDY_ADMIN, StudyRole.STUDY_OPERATOR})
    public ResponseEntity<StudyTimelineDTO> getStudyTimeline(Long studyId, Integer participant, Integer studyGroup, Instant referenceDate, LocalDate from, LocalDate to) {
        return ResponseEntity.ok(
                TimelineTransformer.toStudyTimelineDTO(
                        service.getTimeline(studyId, participant, studyGroup, referenceDate, from, to)
                )
        );
    }
}
