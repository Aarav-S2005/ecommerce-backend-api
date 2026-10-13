package com.aarav.ecommercebackend.cart.controller;

import com.aarav.ecommercebackend.cart.dto.request.AddToCartRequestDTO;
import com.aarav.ecommercebackend.cart.dto.request.UpdateCartItemRequestDTO;
import com.aarav.ecommercebackend.cart.dto.response.CartResponseDTO;
import com.aarav.ecommercebackend.cart.service.CartService;
import com.aarav.ecommercebackend.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/")
    public ResponseEntity<CartResponseDTO> getCart(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(cartService.getCart(principal.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<Void> addItemToCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddToCartRequestDTO request
    ) {
        cartService.addItemToCart(principal.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/items/{itemId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<Void> updateCartItemQuantity(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequestDTO request
    ) {
        cartService.updateCartItemQuantity(principal.getId(), itemId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID itemId
    ) {
        cartService.removeItemFromCart(principal.getId(), itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping({"", "/"})
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        cartService.clearCart(principal.getId());
        return ResponseEntity.noContent().build();
    }
}
