package com.example.warehouse.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OpenApiConfigTest {

    @Autowired(required = false)
    OpenAPI openAPI;

    @Test
    void openApiBeanPresent() {
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getInfo().getTitle());
    }
}
