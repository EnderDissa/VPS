package com.example.warehouse.api;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiExceptionHandlerIT {

    @Autowired
    MockMvc mvc;

    @Test
    void invalidJson_400() throws Exception {
        mvc.perform(
                        post("/test/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\": ,}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_JSON"))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/test/validate"));
    }

    @Test
    void missingRequestParam_400() throws Exception {
        mvc.perform(get("/test/type-mismatch"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_REQUEST_PARAM"))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/test/type-mismatch"))
                .andExpect(jsonPath("$.details[0]", Matchers.containsString("age")))
                .andExpect(jsonPath("$.details[0]", Matchers.containsString("required")));
    }

    @Test
    void typeMismatch_400() throws Exception {
        mvc.perform(get("/test/type-mismatch").param("age", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TYPE_MISMATCH"))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/test/type-mismatch"))
                .andExpect(jsonPath("$.details[0]").value("Parameter 'age' must be of type 'int'"));
    }

    @Test
    void notFound_404() throws Exception {
        mvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    void dataIntegrityViolation_409() throws Exception {
        mvc.perform(post("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DATA_INTEGRITY_VIOLATION"))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/test/conflict"))
                .andExpect(jsonPath("$.details[0]", Matchers.containsString("unique")))
                .andExpect(jsonPath("$.details[0]", Matchers.containsString("conflict")));
    }

    @Nested
    class Validation {

        @Test
        void validationErrors_400_fieldsAndMessages() throws Exception {
            mvc.perform(
                            post("/test/validate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"name\":\"\", \"qty\":0}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.path").value("/test/validate"))
                    .andExpect(jsonPath("$.validationErrors").isArray())
                    .andExpect(jsonPath("$.validationErrors.length()", Matchers.is(2)))
                    .andExpect(jsonPath("$.validationErrors..field", Matchers.containsInAnyOrder("name", "qty")))
                    .andExpect(jsonPath("$.validationErrors[?(@.field=='name')].message", Matchers.contains("name must not be blank")))
                    .andExpect(jsonPath("$.validationErrors[?(@.field=='qty')].message", Matchers.contains("qty must be >= 1")));
        }
    }
}
