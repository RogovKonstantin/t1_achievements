package com.t1.achievements.repository;

import com.t1.achievements.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    Optional<Achievement> findByCode(String code);
    @Query("""
        select distinct a from Achievement a
        left join fetch a.sections
        left join fetch a.tags
        left join fetch a.icon
        left join fetch a.banner
        left join fetch a.createdBy
    """)
    List<Achievement> findAllWithDeps();
}

