package com.aarav.ecommercebackend.order.controller;

import com.aarav.ecommercebackend.order.dto.response.PolicyResponseDTO;
import com.aarav.ecommercebackend.order.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping("/{policy}")
    public ResponseEntity<PolicyResponseDTO> getPolicy(@PathVariable("policy") String policy) {
        return ResponseEntity.ok(policyService.getPolicy(policy));
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<PolicyResponseDTO>> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAllPolicies());
    }
}
