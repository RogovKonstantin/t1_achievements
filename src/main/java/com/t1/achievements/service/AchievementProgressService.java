// src/main/java/com/t1/achievements/service/AchievementProgressService.java
package com.t1.achievements.service;

import com.t1.achievements.entity.*;
import com.t1.achievements.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AchievementProgressService {

    private final AchievementRepository achievementRepo;
    private final AchievementCriterionRepository criterionRepo;
    private final ActivityLogRepository activityLogRepo;
    private final UserAchievementProgressRepository progressRepo;
    private final UserAchievementRepository userAchRepo;

    /**
     * Пересчитать прогресс по всем ачивкам, содержащим критерии с данным ActivityType.
     */
    @Transactional
    public void recalculateForUserByActivityType(User user, ActivityType type) {
        var criteria = criterionRepo.findByActivityType_Id(type.getId());
        if (criteria.isEmpty()) return;

        // Собираем ачивки, где встречается этот критерий
        Set<UUID> achievementIds = new HashSet<>();
        for (var c : criteria) {
            achievementIds.add(c.getAchievement().getId());
        }
        for (UUID achId : achievementIds) {
            recalcForUserAndAchievement(user.getId(), achId);
        }
    }

    /**
     * Полный пересчёт прогресса пользователя по конкретной ачивке.
     * Если закрыта — выдаём (если ещё не выдана).
     */
    @Transactional
    public void recalcForUserAndAchievement(UUID userId, UUID achievementId) {
        Achievement ach = achievementRepo.findById(achievementId).orElseThrow();
        List<AchievementCriterion> criteria = criterionRepo.findByAchievementId(achievementId);

        // Считаем totalRequired
        int totalRequired = criteria.stream()
                .mapToInt(AchievementCriterion::getRequiredCount)
                .sum();

        // Считаем текущие шаги
        User stubUser = User.builder().id(userId).build(); // чтобы не тянуть пользователя полностью
        int current = 0;
        for (var c : criteria) {
            Instant after = null;
            if (c.getWithinDays() != null) {
                after = Instant.now().minus(c.getWithinDays(), ChronoUnit.DAYS);
            }
            long done = activityLogRepo.countByUserAndType(stubUser, c.getActivityType());
            current += (int) Math.min(done, c.getRequiredCount());
        }

        // Обновляем/создаём progress
        var progressOpt = progressRepo.findByUserIdAndAchievementId(userId, achievementId);
        UserAchievementProgress progress = progressOpt.orElseGet(() -> UserAchievementProgress.builder()
                .user(stubUser)
                .achievement(ach)
                .currentStep(0)
                .totalSteps(totalRequired)
                .build());

        progress.setTotalSteps(totalRequired);
        progress.setCurrentStep(current);
        progressRepo.save(progress);

        // Если выполнено полностью — выдаём награду (если ещё не)
        if (current >= totalRequired && totalRequired > 0) {
            if (!userAchRepo.existsByUserIdAndAchievementId(userId, achievementId)) {
                userAchRepo.save(UserAchievement.builder()
                        .user(stubUser)
                        .achievement(ach)
                        .method(UserAchievement.Method.AUTO)
                        .evidence(Map.of("reason", "all criteria satisfied"))
                        .build());
            }
        }
    }
}
