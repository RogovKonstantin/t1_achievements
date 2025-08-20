package com.t1.achievements.RR;

import jakarta.validation.constraints.NotBlank;

public record IconPayLoad(
        @NotBlank String filename,
        @NotBlank String contentType,   // например: image/png
        @NotBlank String dataBase64
) {}
