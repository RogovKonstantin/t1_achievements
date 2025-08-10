package com.t1.achievements.repository;


import com.t1.achievements.entity.User;
import org.springframework.context.annotation.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("select u from User u left join fetch u.roles where u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);
    @Query("select ua.achievement.id, count(ua) from UserAchievement ua group by ua.achievement.id")
    List<Object[]> countHoldersGrouped(); // (achievementId, cnt)
}

