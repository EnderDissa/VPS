package com.mastik.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @GetMapping("/fallback/service1")
    public ResponseEntity<String> service1Fallback() {
        return ResponseEntity.ok("Service 1 is currently unavailable. Please try again later.");
    }

    @GetMapping("/fallback/service2")
    public ResponseEntity<String> service2Fallback() {
        return ResponseEntity.ok("Service 2 is currently unavailable. Please try again later.");
    }
}