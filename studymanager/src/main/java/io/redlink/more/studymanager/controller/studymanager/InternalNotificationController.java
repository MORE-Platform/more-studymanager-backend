/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.controller.studymanager;

import io.redlink.more.studymanager.service.PushNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Internal endpoint for Docker-to-Docker notification sending.
 * Protected by a static API key configured in application.yaml.
 */
@RestController
@RequestMapping(value = "/api/v1/internal", produces = MediaType.APPLICATION_JSON_VALUE)
public class InternalNotificationController {

    private final PushNotificationService pushNotificationService;
    private final String apiKey;

    public InternalNotificationController(
            PushNotificationService pushNotificationService,
            @Value("${more.internal.api-key}") String apiKey) {
        this.pushNotificationService = pushNotificationService;
        this.apiKey = apiKey;
    }

    record NotificationRequest(Long studyId, Integer participantId, String title, String message) {}

    @PostMapping("/notifications")
    public ResponseEntity<Map<String, Object>> sendNotification(
            @RequestHeader(value = "X-Api-Key", required = false) String providedKey,
            @RequestBody NotificationRequest request) {

        if (!apiKey.equals(providedKey)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        if (request.studyId() == null || request.participantId() == null
                || request.title() == null || request.message() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "studyId, participantId, title and message are required"));
        }

        boolean sent = pushNotificationService.sendPushNotification(
                request.studyId(),
                request.participantId(),
                request.title(),
                request.message(),
                Map.of()
        );

        if (sent) {
            return ResponseEntity.ok(Map.of("status", "sent"));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "No FCM token found for participant"));
        }
    }
}
