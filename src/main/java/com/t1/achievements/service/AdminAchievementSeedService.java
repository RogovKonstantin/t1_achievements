// src/main/java/com/t1/achievements/service/AdminAchievementSeedService.java
package com.t1.achievements.service;

import com.t1.achievements.entity.Achievement;
import com.t1.achievements.entity.User;
import com.t1.achievements.entity.UserAchievementProgress;
import com.t1.achievements.repository.AchievementCriterionRepository;
import com.t1.achievements.repository.AchievementRepository;
import com.t1.achievements.repository.UserAchievementProgressRepository;
import com.t1.achievements.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAchievementSeedService {

    private final AchievementRepository achievementRepo;
    private final AchievementCriterionRepository criterionRepo;
    private final UserRepository userRepo;
    private final UserAchievementProgressRepository progressRepo;

    @Transactional
    public SeedResult seedProgressZero(UUID achievementId, boolean resetExisting) {
        Achievement ach = achievementRepo.findById(achievementId).orElseThrow();
        int totalRequired = criterionRepo.findByAchievementId(achievementId)
                .stream().mapToInt(c -> c.getRequiredCount()).sum();

        List<User> users = userRepo.findAllByActiveTrue();
        int created = 0;
        int updated = 0;

        for (User u : users) {
            var opt = progressRepo.findByUserIdAndAchievementId(u.getId(), ach.getId());
            if (opt.isPresent()) {
                if (resetExisting) {
                    var p = opt.get();
                    p.setTotalSteps(totalRequired);
                    p.setCurrentStep(0);
                    progressRepo.save(p);
                    updated++;
                }
            } else {
                progressRepo.save(UserAchievementProgress.builder()
                        .user(u)
                        .achievement(ach)
                        .totalSteps(totalRequired)
                        .currentStep(0)
                        .build());
                created++;
            }
        }
        return new SeedResult(users.size(), created, updated, totalRequired);
    }

    public record SeedResult(int processedUsers, int created, int updated, int totalSteps) {}
}
