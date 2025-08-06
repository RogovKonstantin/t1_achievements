package com.t1.achievements.entity;


import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "user_achievement_stages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievementStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private AchievementStage stage;

    private Boolean completed;
    private Date completedAt;
}
