package com.example.warehouse.dto;

import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ItemDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void valid_minimal() {
        ItemDTO d = new ItemDTO(
                1L,
                "Hammer",
                ItemType.TOOLS,
                ItemCondition.NEW,
                "SN-1",
                "Some description",
                LocalDateTime.now()
        );

        assertThat(validator.validate(d)).isEmpty();
    }
}
