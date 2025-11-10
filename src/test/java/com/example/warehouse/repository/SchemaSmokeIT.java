package com.example.warehouse.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SchemaSmokeIT {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void unique_user_storage_access_userId_storageId_exists() {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "select tc.constraint_name, tc.constraint_type, ccu.column_name " +
                        "from information_schema.table_constraints tc " +
                        "join information_schema.constraint_column_usage ccu " +
                        "on tc.constraint_name = ccu.constraint_name and tc.table_schema = ccu.table_schema " +
                        "where tc.table_schema = 'public' and tc.table_name = 'user_storage_access' and tc.constraint_type = 'UNIQUE'"
        );
        Map<String, Set<String>> byConstraint = new HashMap<>();
        for (Map<String, Object> r : rows) {
            String name = String.valueOf(r.get("constraint_name"));
            String col = String.valueOf(r.get("column_name"));
            byConstraint.computeIfAbsent(name, k -> new HashSet<>()).add(col);
        }
        boolean exists = byConstraint.values().stream().anyMatch(set ->
                set.containsAll(Arrays.asList("user_id", "storage_id"))
        );
        if (!rows.isEmpty()) {
            String dbg = byConstraint.entrySet().stream()
                    .map(e -> e.getKey() + ":" + e.getValue())
                    .collect(Collectors.joining(", "));
            assertTrue(exists, dbg);
        } else {
            fail("no constraints found");
        }
    }
}
