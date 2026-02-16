package com.mastik.gateway;

import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;

import reactor.core.publisher.Mono;

@Configuration
public class OpenApiRewriteConfig {

    private final ObjectMapper objectMapper;

    public OpenApiRewriteConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public RouteLocator openApiRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
//                 Add to existing routes
                .route("itemService", r -> r.path("/api/itemService/**")
                        .filters(f -> f
                                .circuitBreaker(c -> {
                                    c.setName("defaultCircuitBreaker");
                                    c.setFallbackUri("forward:/fallback/itemService");
                                })
                                .modifyResponseBody(String.class, String.class, rewriteOpenApiBody("/api/itemService"))
                                .stripPrefix(2))
                        .uri("lb://ITEMSERVICE"))
                .route("storageService", r -> r.path("/api/storageService/**")
                        .filters(f -> f
                                .circuitBreaker(c -> {
                                    c.setName("defaultCircuitBreaker");
                                    c.setFallbackUri("forward:/fallback/storageService");
                                })
                                .modifyResponseBody(String.class, String.class, rewriteOpenApiBody("/api/storageService"))
                                .stripPrefix(2))
                        .uri("lb://STORAGESERVICE"))
                .route("userService", r -> r.path("/api/userService/**")
                        .filters(f -> f
                                .circuitBreaker(c -> {
                                    c.setName("defaultCircuitBreaker");
                                    c.setFallbackUri("forward:/fallback/userService");
                                })
                                .modifyResponseBody(String.class, String.class, rewriteOpenApiBody("/api/userService"))
                                .stripPrefix(2))
                        .uri("lb://USERSERVICE"))
                .route("fileShareService", r -> r.path("/api/fileShareService/v3/api-docs")
                        .filters(f -> f
                                .circuitBreaker(c -> {
                                    c.setName("defaultCircuitBreaker");
                                    c.setFallbackUri("forward:/fallback/fileShareService");
                                })
                                .modifyResponseBody(String.class, String.class, rewriteOpenApiBody("/api/fileShareService"))
                                .stripPrefix(2))
                        .uri("lb://FILESHARESERVICE")
                )
                .route("fileShareService", r -> r.path("/api/fileShareService/**")
                        .filters(f -> f
                                .circuitBreaker(c -> {
                                    c.setName("defaultCircuitBreaker");
                                    c.setFallbackUri("forward:/fallback/fileShareService");
                                })
                                .stripPrefix(2))
                        .uri("lb://FILESHARESERVICE"))
                .build();
    }

    private RewriteFunction<String, String> rewriteOpenApiBody(String basePath) {
        return (exchange, originalBody) -> {
            String path = exchange.getRequest().getPath().value();
            if (!path.endsWith("/v3/api-docs")) {
                return Mono.justOrEmpty(originalBody);
            }

            try {
                JsonNode jsonNode = objectMapper.readTree(originalBody);
                ArrayNode servers = objectMapper.createArrayNode();
                ObjectNode server = objectMapper.createObjectNode();
                server.put("url", basePath);
                server.put("description", "Gateway Route");
                servers.add(server);

                if (jsonNode instanceof ObjectNode) {
                    ((ObjectNode) jsonNode).set("servers", servers);
                }

                return Mono.just(objectMapper.writeValueAsString(jsonNode));
            } catch (Exception e) {
                // Log error but return original body
                return Mono.justOrEmpty(originalBody);
            }
        };
    }
}