package com.aarav.ecommercebackend.order.service;

import com.aarav.ecommercebackend.cart.entity.Cart;
import com.aarav.ecommercebackend.cart.entity.CartItem;
import com.aarav.ecommercebackend.cart.repository.CartRepository;
import com.aarav.ecommercebackend.catalog.entity.Product;
import com.aarav.ecommercebackend.catalog.repository.ProductRepository;
import com.aarav.ecommercebackend.order.dto.request.CancelOrderRequestDTO;
import com.aarav.ecommercebackend.order.dto.request.CancellationType;
import com.aarav.ecommercebackend.order.dto.request.CheckoutRequestDTO;
import com.aarav.ecommercebackend.order.dto.request.UpdateOrderStatusDTO;
import com.aarav.ecommercebackend.order.dto.response.OrderItemResponseDTO;
import com.aarav.ecommercebackend.order.dto.response.OrderResponseDTO;
import com.aarav.ecommercebackend.order.dto.response.OrderSummaryResponseDTO;
import com.aarav.ecommercebackend.order.entity.Order;
import com.aarav.ecommercebackend.order.entity.OrderItem;
import com.aarav.ecommercebackend.order.entity.OrderStatus;
import com.aarav.ecommercebackend.order.repository.OrderRepository;
import com.aarav.ecommercebackend.shared.exception.ResourceNotFoundException;
import com.aarav.ecommercebackend.user.entity.User;
import com.aarav.ecommercebackend.user.entity.UserAddress;
import com.aarav.ecommercebackend.user.repository.UserAddressRepository;
import com.aarav.ecommercebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "product_pages", allEntries = true)
    })
    public OrderResponseDTO checkout(UUID userId, CheckoutRequestDTO request) {
        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart is empty"));

        List<CartItem> activeItems = cart.getItems() != null
                ? cart.getItems().stream().filter(item -> item.getProduct().isActive()).toList()
                : List.of();

        if (activeItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty or contains no active products to checkout");
        }

        UserAddress address = userAddressRepository.findByIdAndUserId(request.getAddressId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with id: " + request.getAddressId()));

        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("addressLine1", address.getAddressLine1());
        addressMap.put("addressLine2", address.getAddressLine2());
        addressMap.put("city", address.getCity());
        addressMap.put("state", address.getState());
        addressMap.put("postalCode", address.getPostalCode());
        addressMap.put("country", address.getCountry());

        List<CartItem> sortedItems = activeItems.stream()
                .sorted(Comparator.comparing(i -> i.getProduct().getId()))
                .toList();

        for (CartItem item : sortedItems) {
            UUID productId = item.getProduct().getId();
            int requestedQuantity = item.getQuantity();

            int updated = productRepository.decrementStockIfAvailable(productId, requestedQuantity);
            if (updated == 0) {
                Product p = productRepository.findById(productId).orElse(item.getProduct());
                int available = Math.max(0, p.getStockQuantity());
                if (available == 0) {
                    throw new IllegalArgumentException("Product '" + p.getName() + "' is out of stock");
                } else {
                    throw new IllegalArgumentException("Product '" + p.getName() + "' only has " + available + " items available");
                }
            }
        }

        BigDecimal totalAmount = sortedItems.stream()
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String paymentMethod = (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank())
                ? request.getPaymentMethod().trim().toUpperCase()
                : "CARD";

        String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        User user = userRepository.getReferenceById(userId);

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .status(OrderStatus.PENDING)
                .paymentMethod(paymentMethod)
                .totalAmount(totalAmount)
                .shippingAddress(addressMap)
                .build();

        for (CartItem item : sortedItems) {
            BigDecimal subtotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(item.getProduct())
                    .productName(item.getProduct().getName())
                    .unitPrice(item.getProduct().getPrice())
                    .quantity(item.getQuantity())
                    .subtotal(subtotal)
                    .build();
            order.addItem(orderItem);
        }

        cart.getItems().clear();
        cartRepository.save(cart);

        Order saved = orderRepository.save(order);
        return toOrderResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponseDTO> getOrderHistory(UUID userId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);
        return orderPage.map(this::toOrderSummaryResponseDTO);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(UUID userId, UUID orderId, boolean isAdmin) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!isAdmin && !order.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }

        return toOrderResponseDTO(order);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "product_pages", allEntries = true)
    })
    public OrderResponseDTO cancelOrReturnOrder(UUID userId, UUID orderId, CancelOrderRequestDTO request) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }

        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.RETURNED || currentStatus == OrderStatus.EXCHANGED) {
            throw new IllegalArgumentException("Order has already been " + currentStatus.name().toLowerCase());
        }

        java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
        boolean isDelivered = currentStatus == OrderStatus.DELIVERED;
        boolean isCashOnDelivery = "CASH_ON_DELIVERY".equalsIgnoreCase(order.getPaymentMethod());
        CancellationType type = request.getType();

        if (isDelivered) {
            java.time.OffsetDateTime deliveryTime = order.getUpdatedAt();
            if (deliveryTime.plusDays(7).isBefore(now)) {
                throw new IllegalArgumentException("Refund or exchange requests are only eligible within 7 days of delivery.");
            }

            if (type == CancellationType.CANCELLATION) {
                throw new IllegalArgumentException("Delivered orders cannot be cancelled. You can only request an EXCHANGE or REFUND within 7 days of delivery.");
            }

            if (type == CancellationType.REFUND) {
                order.setStatus(OrderStatus.CANCELLED);
                restoreStock(order);
            } else if (type == CancellationType.EXCHANGE) {
                order.setStatus(OrderStatus.EXCHANGED);
            }
        } else {
            if (isCashOnDelivery) {
                if (type != CancellationType.CANCELLATION) {
                    throw new IllegalArgumentException("For undelivered Cash on Delivery orders, only CANCELLATION is permitted as no payment was collected.");
                }
                order.setStatus(OrderStatus.CANCELLED);
                restoreStock(order);
            } else {
                if (type != CancellationType.REFUND && type != CancellationType.CANCELLATION) {
                    throw new IllegalArgumentException("For undelivered prepaid orders, only CANCELLATION or REFUND is permitted.");
                }
                order.setStatus(OrderStatus.CANCELLED);
                restoreStock(order);
            }
        }

        order.setCancellationType(type.name());
        if (request.getReason() != null) {
            order.setCancellationReason(request.getReason().trim());
        }

        Order updated = orderRepository.save(order);
        return toOrderResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponseDTO> getAllOrdersAdmin(OrderStatus status, Pageable pageable) {
        Page<Order> orderPage = (status != null)
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAll(pageable);

        return orderPage.map(this::toOrderSummaryResponseDTO);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "product_pages", allEntries = true)
    })
    public OrderResponseDTO updateOrderStatusAdmin(UUID orderId, UpdateOrderStatusDTO request) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED && oldStatus != OrderStatus.RETURNED) {
            restoreStock(order);
        }

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        return toOrderResponseDTO(updated);
    }

    private void restoreStock(Order order) {
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                productRepository.incrementStock(item.getProduct().getId(), item.getQuantity());
            }
        }
    }

    private OrderResponseDTO toOrderResponseDTO(Order order) {
        List<OrderItemResponseDTO> itemDTOs = order.getItems() != null
                ? order.getItems().stream().map(this::toOrderItemResponseDTO).toList()
                : List.of();

        return OrderResponseDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .cancellationType(order.getCancellationType())
                .cancellationReason(order.getCancellationReason())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(itemDTOs)
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderSummaryResponseDTO toOrderSummaryResponseDTO(Order order) {
        int totalItems = order.getItems() != null
                ? order.getItems().stream().mapToInt(OrderItem::getQuantity).sum()
                : 0;

        return OrderSummaryResponseDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .totalAmount(order.getTotalAmount())
                .totalItems(totalItems)
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderItemResponseDTO toOrderItemResponseDTO(OrderItem item) {
        Product p = item.getProduct();
        String imageUrl = (p.getImages() != null && !p.getImages().isEmpty())
                ? p.getImages().get(0).getImageUrl()
                : null;

        return OrderItemResponseDTO.builder()
                .id(item.getId())
                .productId(p.getId())
                .productName(item.getProductName())
                .productImageUrl(imageUrl)
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }
}
