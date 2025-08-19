// com.t1.achievements.dto.SectionDto
package com.t1.achievements.dto;

import lombok.Builder;

import java.util.UUID;
@Builder
public record SectionDto(
        UUID id,
        String code,
        String name,
        String description,
        Integer sortOrder,
        Boolean active
) {}
