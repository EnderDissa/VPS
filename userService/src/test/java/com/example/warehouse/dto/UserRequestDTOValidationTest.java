package com.example.warehouse.dto;

import com.example.warehouse.dto.UserDTO.UserRequestDTO;
import com.example.warehouse.enumeration.RoleType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class UserRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void init() {
        ValidatorFactory f = Validation.buildDefaultValidatorFactory();
        validator = f.getValidator();
    }

    @Test
    void invalid_fields() {
        UserRequestDTO rq = new UserRequestDTO(
                null,
                "",
                null,
                "",
                null,
                "bad",
                LocalDateTime.now()
        );

        var v = validator.validate(rq);
        assertThat(v).extracting(cv -> cv.getPropertyPath().toString())
                .contains("firstName","lastName","email","role");
    }

    @Test
    void valid() {
        UserRequestDTO rq = new UserRequestDTO(
                null,
                "Neo",
                "The",
                "One",
                RoleType.STUDENT,
                "neo@matrix.io",
                LocalDateTime.now()
        );


        assertThat(validator.validate(rq)).isEmpty();
    }
}
