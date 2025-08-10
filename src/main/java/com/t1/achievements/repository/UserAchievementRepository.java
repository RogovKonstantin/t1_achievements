package com.t1.achievements.repository;

import com.t1.achievements.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    List<UserAchievement> findByUserId(UUID userId);

    @Query("select ua.achievement.id, count(ua) from UserAchievement ua group by ua.achievement.id")
    List<Object[]> countHoldersGrouped(); // (achievementId, cnt)

    @Query("""
        select ua
        from UserAchievement ua
          join fetch ua.achievement a
          left join fetch a.sections
          left join fetch a.tags
          left join fetch a.icon
          left join fetch a.banner
          left join fetch a.createdBy
        where ua.user.id = :userId
    """)
    List<UserAchievement> findByUserIdWithDeps(@Param("userId") UUID userId);
}
