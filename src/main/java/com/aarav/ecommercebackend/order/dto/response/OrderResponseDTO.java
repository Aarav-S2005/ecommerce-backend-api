package com.aarav.ecommercebackend.order.dto.response;

import com.aarav.ecommercebackend.order.entity.OrderStatus;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO implements Serializable {
    private UUID id;
    private String orderNumber;
    private OrderStatus status;
    private String paymentMethod;
    private String cancellationType;
    private String cancellationReason;
    private BigDecimal totalAmount;
    private Map<String, Object> shippingAddress;
    private List<OrderItemResponseDTO> items;
    private OffsetDateTime createdAt;
}
