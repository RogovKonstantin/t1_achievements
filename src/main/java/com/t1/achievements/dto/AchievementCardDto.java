// AchievementCardDto.java
package com.t1.achievements.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AchievementCardDto {
    private String id;
    private String code;
    private String title;
    private String shortDescription;
    private Integer points;
    private Boolean repeatable;
    private String visibility;
    private Boolean active;

    private String iconUrl;
    private String bannerUrl;

    private List<SectionDto> sections;
    private List<TagDto> tags;

    private CreatedByDto createdBy;
    private String awardedAt;
    private String awardMethod;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SectionDto {
        private String id;
        private String code;
        private String title;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TagDto {
        private String id;
        private String code;
        private String name;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreatedByDto {
        private String id;
        private String fullName;
    }
}
