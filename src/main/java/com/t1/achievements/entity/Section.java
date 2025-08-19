package com.t1.achievements.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "sections", indexes = @Index(name="idx_sections_active", columnList = "active"))
public class Section {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true) private String code;
    @Column(nullable = false) private String name;
    private String description;
    @Column(nullable = false) private Integer sortOrder = 100;
    @Column(nullable = false) private Boolean active = true;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
