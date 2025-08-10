package com.t1.achievements.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "assets",
        indexes = {
                @Index(name="idx_assets_bucket_key", columnList = "bucket,objectKey"),
                @Index(name="uq_assets_bucket_key_version", columnList = "bucket,objectKey,versionId", unique = true)
        })
public class Asset {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false) private String bucket;      // e.g. "achievements"
    @Column(nullable = false) private String objectKey;   // e.g. "icons/ambassador.png"
    @Column(nullable = false) private String versionId = ""; // пустая строка вместо NULL

    private String etag;
    private String contentType;
    private Long sizeBytes;
    private String checksumSha256;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "uploaded_by")
    private User uploadedBy;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
