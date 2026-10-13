package com.aarav.ecommercebackend.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderRequestDTO {

    @NotNull(message = "Action type is required (CANCELLATION, RETURN, REFUND, EXCHANGE)")
    private CancellationType type;

    private String reason;
}
