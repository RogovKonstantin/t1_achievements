package com.t1.achievements.repository;


import com.t1.achievements.entity.AchievementStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AchievementStageRepository extends JpaRepository<AchievementStage, UUID> {

}

