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
            "return", PolicyResponseDTO.builder()
                    .policyType("return")
                    .title("Return Policy")
                    .description("Delivered items can be returned within 14 days of delivery if they are in their original packaging and unused condition.")
                    .timeframe("14 days from delivery")
                    .applicableForCashOnDelivery(true)
                    .terms(List.of(
                            "Order status must be DELIVERED to request a return.",
                            "Item must remain unused, unwashed, and with all original tags attached.",
                            "Doorstep quality inspection will be conducted during reverse pickup.",
                            "Cash on Delivery orders require bank details verification during return pickup."
                    ))
                    .build(),

            "refund", PolicyResponseDTO.builder()
                    .policyType("refund")
                    .title("Refund Policy")
                    .description("Online payments are automatically refunded to the source payment method upon cancellation or verified return. Cash on Delivery orders are not eligible for direct instant refunds without physical return inspection.")
                    .timeframe("5-7 business days post-verification")
                    .applicableForCashOnDelivery(false)
                    .terms(List.of(
                            "Credit/Debit cards and UPI refunds are credited within 5-7 business days.",
                            "Cash on Delivery orders cannot receive instant electronic refunds before delivery.",
                            "For delivered COD items, refunds are processed via NEFT bank transfer after warehouse verification.",
                            "Shipping and convenience charges are non-refundable."
                    ))
                    .build(),

            "exchange", PolicyResponseDTO.builder()
                    .policyType("exchange")
                    .title("Exchange Policy")
                    .description("Delivered items can be exchanged for alternative sizes, colors, or replacement of defective units within 7 days of delivery.")
                    .timeframe("7 days from delivery")
                    .applicableForCashOnDelivery(true)
                    .terms(List.of(
                            "Order must be DELIVERED to request an exchange.",
                            "Exchanges are subject to inventory availability in the warehouse.",
                            "If replacement stock is unavailable, a full refund will be offered.",
                            "Reverse pickup is free of charge for damaged or defective items."
                    ))
                    .build(),

            "cancellation", PolicyResponseDTO.builder()
                    .policyType("cancellation")
                    .title("Cancellation Policy")
                    .description("Orders can be cancelled at any time before delivery (while PENDING or CONFIRMED). If paid online, a full refund is initiated automatically. Once delivered, cancellations are closed and return/exchange policies apply.")
                    .timeframe("Anytime prior to delivery")
                    .applicableForCashOnDelivery(true)
                    .terms(List.of(
                            "Allowed freely while the order is in PENDING or CONFIRMED state.",
                            "Online prepaid orders receive an automatic full refund upon cancellation.",
                            "Cash on Delivery orders can be cancelled with zero penalty prior to dispatch.",
                            "Delivered orders cannot be cancelled; please use Return or Exchange instead."
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
            throw new ResourceNotFoundException("Policy not found for: " + policyType + ". Available policies: return, refund, exchange, cancellation.");
        }
        return policy;
    }

    public List<PolicyResponseDTO> getAllPolicies() {
        return List.copyOf(POLICIES.values());
    }
}
