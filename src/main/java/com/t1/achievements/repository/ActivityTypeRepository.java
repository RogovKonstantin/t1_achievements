package com.t1.achievements.repository;

import com.t1.achievements.entity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ActivityTypeRepository extends JpaRepository<ActivityType, UUID> {
}
