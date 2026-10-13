package com.aarav.ecommercebackend.order.controller;

import com.aarav.ecommercebackend.order.dto.request.CancelOrderRequestDTO;
import com.aarav.ecommercebackend.order.dto.request.CheckoutRequestDTO;
import com.aarav.ecommercebackend.order.dto.request.CreateReviewRequestDTO;
import com.aarav.ecommercebackend.order.dto.request.UpdateOrderStatusDTO;
import com.aarav.ecommercebackend.order.dto.response.OrderResponseDTO;
import com.aarav.ecommercebackend.order.dto.response.OrderSummaryResponseDTO;
import com.aarav.ecommercebackend.order.dto.response.ReviewResponseDTO;
import com.aarav.ecommercebackend.order.entity.OrderStatus;
import com.aarav.ecommercebackend.order.service.OrderService;
import com.aarav.ecommercebackend.order.service.ReviewService;
import com.aarav.ecommercebackend.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ReviewService reviewService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponseDTO> checkout(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CheckoutRequestDTO request
    ) {
        OrderResponseDTO response = orderService.checkout(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping({"", "/"})
    public ResponseEntity<Page<OrderSummaryResponseDTO>> getOrderHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getOrderHistory(principal.getId(), pageable));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID orderId
    ) {
        boolean isAdmin = principal != null && principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(orderService.getOrderById(principal.getId(), orderId, isAdmin));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrReturnOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID orderId,
            @Valid @RequestBody CancelOrderRequestDTO request
    ) {
        return ResponseEntity.ok(orderService.cancelOrReturnOrder(principal.getId(), orderId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<Page<OrderSummaryResponseDTO>> getAllOrdersAdmin(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getAllOrdersAdmin(status, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatusAdmin(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusDTO request
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatusAdmin(orderId, request));
    }

    @PostMapping("/{orderId}/items/{productId}/review")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID orderId,
            @PathVariable UUID productId,
            @Valid @RequestBody CreateReviewRequestDTO request
    ) {
        ReviewResponseDTO response = reviewService.createReview(principal.getId(), orderId, productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
