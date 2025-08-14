// com.t1.achievements.service.AchievementViewService
package com.t1.achievements.service;

import com.t1.achievements.dto.AchievementDetailDto;
import com.t1.achievements.entity.*;
import com.t1.achievements.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AchievementViewService {

    private final AchievementRepository achievementRepo;
    private final UserRepository userRepo;
    private final UserAchievementRepository userAchRepo;
    private final UserAchievementProgressRepository progressRepo;
    private final AchievementCriterionRepository criterionRepo;

    private String assetUrl(Asset a) { return a == null ? null : "/assets/" + a.getId(); }

    @Transactional(readOnly = true)
    public AchievementDetailDto getForUser(UUID achievementId, UUID userId) {
        Achievement a = achievementRepo.findById(achievementId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Ачивка не найдена"));
        userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Пользователь не найден"));

        var awardOpt = userAchRepo.findFirstByUserIdAndAchievementId(userId, achievementId);

        var progressOpt = progressRepo.findByUserIdAndAchievementId(userId, achievementId);

        int totalSteps;
        int currentStep;

        if (progressOpt.isPresent()) {
            totalSteps = Math.max(1, progressOpt.get().getTotalSteps());
            currentStep = Math.min(progressOpt.get().getCurrentStep(), totalSteps);
        } else {
            var totals = criterionRepo.sumRequiredByAchievementIds(Set.of(achievementId));
            totalSteps = totals.isEmpty() ? 1 : Math.max(1, Optional.ofNullable(totals.get(0).getTotalRequired()).orElse(1));
            currentStep = 0;
        }

        long activeUsers = Math.max(1, userRepo.countActive());
        long awardedUsers = userAchRepo.countAwardedByAchievementId(achievementId);
        double rarityPercent = 100.0 * (awardedUsers / (double) activeUsers);

        return new AchievementDetailDto(
                a.getId(),
                a.getCode(),
                a.getTitle(),
                a.getShortDescription(),
                a.getDescriptionMd(),
                assetUrl(a.getAnimation()),
                awardOpt.isPresent(),
                awardOpt.map(UserAchievement::getAwardedAt).orElse(null),
                awardOpt.map(ua -> ua.getMethod().name()).orElse(null),
                currentStep,
                totalSteps,
                rarityPercent
        );
    }
}
