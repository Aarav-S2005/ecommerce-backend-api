package com.aarav.ecommercebackend.order.dto.request;

import com.aarav.ecommercebackend.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusDTO {

    @NotNull(message = "Status is required")
    private OrderStatus status;
}
