// com.t1.achievements.controller.api.AchievementAdminApi
package com.t1.achievements.controller.api;

import com.t1.achievements.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
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
}
