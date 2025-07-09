package com.grapheople.controllers;

import agents.multitool.MultiToolAgent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/hello")
    public ResponseEntity<Object> checkSensorDuplicated() {
        MultiToolAgent.initAgent();
        return ResponseEntity.ok("hello world");
    }
}
