package com.t1.achievements.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "achievement_stages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementStage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "achievement_id")
    private Achievement achievement;

    private String stageName;
    private Boolean required;
}

