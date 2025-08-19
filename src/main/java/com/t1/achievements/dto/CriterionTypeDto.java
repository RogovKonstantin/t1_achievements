// com.t1.achievements.dto.CriterionTypeDto
package com.t1.achievements.dto;

public record CriterionTypeDto(
        String code,
        String name,
        String description,
        String inputType,
        String unit
) {}