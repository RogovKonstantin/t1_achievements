package com.t1.achievements.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "activity_types",
        uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class ActivityType {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private String name;
    private String description;
    private String sourceSystem;

    @Column(nullable = false) private Boolean active = true;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
