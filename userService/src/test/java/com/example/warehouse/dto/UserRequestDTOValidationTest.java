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
                RoleType.ADMIN,
                null,
                "bad"
        );

        var v = validator.validate(rq);
        assertThat(v).extracting(cv -> cv.getPropertyPath().toString())
                .contains("firstName","lastName","email");
    }

    @Test
    void valid() {
        UserRequestDTO rq = new UserRequestDTO(
                "534534",
                "Neo",
                "The",
                RoleType.STUDENT,
                "neo@matrix.io",
                "neo@matrix.io"
        );


        assertThat(validator.validate(rq)).isEmpty();
    }
}
