package com.example.warehouse.dto;

import com.example.warehouse.domain.enumeration.ItemCondition;
import com.example.warehouse.domain.enumeration.ItemType;
import com.example.warehouse.infrastructure.web.dto.ItemDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
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
