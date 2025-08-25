package com.t1.achievements.RR;

import jakarta.validation.constraints.NotBlank;

public record IconPayLoad(
        @NotBlank String filename,
        @NotBlank String contentType,
        @NotBlank String dataBase64
) {}
