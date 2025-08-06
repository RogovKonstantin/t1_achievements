package com.t1.achievements.dto;

import lombok.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievementStageDto {
    private Long id;
    private UserDto user;
    private AchievementStageDto stage;
    private Boolean completed;
    private Date completedAt;
}

