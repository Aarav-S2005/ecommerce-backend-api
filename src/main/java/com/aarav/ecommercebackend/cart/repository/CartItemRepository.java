package com.aarav.ecommercebackend.cart.repository;

import com.aarav.ecommercebackend.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByIdAndCartUserId(UUID itemId, UUID userId);
}
