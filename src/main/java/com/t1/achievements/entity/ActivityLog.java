package com.t1.achievements.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "activity_logs",
        indexes = {
                @Index(name="idx_activity_emp_type_time", columnList = "user_id,activity_type_id,occurredAt DESC"),
                @Index(name="uq_activity_fact", columnList = "user_id,activity_type_id,sourceSystem,sourceEventId", unique = true)
        })
public class ActivityLog {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "activity_type_id", nullable = false)
    private ActivityType activityType;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false) private String sourceSystem = "";
    @Column(nullable = false) private String sourceEventId = "";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payload = Map.of();
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
