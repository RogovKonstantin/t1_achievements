package com.t1.achievements.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementStageDto {
    private Long id;
    private String stageName;
    private Boolean required;
    private Long achievementId;
}

