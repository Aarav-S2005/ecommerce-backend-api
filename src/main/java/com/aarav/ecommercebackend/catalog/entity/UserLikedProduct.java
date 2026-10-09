package com.aarav.ecommercebackend.catalog.entity;

import com.aarav.ecommercebackend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_liked_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLikedProduct {

    @EmbeddedId
    private UserLikedProductId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_liked_products_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_liked_products_product"))
    private Product product;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
