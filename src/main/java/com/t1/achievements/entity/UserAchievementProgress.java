package com.t1.achievements.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "user_achievement_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","achievement_id"}))
public class UserAchievementProgress {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(nullable = false) private Integer currentCount = 0;
    @Column(nullable = false) private Double percentDone = 0.0; // 0..100

    @Column(nullable = false) private Instant updatedAt = Instant.now();
}
