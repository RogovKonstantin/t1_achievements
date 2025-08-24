package com.t1.achievements.repository;

import com.t1.achievements.entity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityTypeRepository extends JpaRepository<ActivityType, UUID> {
    Optional<ActivityType> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    Optional<ActivityType> findByCode(String code);


    @Query("select t from ActivityType t where t.active = true")
    List<ActivityType> findAllActive();
}
