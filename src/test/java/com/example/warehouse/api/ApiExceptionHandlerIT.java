package com.example.warehouse.api;

import com.example.warehouse.testinfra.PostgresTcBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiExceptionHandlerIT extends PostgresTcBase {

    @Autowired
    MockMvc mvc;

    @Nested
    class Validation {

        @Test
        @DisplayName("400: ошибки валидации — список полей и сообщения")
        void shouldReturn400WithFields() throws Exception {
            String body = """
                    {"name":"", "qty":0}
                    """;
            mvc.perform(post("/test/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message", containsString("Validation failed")))
                    .andExpect(jsonPath("$.fields", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.fields[*].field", hasItems("name", "qty")));
        }
    }

    @Test
    @DisplayName("404: сущность не найдена")
    void shouldReturn404NotFound() throws Exception {
        mvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("409: конфликт целостности данных")
    void shouldReturn409Conflict() throws Exception {
        mvc.perform(post("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("DB_INTEGRITY"));
    }

    @Test
    @DisplayName("400: неверный тип параметра запроса (type mismatch)")
    void shouldReturn400TypeMismatch() throws Exception {
        mvc.perform(get("/test/type-mismatch").param("age", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields[0].field").value("age"));
    }

    @Test
    @DisplayName("400: невалидный JSON (HttpMessageNotReadableException)")
    void shouldReturn400MalformedJson() throws Exception {
        String malformed = "{name: 'no-quotes for key'}";
        mvc.perform(post("/test/malformed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformed))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Malformed JSON")));
    }
}
