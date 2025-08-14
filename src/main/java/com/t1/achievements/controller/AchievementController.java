package com.t1.achievements.controller;

import com.t1.achievements.controller.api.AchievementApi;
import com.t1.achievements.dto.AchievementDetailDto;
import com.t1.achievements.service.AchievementViewService;
import com.t1.achievements.service.ProfileAchievementsService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated

public class AchievementController implements AchievementApi {
    private final ProfileAchievementsService profileAchievementsService;
    private final AchievementViewService service;

    @Override
    public ProfileAchievementsService.ProfileViewDto getUserAchievements(@PathVariable @NotNull(message = "Параметр userId обязателен") UUID userId) {
        return profileAchievementsService.getProfileView(userId);
    }
    @Override
    public AchievementDetailDto getAchievementForUser(@NotNull UUID achievementId, @NotNull UUID userId) {
        return service.getForUser(achievementId, userId);
    }
}

