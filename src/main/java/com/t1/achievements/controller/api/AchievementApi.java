package com.t1.achievements.controller.api;

import com.t1.achievements.dto.AchievementCategoriesDto;
import com.t1.achievements.dto.PageResponse;
import com.t1.achievements.dto.UpdateCategoriesRequest;
import com.t1.achievements.dto.UserListItemDto;
import com.t1.achievements.exception.StatusResponse;
import com.t1.achievements.service.ProfileAchievementsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "achievements", description = "API для управления ачивками")
@RequestMapping("/achievements")
public interface AchievementApi {

    @Operation(summary = "Получить ачивки пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный ID",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    })
    @GetMapping("/user/{userId}")
    ProfileAchievementsService.ProfileViewDto getUserAchievements(
            @PathVariable @NotNull(message = "Параметр userId обязателен") UUID userId);

}
