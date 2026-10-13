package com.aarav.ecommercebackend.order.repository;

import com.aarav.ecommercebackend.order.entity.Order;
import com.aarav.ecommercebackend.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Order> findWithItemsById(UUID id);

    @EntityGraph(attributePaths = {"items"})
    Page<Order> findByUserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"items"})
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Optional<Order> findByOrderNumber(String orderNumber);
}
