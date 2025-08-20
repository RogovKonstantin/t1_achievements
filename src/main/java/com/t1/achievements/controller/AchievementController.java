package com.t1.achievements.controller;

import com.t1.achievements.controller.api.AchievementApi;
import com.t1.achievements.dto.AchievementDetailDto;
import com.t1.achievements.dto.view.ActivityFeedDto;
import com.t1.achievements.dto.view.ProfileViewDto;
import com.t1.achievements.dto.view.SectionsViewDto;
import com.t1.achievements.repository.UserRepository;
import com.t1.achievements.service.AchievementViewService;
import com.t1.achievements.service.ProfileAchievementsService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
public class AchievementController implements AchievementApi {
    private final ProfileAchievementsService profileAchievementsService;
    private final AchievementViewService service;
    private final UserRepository userRepo;

    @Override
    public ProfileViewDto getUserAchievements(
            @PathVariable @NotNull(message = "Параметр userId обязателен") UUID userId) {
        return profileAchievementsService.getProfileView(userId);
    }

    @Override
    public SectionsViewDto getMyAchievements(@AuthenticationPrincipal UserDetails principal) {
        var u = userRepo.findByUsername(principal.getUsername()).orElseThrow();
        return profileAchievementsService.getSectionsViewAll(u.getId());
    }

    @Override
    public AchievementDetailDto getAchievementForUser(@NotNull UUID achievementId, @NotNull UUID userId) {
        return service.getForUser(achievementId, userId);
    }

    @Override
    public ActivityFeedDto getUserActivity(UUID userId) {
        return profileAchievementsService.getActivity(userId);
    }
}
