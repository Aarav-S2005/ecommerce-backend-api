package com.aarav.ecommercebackend.cart.dto.response;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponseDTO implements Serializable {
    private UUID id;
    private UUID productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Integer quantity;
}
