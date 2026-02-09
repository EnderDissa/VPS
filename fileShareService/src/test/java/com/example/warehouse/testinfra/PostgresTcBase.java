package com.example.warehouse.testinfra;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public abstract class PostgresTcBase {
    // intentionally empty: using JDBC Testcontainers URL (jdbc:tc:...) from application-test.yml
}
