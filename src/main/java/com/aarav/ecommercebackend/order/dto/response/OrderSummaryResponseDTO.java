package com.aarav.ecommercebackend.order.dto.response;

import com.aarav.ecommercebackend.order.entity.OrderStatus;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryResponseDTO implements Serializable {
    private UUID id;
    private String orderNumber;
    private OrderStatus status;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private int totalItems;
    private OffsetDateTime createdAt;
}
