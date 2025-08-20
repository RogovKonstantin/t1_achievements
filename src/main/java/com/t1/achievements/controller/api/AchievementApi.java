package com.t1.achievements.controller.api;

import com.t1.achievements.dto.AchievementDetailDto;
import com.t1.achievements.dto.admin.AchievementAdminFullDto;
import com.t1.achievements.dto.view.ActivityFeedDto;
import com.t1.achievements.dto.view.ProfileViewDto;
import com.t1.achievements.dto.view.SectionsViewDto;
import com.t1.achievements.exception.StatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "achievements", description = "API для чтения ачивок")
@RequestMapping("/achievements")
public interface AchievementApi {

    @Operation(summary = "Профиль пользователя с полученными и начатыми ачивками")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ProfileViewDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный ID",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    })
    @GetMapping("/user/{userId}")
    ProfileViewDto getUserAchievements(
            @PathVariable @NotNull(message = "Параметр userId обязателен") UUID userId
    );

    @Operation(summary = "Все ачивки по секциям (userId из токена), карта ачивок")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = SectionsViewDto.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    })
    @GetMapping("/map")
    SectionsViewDto getMyAchievements(@AuthenticationPrincipal UserDetails principal);

    @Operation(summary = "Детали ачивки для конкретного пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = AchievementDetailDto.class))),
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный ID",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    })
    @GetMapping("/details/{achievementId}/{userId}")
    AchievementDetailDto getAchievementForUser(
            @PathVariable @NotNull UUID achievementId,
            @PathVariable @NotNull UUID userId
    );
    @Operation(summary = "Лента активности пользователя (награды)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ActivityFeedDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный ID",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    })
    @GetMapping("/activity/{userId}")
    ActivityFeedDto getUserActivity(
            @PathVariable @NotNull(message = "Параметр userId обязателен") UUID userId
    );


}
