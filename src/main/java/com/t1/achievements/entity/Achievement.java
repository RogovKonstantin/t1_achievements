package com.t1.achievements.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "achievements", indexes = @Index(name="idx_achievements_active", columnList="active"))
public class Achievement {

    public enum Visibility { PUBLIC, PRIVATE, HIDDEN }

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true) private String code;
    @Column(nullable = false) private String title;

    private String shortDescription;
    @Column(columnDefinition = "text") private String descriptionMd;

    @Column(nullable = false) private Integer points = 0;
    @Column(nullable = false) private Boolean repeatable = false;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Visibility visibility = Visibility.PUBLIC;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "icon_asset_id")
    private Asset icon;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "banner_asset_id")
    private Asset banner;

    @Column(nullable = false) private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @UpdateTimestamp @Column(nullable = false)
    private Instant updatedAt = Instant.now();
    @PreUpdate void touch() { this.updatedAt = Instant.now(); }

    @ManyToMany
    @JoinTable(
            name = "achievement_sections",
            joinColumns = @JoinColumn(name = "achievement_id"),
            inverseJoinColumns = @JoinColumn(name = "section_id")
    )
    private Set<Section> sections = new HashSet<>();
}

