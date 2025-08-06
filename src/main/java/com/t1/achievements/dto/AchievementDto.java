package com.t1.achievements.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementDto {
    private Long id;
    private String name;
    private String description;
    private String criteria;
}

