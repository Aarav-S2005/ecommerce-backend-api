package com.aarav.ecommercebackend.cart.dto.response;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDTO implements Serializable {
    private UUID id;
    private List<CartItemResponseDTO> items;
    private BigDecimal subtotal;
    private Integer totalItems;
}
