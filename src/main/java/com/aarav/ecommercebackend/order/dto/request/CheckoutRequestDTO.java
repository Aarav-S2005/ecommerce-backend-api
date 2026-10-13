package com.aarav.ecommercebackend.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequestDTO {

    @NotNull(message = "Address ID is required")
    private UUID addressId;

    @Builder.Default
    private String paymentMethod = "CARD";
}
