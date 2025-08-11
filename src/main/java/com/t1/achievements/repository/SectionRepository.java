package com.t1.achievements.repository;

import com.t1.achievements.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SectionRepository extends JpaRepository<Section, UUID> {
    Optional<Section> findByCode(String code);
}