package com.t1.achievements.repository;

import com.t1.achievements.entity.UserAchievement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    @Query("select ua from UserAchievement ua where ua.user.id = :userId")
    List<UserAchievement> findByUserId(UUID userId);

    interface AwardStat {
        UUID getAchievementId();
        long getAwardedCount();
    }
    boolean existsByUserIdAndAchievementId(UUID userId, UUID achievementId);
    void deleteByUserIdAndAchievementId(UUID userId, UUID achievementId);
    Optional<UserAchievement> findByUserIdAndAchievementId(UUID userId, UUID achievementId);

    @Query("""
           select ua.achievement.id as achievementId, count(distinct ua.user.id) as awardedCount
           from UserAchievement ua
           group by ua.achievement.id
           """)
    List<AwardStat> countAwardedByAchievement();
    Optional<UserAchievement> findFirstByUserIdAndAchievementId(UUID userId, UUID achievementId);

    @Query("""
           select count(distinct ua.user.id)
           from UserAchievement ua
           where ua.achievement.id = :achievementId
           """)
    long countAwardedByAchievementId(@Param("achievementId") UUID achievementId);

    @Query("select ua from UserAchievement ua " +
            "where ua.user.id = :userId " +
            "order by ua.awardedAt desc")
    List<UserAchievement> findByUserIdOrderByAwardedAtDesc(@Param("userId") UUID userId);

    @Query("select count(ua) from UserAchievement ua " +
            "where ua.awardedAt >= :from")
    long countAwardedSince(@Param("from") Instant from);

    @Query("select count(distinct ua.user.id) from UserAchievement ua " )
    long countUsersWithAnyAwards();

    interface TopAward {
        UUID getAchievementId();
        long getCnt();
    }

    @Query("select ua.achievement.id as achievementId, count(ua) as cnt " +
            "from UserAchievement ua " +
            "group by ua.achievement.id " +
            "order by cnt desc")
    List<TopAward> findTopAwardedAllTime(Pageable pageable);

    @Query("select ua.achievement.id as achievementId, count(ua) as cnt " +
            "from UserAchievement ua " +
            "where ua.awardedAt >= :from " +
            "group by ua.achievement.id " +
            "order by cnt desc")
    List<TopAward> findTopAwardedSince(@Param("from") Instant from, Pageable pageable);

    // Для postgres-агрегации по месяцам
    interface MonthBucket {
        String getMonth(); // формат YYYY-MM
        long getCnt();
    }

    @Query(value = """
        select to_char(date_trunc('month', ua.awarded_at), 'YYYY-MM') as month,
               count(*) as cnt
          from user_achievements ua
         where ua.awarded_at >= :from
         group by 1
         order by 1
        """, nativeQuery = true)
    List<MonthBucket> countAwardsByMonthSince(@Param("from") Instant from);
}
