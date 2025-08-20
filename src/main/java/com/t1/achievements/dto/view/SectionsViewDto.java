package com.t1.achievements.dto.view;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "SectionsViewDto", description = "Карта ачивок (все секции)")
public record SectionsViewDto(
        List<SectionWithCardsDto> sections
) {}
