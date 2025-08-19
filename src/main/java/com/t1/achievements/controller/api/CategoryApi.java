package com.t1.achievements.controller.api;

import com.t1.achievements.dto.SectionShortDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "categories", description = "API категорий ачивок")
@RequestMapping("/categories")
public interface CategoryApi {

    @Operation(summary = "Список активных категорий")
    @GetMapping
    List<SectionShortDto> listCategories();
}
