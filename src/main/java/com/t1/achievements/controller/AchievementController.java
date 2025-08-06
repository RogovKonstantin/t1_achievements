package com.t1.achievements.controller;

import com.t1.achievements.controller.api.AchievementApi;
import com.t1.achievements.dto.AchievementDto;
import com.t1.achievements.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class AchievementController implements AchievementApi {
    private final AchievementService achievementService;

    @Override
    public List<AchievementDto> getAll() {
        return achievementService.getAllAchievements();
    }

    @Override
    public List<AchievementDto> getUserAchievements(UUID userId) {
        return achievementService.getUserAchievements(userId);
    }
}

