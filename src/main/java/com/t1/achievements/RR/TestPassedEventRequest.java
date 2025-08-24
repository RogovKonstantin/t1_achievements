package com.t1.achievements.RR;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TestPassedEventRequest(
        @NotNull UUID userId,
        @NotBlank String testCode             // см. TestCodes
) {}

