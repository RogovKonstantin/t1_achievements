package com.t1.achievements.repository;

import com.t1.achievements.entity.User;
import org.springframework.context.annotation.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("select u from User u left join fetch u.role where u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);
    @Query("select ua.achievement.id, count(ua) from UserAchievement ua group by ua.achievement.id")
    List<Object[]> countHoldersGrouped();
    @Query("select count(u) from User u where u.active = true")
    long countActive();
    @Query("""
           select u from UserAchievement ua
           join ua.user u
           where ua.achievement.id = :achievementId
           order by u.fullName asc
           """)
    Page<User> findHoldersByAchievementId(@Param("achievementId") UUID achievementId, Pageable pageable);

    Page<User> findByActiveTrue(Pageable pageable);
    List<User> findAllByActiveTrue();

}

