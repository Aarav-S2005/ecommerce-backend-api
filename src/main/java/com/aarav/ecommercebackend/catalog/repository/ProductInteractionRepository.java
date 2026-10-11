package com.aarav.ecommercebackend.catalog.repository;

import com.aarav.ecommercebackend.catalog.entity.ProductInteraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductInteractionRepository extends JpaRepository<ProductInteraction, UUID> {

    Optional<ProductInteraction> findByUserIdAndProductId(UUID userId, UUID productId);

    @EntityGraph(attributePaths = {"product", "product.brand", "product.category"})
    @Query("SELECT pi FROM ProductInteraction pi WHERE pi.user.id = :userId AND pi.liked = true AND pi.product.active = true")
    Page<ProductInteraction> findLikedProductsByUserId(@Param("userId") UUID userId, Pageable pageable);
}
