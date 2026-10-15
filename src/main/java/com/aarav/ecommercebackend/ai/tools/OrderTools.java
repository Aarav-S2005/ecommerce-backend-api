package com.aarav.ecommercebackend.ai.tools;

import com.aarav.ecommercebackend.cart.entity.Cart;
import com.aarav.ecommercebackend.cart.entity.CartItem;
import com.aarav.ecommercebackend.cart.repository.CartRepository;
import com.aarav.ecommercebackend.order.dto.request.CheckoutRequestDTO;
import com.aarav.ecommercebackend.order.dto.response.OrderResponseDTO;
import com.aarav.ecommercebackend.order.dto.response.OrderSummaryResponseDTO;
import com.aarav.ecommercebackend.order.dto.response.PolicyResponseDTO;
import com.aarav.ecommercebackend.order.entity.Order;
import com.aarav.ecommercebackend.order.repository.OrderRepository;
import com.aarav.ecommercebackend.order.service.OrderService;
import com.aarav.ecommercebackend.order.service.PolicyService;
import com.aarav.ecommercebackend.security.SecurityUtils;
import com.aarav.ecommercebackend.user.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderTools {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserAddressRepository userAddressRepository;
    private final PolicyService policyService;

    @Tool(description = "Calculate checkout summary including item totals, shipping estimate, and final grand total")
    public Map<String, Object> getCheckoutSummary(
            @ToolParam(description = "The UUID of the delivery address") String addressId
    ) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Cart cart = cartRepository.findWithItemsByUserId(userId).orElse(null);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return Map.of("ready", false, "message", "Cart is empty.");
        }
        BigDecimal subtotal = cart.getItems().stream()
                .filter(i -> i.getProduct().isActive())
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingFee = subtotal.compareTo(BigDecimal.valueOf(50)) >= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(5.00);
        BigDecimal total = subtotal.add(shippingFee);
        return Map.of(
                "ready", true,
                "itemCount", cart.getItems().stream().mapToInt(CartItem::getQuantity).sum(),
                "subtotal", subtotal,
                "shippingFee", shippingFee,
                "freeShippingEligible", shippingFee.compareTo(BigDecimal.ZERO) == 0,
                "totalAmount", total,
                "addressId", addressId
        );
    }

    @Tool(description = "Verify if checkout can proceed based on address existence, active items, and stock inventory")
    public Map<String, Object> validateOrderEligibility(
            @ToolParam(description = "The UUID of the delivery address") String addressId
    ) {
        UUID userId = SecurityUtils.getCurrentUserId();
        List<String> errors = new ArrayList<>();
        if (addressId == null || addressId.isBlank()) {
            errors.add("Delivery address ID is missing.");
        } else {
            boolean addressExists = userAddressRepository.findByIdAndUserId(UUID.fromString(addressId), userId).isPresent();
            if (!addressExists) {
                errors.add("Selected delivery address does not exist or does not belong to you.");
            }
        }
        Cart cart = cartRepository.findWithItemsByUserId(userId).orElse(null);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            errors.add("Cart is empty.");
        } else {
            for (CartItem item : cart.getItems()) {
                if (!item.getProduct().isActive()) {
                    errors.add("Product '" + item.getProduct().getName() + "' is inactive.");
                } else if (item.getProduct().getStockQuantity() < item.getQuantity()) {
                    errors.add("Product '" + item.getProduct().getName() + "' has insufficient stock.");
                }
            }
        }
        return Map.of(
                "eligible", errors.isEmpty(),
                "issues", errors
        );
    }

    @Tool(description = "Check if a payment method (e.g. 'CASH_ON_DELIVERY', 'CARD', 'UPI') is supported")
    public Map<String, Object> checkPaymentMethodAvailability(
            @ToolParam(description = "Payment method name, e.g. 'CASH_ON_DELIVERY', 'CARD', 'UPI'") String paymentMethod
    ) {
        String method = (paymentMethod != null) ? paymentMethod.trim().toUpperCase() : "CARD";
        List<String> supported = List.of("CARD", "CASH_ON_DELIVERY", "UPI");
        boolean available = supported.contains(method);
        return Map.of(
                "paymentMethod", method,
                "available", available,
                "supportedMethods", supported,
                "message", available ? "Payment method is available." : "Unsupported payment method. Please select from: " + supported
        );
    }

    @Tool(description = "Execute checkout and place the order. IMPORTANT: AI agent can ONLY place orders using 'CASH_ON_DELIVERY'.")
    public Map<String, Object> placeOrder(
            @ToolParam(description = "The UUID of the delivery address") String addressId,
            @ToolParam(description = "Payment method. MUST be 'CASH_ON_DELIVERY'") String paymentMethod
    ) {
        // Strict Guardrail: AI can ONLY place COD orders
        if (paymentMethod == null || !"CASH_ON_DELIVERY".equalsIgnoreCase(paymentMethod.trim())) {
            return Map.of(
                    "success", false,
                    "error", "Automated AI ordering is only permitted for 'CASH_ON_DELIVERY'. For Credit/Debit Card or UPI payments, please proceed to the checkout screen to pay securely through our payment gateway."
            );
        }
        UUID userId = SecurityUtils.getCurrentUserId();
        try {
            CheckoutRequestDTO dto = CheckoutRequestDTO.builder()
                    .addressId(UUID.fromString(addressId))
                    .paymentMethod("CASH_ON_DELIVERY")
                    .build();
            OrderResponseDTO order = orderService.checkout(userId, dto);
            return Map.of(
                    "success", true,
                    "orderId", order.getId().toString(),
                    "orderNumber", order.getOrderNumber(),
                    "status", order.getStatus().name(),
                    "totalAmount", order.getTotalAmount(),
                    "paymentMethod", "CASH_ON_DELIVERY",
                    "message", "Order placed successfully using Cash on Delivery! Order number: " + order.getOrderNumber()
            );
        } catch (Exception ex) {
            return Map.of("success", false, "error", ex.getMessage());
        }
    }

    @Tool(description = "Retrieve full details and items for an existing order by its UUID")
    public OrderResponseDTO getOrderDetails(
            @ToolParam(description = "The UUID of the order") String orderId
    ) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return orderService.getOrderById(userId, UUID.fromString(orderId), false);
    }

    @Tool(description = "Retrieve the customer's previous orders with order numbers, dates, and total amounts")
    public List<OrderSummaryResponseDTO> getOrderHistory(
            @ToolParam(description = "Number of recent orders to retrieve (default is 5, max is 10)") Integer limit
    ) {
        UUID userId = SecurityUtils.getCurrentUserId();
        int pageSize = (limit != null && limit > 0) ? Math.min(limit, 10) : 5;
        return orderService.getOrderHistory(userId, PageRequest.of(0, pageSize)).getContent();
    }

    @Tool(description = "Check the live fulfillment and shipping status of an existing order")
    public Map<String, Object> trackOrder(
            @ToolParam(description = "The UUID of the order to track") String orderId
    ) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Order order = orderRepository.findWithItemsById(UUID.fromString(orderId))
                .orElse(null);
        if (order == null || !order.getUser().getId().equals(userId)) {
            return Map.of("found", false, "message", "Order not found.");
        }
        return Map.of(
                "found", true,
                "orderNumber", order.getOrderNumber(),
                "status", order.getStatus().name(),
                "updatedAt", order.getUpdatedAt().toString(),
                "totalItems", order.getItems() != null ? order.getItems().size() : 0
        );
    }

    @Tool(description = "Request order cancellation, refund, or exchange. For undelivered orders: only CANCELLATION is allowed (initiates refund for prepaid). For delivered orders: REFUND or EXCHANGE can be requested within 7 days.")
    public Map<String, Object> cancelOrder(
            @ToolParam(description = "The UUID of the order") String orderId,
            @ToolParam(description = "Action type: 'CANCELLATION' (if undelivered), 'REFUND' (if delivered within 7 days), or 'EXCHANGE' (if delivered within 7 days)") String actionType,
            @ToolParam(description = "Reason for the cancellation, refund, or exchange") String reason
    ) {
        UUID userId = SecurityUtils.getCurrentUserId();

        try {
            com.aarav.ecommercebackend.order.dto.request.CancellationType type =
                    com.aarav.ecommercebackend.order.dto.request.CancellationType.valueOf(actionType.trim().toUpperCase());
            com.aarav.ecommercebackend.order.dto.request.CancelOrderRequestDTO dto =
                    com.aarav.ecommercebackend.order.dto.request.CancelOrderRequestDTO.builder()
                            .type(type)
                            .reason(reason)
                            .build();

            OrderResponseDTO updated = orderService.cancelOrReturnOrder(userId, UUID.fromString(orderId), dto);
            return Map.of(
                    "success", true,
                    "orderNumber", updated.getOrderNumber(),
                    "newStatus", updated.getStatus().name(),
                    "message", "Order action '" + type.name() + "' processed successfully."
            );
        } catch (Exception ex) {
            return Map.of(
                    "success", false,
                    "error", ex.getMessage()
            );
        }
    }

    @Tool(description = "Retrieve customer store policies: 'refund', 'exchange', or 'cancellation'")
    public PolicyResponseDTO getStorePolicy(
            @ToolParam(description = "Policy type: 'refund', 'exchange', or 'cancellation'") String policyType
    ) {
        return policyService.getPolicy(policyType);
    }
}
