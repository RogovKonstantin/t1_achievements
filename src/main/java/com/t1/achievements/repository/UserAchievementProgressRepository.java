package com.t1.achievements.repository;

import com.t1.achievements.entity.UserAchievementProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAchievementProgressRepository extends JpaRepository<UserAchievementProgress, UUID> {
    List<UserAchievementProgress> findByUserId(UUID userId);
    Optional<UserAchievementProgress> findByUserIdAndAchievementId(UUID userId, UUID achievementId);
    boolean existsByUserIdAndAchievementId(UUID userId, UUID achievementId);
    void deleteByUserIdAndAchievementId(UUID userId, UUID achievementId);
}
