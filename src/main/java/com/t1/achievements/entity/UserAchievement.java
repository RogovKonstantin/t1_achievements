package com.t1.achievements.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "user_achievements", indexes = {
        @Index(name="idx_user_achievements_user", columnList = "user_id"),
        @Index(name="idx_user_achievements_ach", columnList = "achievement_id")
})
public class UserAchievement {
    public enum Method { AUTO, MANUAL, IMPORT }

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(nullable = false)
    private Instant awardedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "awarded_by")
    private User awardedBy; // null для авто

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Method method = Method.AUTO;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> evidence;

}
