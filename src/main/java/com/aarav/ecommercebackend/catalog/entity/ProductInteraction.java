package com.aarav.ecommercebackend.catalog.entity;

import com.aarav.ecommercebackend.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "product_interactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_interactions_user_product",
                        columnNames = {"user_id", "product_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_product_interactions_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_product_interactions_product",
                        columnList = "product_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductInteraction {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "has_seen", nullable = false)
    @Builder.Default
    private boolean hasSeen = false;

    @Min(0)
    @Column(name = "total_view_duration_ms", nullable = false)
    @Builder.Default
    private Long totalViewDurationMs = 0L;

    @Min(0)
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "liked", nullable = false)
    @Builder.Default
    private boolean liked = false;

    @Column(name = "first_seen_at")
    private OffsetDateTime firstSeenAt;

    @Column(name = "last_seen_at")
    private OffsetDateTime lastSeenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public void recordView(long durationMs) {
        if (durationMs < 0) {
            throw new IllegalArgumentException(
                    "View duration cannot be negative"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        if (!hasSeen) {
            firstSeenAt = now;
        }

        hasSeen = true;
        lastSeenAt = now;
        viewCount++;
        totalViewDurationMs += durationMs;
    }
}