package com.t1.achievements.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "SectionRefDto", description = "Короткая информация о секции, в которой состоит ачивка")
public record SectionRefDto(
        UUID id,
        String code,
        String name
) {}
