package com.t1.achievements.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username"),
        @Index(name = "idx_users_active", columnList = "active")
})
public class User {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String fullName;
    private String position;
    private String department;
    private Integer grade;
    private LocalDate hireDate;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false) // захэшированный пароль
    private String password;

    @Column(nullable = false)
    private Boolean active = true;

    private String email;
    private String phone;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate void touch() { this.updatedAt = Instant.now(); }
}
