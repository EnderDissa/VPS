package com.example.warehouse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        try (Connection connection = dataSource.getConnection()) {
            return ResponseEntity.ok("Database connection: OK");
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Database connection: FAILED");
        }
    }
}
