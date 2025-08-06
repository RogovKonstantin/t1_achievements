package com.t1.achievements.repository;

import com.t1.achievements.entity.UserAchievementStage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAchievementStageRepository extends JpaRepository<UserAchievementStage, Long> {
}
