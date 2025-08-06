package com.t1.achievements.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "achievement_stages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "achievement_id")
    private Achievement achievement;

    private String stageName;
    private Boolean required;
}

