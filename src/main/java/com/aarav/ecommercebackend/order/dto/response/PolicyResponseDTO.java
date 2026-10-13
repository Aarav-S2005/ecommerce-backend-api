package com.aarav.ecommercebackend.order.dto.response;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyResponseDTO implements Serializable {
    private String policyType;
    private String title;
    private String description;
    private List<String> terms;
    private String timeframe;
    private boolean applicableForCashOnDelivery;
}
