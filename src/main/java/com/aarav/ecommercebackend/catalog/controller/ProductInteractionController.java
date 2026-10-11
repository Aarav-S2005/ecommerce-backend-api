package com.aarav.ecommercebackend.catalog.controller;

import com.aarav.ecommercebackend.catalog.dto.request.RecordViewRequestDTO;
import com.aarav.ecommercebackend.catalog.dto.response.GetAllProductsResponseDTO;
import com.aarav.ecommercebackend.catalog.service.ProductInteractionService;
import com.aarav.ecommercebackend.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/catalog/interaction")
@RequiredArgsConstructor
public class ProductInteractionController {

    private final ProductInteractionService productInteractionService;

    @PostMapping("/{productId}/view")
    public void viewProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("productId") UUID productId,
            @Valid @RequestBody RecordViewRequestDTO reqBody
    ) {
        productInteractionService.recordView(principal.getId(), productId, reqBody);
    }

    @PostMapping("/{productId}/like")
    public void likeProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("productId") UUID productId
    ) {
        productInteractionService.likeProduct(principal.getId(), productId);
    }

    @DeleteMapping("/{productId}/like")
    public void dislikeProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("productId") UUID productId
    ) {
        productInteractionService.dislikeProduct(principal.getId(), productId);
    }

    @GetMapping("/liked")
    public ResponseEntity<GetAllProductsResponseDTO> getLikedProducts(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(productInteractionService.getLikedProducts(principal.getId(), pageable));
    }
}
