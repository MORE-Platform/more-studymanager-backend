/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.controller;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/login/init")
public class LoginController {

    @GetMapping
    public ResponseEntity<Void> initAuth(@RequestParam(value = "target", defaultValue = "/") URI target) {
        // Spring-Magic handles authentication, just send the client back where it came from.
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(target)
                .build();
    }

}
