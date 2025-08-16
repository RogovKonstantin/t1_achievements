package com.t1.achievements.controller.api;

import com.t1.achievements.dto.*;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "achievements", description = "API для чтения ачивок")
@RequestMapping("/achievements")
public interface AchievementApi {

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный ID",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    })

    @Operation(summary = "Профиль пользователя с полученными и начатыми ачивками")
    @GetMapping("/user/{userId}")
    ProfileAchievementsService.ProfileViewDto getUserAchievements(
            @PathVariable @NotNull(message = "Параметр userId обязателен") UUID userId);

    @Operation(summary = "Все ачивки по секциям (userId из токена), карта ачивок")
    @GetMapping("/map")
    ProfileAchievementsService.SectionsViewDto getMyAchievements(
            @AuthenticationPrincipal UserDetails principal);




    @Operation(summary = "Детали ачивки для конкретного пользователя")
    @GetMapping("details/{achievementId}/{userId}")
    AchievementDetailDto getAchievementForUser(@PathVariable @NotNull UUID achievementId,
                                               @PathVariable @NotNull UUID userId);
}
