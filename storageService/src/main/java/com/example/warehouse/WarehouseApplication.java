package com.example.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import reactivefeign.spring.config.EnableReactiveFeignClients;

@SpringBootApplication
@EnableReactiveFeignClients(basePackages = "com.example.warehouse.client")
public class WarehouseApplication {
	public static void main(String[] args) {
		SpringApplication.run(WarehouseApplication.class, args);
	}
}
