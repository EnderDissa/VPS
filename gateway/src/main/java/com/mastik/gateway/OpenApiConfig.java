package com.mastik.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI warehouseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Warehouse Management API")
                        .description("REST API for Warehouse Management System")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Warehouse Team")
                                .email("support@warehouse.com")));
    }
}
