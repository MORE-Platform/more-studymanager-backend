/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class RootController {

    @GetMapping
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("More Studymanager Backend up and running");
    }

}
