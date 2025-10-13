package com.example.warehouse.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SimpleServiceUnitTest {

    @Test
    @DisplayName("Пример простого unit-теста бизнес-логики")
    void simpleMath() {
        int sum = 2 + 3;
        assertThat(sum).isEqualTo(5);
    }
}
