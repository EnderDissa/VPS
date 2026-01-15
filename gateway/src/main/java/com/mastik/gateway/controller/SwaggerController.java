package com.mastik.gateway.controller;


import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class SwaggerController {


    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${static.swagger.mappings:}")
    private String staticMappings;

    @GetMapping("/swagger-config.json")
    public Map<String, Object> swaggerConfig() {
        List<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new LinkedList<>();

        addStaticMappings(urls);

        if (!urls.isEmpty())
            return Map.of("urls", urls);

        discoveryClient.getServices().forEach(serviceName ->
                discoveryClient.getInstances(serviceName).forEach(serviceInstance ->
                        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("default", serviceInstance.getUri() + "/v3/api-docs", serviceName))
                )
        );

        return new java.util.HashMap<>(Map.of("urls", urls));
    }

    private void addStaticMappings(List<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls) {
        if (staticMappings == null || staticMappings.trim().isEmpty()) {
            return;
        }

        Arrays.stream(staticMappings.split(","))
                .map(String::trim)
                .filter(mapping -> mapping.contains("="))
                .forEach(mapping -> {
                    String[] parts = mapping.split("=", 2);
                    if (parts.length == 2) {
                        String serviceName = parts[0].trim();
                        String url = parts[1].trim();
                        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("default", url, serviceName));
                    }
                });
    }
}