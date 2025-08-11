package com.t1.achievements.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "tags", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private String name;
}
