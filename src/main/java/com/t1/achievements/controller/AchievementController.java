package com.t1.achievements.controller;

import com.t1.achievements.controller.api.AchievementApi;
import com.t1.achievements.dto.AchievementCardDto;
import com.t1.achievements.service.AchievementService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated

public class AchievementController implements AchievementApi {
    private final AchievementService achievementService;

    @Override
    public List<AchievementCardDto> getAll() {
        return achievementService.getAllAchievements();
    }

    @Override
    public List<AchievementCardDto> getUserAchievements(@PathVariable @NotNull(message = "Параметр userId обязателен") UUID userId) {
        return achievementService.getUserAchievements(userId);
    }
}

