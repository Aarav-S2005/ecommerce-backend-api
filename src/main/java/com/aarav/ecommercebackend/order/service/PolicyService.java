package com.aarav.ecommercebackend.order.service;

import com.aarav.ecommercebackend.order.dto.response.PolicyResponseDTO;
import com.aarav.ecommercebackend.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PolicyService {

    private static final Map<String, PolicyResponseDTO> POLICIES = Map.of(
            "refund", PolicyResponseDTO.builder()
                    .policyType("refund")
                    .title("Refund Policy")
                    .description("If your order is not yet delivered, prepaid orders will receive an automatic refund upon cancellation. For delivered orders, refunds can be requested within 7 days of delivery.")
                    .timeframe("Within 7 days of delivery (or immediate for undelivered cancellations)")
                    .applicableForCashOnDelivery(false)
                    .terms(List.of(
                            "Undelivered prepaid orders are fully refunded immediately upon cancellation.",
                            "Delivered orders are eligible for refund within 7 days of delivery.",
                            "COD orders are cancelled without refund if undelivered."
                    ))
                    .build(),

            "exchange", PolicyResponseDTO.builder()
                    .policyType("exchange")
                    .title("Exchange Policy")
                    .description("Delivered items can be exchanged for alternative sizes, colors, or replacement units within 7 days of delivery.")
                    .timeframe("Within 7 days of delivery")
                    .applicableForCashOnDelivery(true)
                    .terms(List.of(
                            "Exchanges are only permitted after order is DELIVERED.",
                            "Requests must be submitted within 7 days of delivery.",
                            "Subject to product inventory availability."
                    ))
                    .build(),

            "cancellation", PolicyResponseDTO.builder()
                    .policyType("cancellation")
                    .title("Cancellation Policy")
                    .description("Orders can be cancelled anytime before delivery. Cash on Delivery orders are cancelled with zero payment collected. Prepaid orders are cancelled and refunded.")
                    .timeframe("Anytime prior to delivery")
                    .applicableForCashOnDelivery(true)
                    .terms(List.of(
                            "Allowed freely while order is PENDING, CONFIRMED, or SHIPPED.",
                            "COD orders are cancelled with zero fees.",
                            "Prepaid orders trigger an automatic refund.",
                            "Delivered orders cannot be cancelled (use Refund or Exchange instead)."
                    ))
                    .build(),

            "return", PolicyResponseDTO.builder()
                    .policyType("return")
                    .title("Return & Refund Policy")
                    .description("Returns are processed directly as Refunds within 7 days of delivery.")
                    .timeframe("Within 7 days of delivery")
                    .applicableForCashOnDelivery(false)
                    .terms(List.of(
                            "Returns for money back are handled as Refunds.",
                            "Request must be made within 7 days of delivery."
                    ))
                    .build()
    );

    public PolicyResponseDTO getPolicy(String policyType) {
        if (policyType == null) {
            throw new ResourceNotFoundException("Policy type is required");
        }

        String normalized = policyType.trim().toLowerCase(Locale.ROOT);
        PolicyResponseDTO policy = POLICIES.get(normalized);
        if (policy == null) {
            throw new ResourceNotFoundException("Policy not found for: " + policyType + ". Available policies: refund, exchange, cancellation.");
        }
        return policy;
    }

    public List<PolicyResponseDTO> getAllPolicies() {
        return List.copyOf(POLICIES.values());
    }
}
