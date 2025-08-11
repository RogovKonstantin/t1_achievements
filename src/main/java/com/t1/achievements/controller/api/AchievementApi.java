package com.t1.achievements.controller.api;

import com.t1.achievements.exception.StatusResponse;
import com.t1.achievements.service.ProfileAchievementsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "achievements", description = "API для управления ачивками")
@RequestMapping("/user")
public interface AchievementApi {

    @Operation(summary = "Получить ачивки пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный ID",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    })
    @GetMapping("/achievements/{userId}")
    ProfileAchievementsService.ProfileViewDto getUserAchievements(
            @PathVariable @NotNull(message = "Параметр userId обязателен") UUID userId);

}
