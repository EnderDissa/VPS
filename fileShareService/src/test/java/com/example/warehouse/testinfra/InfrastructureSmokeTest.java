package com.example.warehouse.testinfra;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InfrastructureSmokeTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void liquibaseHasRun() {
        Integer cnt = jdbc.queryForObject("select count(*) from public.databasechangelog", Integer.class);
        Assertions.assertNotNull(cnt);
        Assertions.assertTrue(cnt > 0);
    }
}
