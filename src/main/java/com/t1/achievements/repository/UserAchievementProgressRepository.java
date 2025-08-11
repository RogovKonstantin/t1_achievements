package com.t1.achievements.repository;

import com.t1.achievements.entity.UserAchievementProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserAchievementProgressRepository extends JpaRepository<UserAchievementProgress, UUID> {
    List<UserAchievementProgress> findByUserId(UUID userId);
}
