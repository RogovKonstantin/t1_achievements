package com.t1.achievements.dto;

import java.util.UUID;

public record SectionShortDto(
        UUID id,
        String name,
        String description
) {}
