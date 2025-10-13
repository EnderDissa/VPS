package com.example.warehouse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InfrastructureSmokeTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void liquibaseHasRun() {
        Integer cnt = jdbcTemplate.queryForObject(
                "select count(*) from public.databasechangelog",
                Integer.class
        );
        Assertions.assertNotNull(cnt);
        Assertions.assertTrue(cnt > 0, "Liquibase changelog must have entries");
    }
}
