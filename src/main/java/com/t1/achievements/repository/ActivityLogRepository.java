package com.t1.achievements.repository;

import com.t1.achievements.entity.ActivityLog;
import com.t1.achievements.entity.ActivityType;
import com.t1.achievements.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    @Query("""
        select count(l) from ActivityLog l
        where l.user = :user and l.activityType = :type
    """)
    long countByUserAndType(User user, ActivityType type);

}

