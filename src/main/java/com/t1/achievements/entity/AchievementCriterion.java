package com.t1.achievements.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "achievement_criteria", indexes = {
        @Index(name="idx_criteria_achievement", columnList = "achievement_id")
})
public class AchievementCriterion {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "activity_type_id", nullable = false)
    private ActivityType activityType;

    @Column(nullable = false) private Integer requiredCount;
    private Integer withinDays;
    private String descriptionOverride;
    @Column(nullable = false) private Integer sortOrder = 100;
}
