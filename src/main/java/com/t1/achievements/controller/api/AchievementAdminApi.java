package com.t1.achievements.controller.api;

import com.t1.achievements.RR.CreateAchievementRequest;
import com.t1.achievements.RR.CreateSectionRequest;
import com.t1.achievements.dto.*;
import com.t1.achievements.dto.admin.AchievementAdminFullDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "achievements", description = "API админа для управления ачивками")
@RequestMapping("/admin")
public interface AchievementAdminApi {

    @Operation(summary = "Список обладателей ачивки (пагинация)")
    @GetMapping("/{achievementId}/holders")
    PageResponse<UserListItemDto> getHolders(
            @PathVariable UUID achievementId,
            @ParameterObject Pageable pageable
    );

    @Operation(summary = "Обновить привязки категорий (секций) к ачивке")
    @PatchMapping("/{achievementId}/categories")
    AchievementCategoriesDto updateCategories(
            @PathVariable UUID achievementId,
            @RequestBody UpdateCategoriesRequest body
    );

    @Operation(summary = "Создать раздел (категорию)")
    @PostMapping("/sections")
    SectionDto createSection(@RequestBody CreateSectionRequest body);

    @Operation(summary = "Список поддерживаемых типов критериев")
    @GetMapping("/achievements/criteria")
    List<CriterionTypeDto> listCriterionTypes();

    @Operation(summary = "Создать ачивку (multipart: JSON + icon + optional animation)")
    @PostMapping(value = "/achievements", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AchievementDto createAchievement(
            @RequestPart("request") @Valid CreateAchievementRequest request,
            @RequestPart("icon") MultipartFile icon,                               // обязательно
            @RequestPart(value = "animation") MultipartFile animation // опционально (gif)
    );
    @Operation(
            summary = "Полный список ачивок (для админа)",
            description = "Возвращает все ачивки с максимумом деталей: секции, критерии, суммарное число шагов, ссылки на иконку и анимацию."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AchievementAdminFullDto.class)))),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав")
    })
    @GetMapping("/achievements/full")
    List<AchievementAdminFullDto> listAllAchievementsFull();
}
