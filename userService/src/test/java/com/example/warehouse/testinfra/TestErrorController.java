package com.example.warehouse.testinfra;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Profile("test")
@RestController
@RequestMapping("/test")
public class TestErrorController {

    public record CreateRq(
            @NotBlank(message = "name must not be blank")
            String name,
            @Min(value = 1, message = "qty must be >= 1")
            int qty
    ) {}

    @PostMapping(value = "/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String validate(@Valid @RequestBody CreateRq rq) {
        return "OK";
    }


    @PostMapping("/conflict")
    public void conflict() {
        throw new DataIntegrityViolationException("Simulated unique key conflict");
    }

    @GetMapping("/type-mismatch")
    public String typeMismatch(@RequestParam int age) {
        return "age=" + age;
    }

    @PostMapping(value = "/malformed", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void malformedJson(@RequestBody CreateRq rq) {
    }
}
