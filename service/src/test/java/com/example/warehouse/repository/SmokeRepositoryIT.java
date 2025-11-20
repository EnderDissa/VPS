package com.example.warehouse.repository;

import com.example.warehouse.testinfra.PostgresTcBase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
class SmokeRepositoryIT extends PostgresTcBase {

    @PersistenceContext
    EntityManager em;

    @Test
    void liquibaseAndJpaAreWorking() {
        assertThatCode(() ->
                em.createNativeQuery("SELECT 1").getSingleResult()
        ).doesNotThrowAnyException();
    }
}
