package com.t1.achievements.repository;

import com.t1.achievements.entity.AchievementCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AchievementCriterionRepository extends JpaRepository<AchievementCriterion, UUID> {

    interface SumRequired {
        UUID getAchievementId();
        Integer getTotalRequired();
    }

    @Query("""
        select c.achievement.id as achievementId, sum(c.requiredCount) as totalRequired
        from AchievementCriterion c
        where c.achievement.id in :achievementIds
        group by c.achievement.id
    """)
    List<SumRequired> sumRequiredByAchievementIds(Collection<UUID> achievementIds);
    List<AchievementCriterion> findByAchievementId(UUID achievementId);

}
