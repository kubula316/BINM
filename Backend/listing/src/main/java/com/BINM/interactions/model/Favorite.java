package com.BINM.interactions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "favorites",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "entityId", "entityType"}, name = "uk_favorite_user_entity")
    },
    indexes = {
        @Index(columnList = "userId, entityType", name = "idx_favorite_user_type")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String userId;

    @Column(nullable = false, updatable = false)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private EntityType entityType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
