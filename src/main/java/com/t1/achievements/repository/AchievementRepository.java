package com.t1.achievements.repository;

import com.t1.achievements.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import java.util.UUID;

public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    @Query("""
           select distinct a from Achievement a
           left join fetch a.sections s
           left join fetch a.icon i
           where a.active = true
           """)
    List<Achievement> findAllActiveWithDeps();
}

