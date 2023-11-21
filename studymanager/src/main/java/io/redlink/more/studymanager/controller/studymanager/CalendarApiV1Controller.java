package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.api.v1.webservices.CalendarApi;
import io.redlink.more.studymanager.properties.GatewayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableConfigurationProperties(GatewayProperties.class)
public class CalendarApiV1Controller implements CalendarApi {

    private final GatewayProperties properties;

    public CalendarApiV1Controller(GatewayProperties properties) {
        this.properties = properties;
    }

    @Override
    public ResponseEntity<String> getStudyCalendar(Long studyId) {
        return ResponseEntity
                .status(301)
                .header("Location", properties.getBaseUrl() + "/api/v1/calendar/studies/" + studyId + "/calendar.ics")
                .build();
    }
}
