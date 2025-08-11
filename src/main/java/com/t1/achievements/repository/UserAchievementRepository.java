package com.t1.achievements.repository;

import com.t1.achievements.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    @Query("select ua from UserAchievement ua where ua.user.id = :userId")
    List<UserAchievement> findByUserId(UUID userId);

    interface AwardStat {
        UUID getAchievementId();
        long getAwardedCount();
    }

    @Query("""
           select ua.achievement.id as achievementId, count(distinct ua.user.id) as awardedCount
           from UserAchievement ua
           group by ua.achievement.id
           """)
    List<AwardStat> countAwardedByAchievement();
}
