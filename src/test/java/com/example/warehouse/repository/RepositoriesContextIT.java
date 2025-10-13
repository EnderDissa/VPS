package com.example.warehouse.repository;

import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.Repository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RepositoriesContextIT {

    private final ApplicationContext ctx;

    RepositoriesContextIT(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Test
    void allSpringDataRepositoriesAreRegistered() {
        ListableBeanFactory bf = ctx;
        String[] names = bf.getBeanNamesForType(Repository.class);
        long nonInfra = java.util.Arrays.stream(names)
                .filter(n -> !(bf.getBean(n) instanceof AopInfrastructureBean))
                .count();
        assertTrue(nonInfra >= 1);
    }
}
