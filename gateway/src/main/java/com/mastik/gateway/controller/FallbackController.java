package com.mastik.gateway.controller;

import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class FallbackController {

    @GetMapping("/fallback/{service}")
    public ResponseEntity<String> serviceFallback(@PathVariable String service) {
        return new ResponseEntity<String>("{\"message\": \"" + service + " is currently unavailable. Please try again later.\"}", HttpStatusCode.valueOf(503));
    }

}