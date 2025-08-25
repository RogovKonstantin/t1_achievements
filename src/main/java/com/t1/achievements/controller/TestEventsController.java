package com.t1.achievements.controller;

import com.t1.achievements.RR.TestPassedEventRequest;
import com.t1.achievements.exception.StatusResponse;
import com.t1.achievements.service.ActivityIngestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "events", description = "Интеграция с фронтом: события прохождения тестов")
@RestController
@RequestMapping("/events/tests")
@RequiredArgsConstructor
public class TestEventsController {

    private final ActivityIngestService ingest;

    @Operation(summary = "Зафиксировать, что пользователь прошёл тест")
    @PostMapping("/passed")
    public ResponseEntity<StatusResponse> testPassed(@RequestBody @Valid TestPassedEventRequest req) {
        ingest.registerTestPassed(req.testCode(), req.userId());
        return ResponseEntity.ok(new StatusResponse("ok", "test completion recorded"));
    }
}
