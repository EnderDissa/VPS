package com.mastik.gateway.controller;


import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class SwaggerController {


    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/swagger-config.json")
    public Map<String, Object> swaggerConfig() {
        List<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new LinkedList<>();
        discoveryClient.getServices().forEach(serviceName ->
                discoveryClient.getInstances(serviceName).forEach(serviceInstance ->
                        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("default", serviceInstance.getUri() + "/v3/api-docs", serviceName))
                )
        );
        Map<String, Object> resMap = new java.util.HashMap<>(Map.of("urls", urls));
        return resMap;
    }
}