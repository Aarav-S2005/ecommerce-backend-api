package com.aarav.ecommercebackend.ai.tools;

import com.aarav.ecommercebackend.cart.dto.request.AddToCartRequestDTO;
import com.aarav.ecommercebackend.cart.dto.request.UpdateCartItemRequestDTO;
import com.aarav.ecommercebackend.cart.dto.response.CartResponseDTO;
import com.aarav.ecommercebackend.cart.entity.Cart;
import com.aarav.ecommercebackend.cart.entity.CartItem;
import com.aarav.ecommercebackend.cart.repository.CartRepository;
import com.aarav.ecommercebackend.cart.service.CartService;
import com.aarav.ecommercebackend.catalog.entity.Product;
import com.aarav.ecommercebackend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class CartTools {
    private final CartService cartService;
    private final CartRepository cartRepository;

    @Tool(description = "Retrieve the authenticated customer's shopping cart, items, quantities, and subtotal")
    public CartResponseDTO getCurrentCart() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return cartService.getCart(userId);
    }

    @Tool(description = "Add a product and quantity to the customer's shopping cart")
    public Map<String, Object> addItemToCart(
            @ToolParam(description = "The UUID of the product to add") String productId,
            @ToolParam(description = "The quantity to add (minimum 1, default 1)") Integer quantity
    ) {
        UUID userId = SecurityUtils.getCurrentUserId();
        int qty = (quantity != null && quantity > 0) ? quantity : 1;
        try {
            AddToCartRequestDTO dto = new AddToCartRequestDTO(UUID.fromString(productId), qty);
            cartService.addItemToCart(userId, dto);
            return Map.of(
                    "success", true,
                    "message", "Successfully added " + qty + " item(s) to your cart."
            );
        } catch (Exception ex) {
            return Map.of(
                    "success", false,
                    "error", ex.getMessage()
            );
        }
    }

    @Tool(description = "Update the quantity of an existing item in the cart. Setting quantity to 0 removes the item.")
    public Map<String, Object> updateCartItemQuantity(
            @ToolParam(description = "The UUID of the cart item (itemId)") String itemId,
            @ToolParam(description = "The new quantity (0 or greater)") Integer quantity
    ) {
        UUID userId = SecurityUtils.getCurrentUserId();
        int qty = (quantity != null && quantity >= 0) ? quantity : 0;
        try {
            UpdateCartItemRequestDTO dto = new UpdateCartItemRequestDTO(qty);
            cartService.updateCartItemQuantity(userId, UUID.fromString(itemId), dto);
            return Map.of(
                    "success", true,
                    "message", qty == 0 ? "Item removed from cart." : "Cart item quantity updated to " + qty + "."
            );
        } catch (Exception ex) {
            return Map.of(
                    "success", false,
                    "error", ex.getMessage()
            );
        }
    }

    @Tool(description = "Remove a specific item from the shopping cart by its item ID")
    public Map<String, Object> removeItemFromCart(
            @ToolParam(description = "The UUID of the cart item (itemId) to remove") String itemId
    ) {
        UUID userId = SecurityUtils.getCurrentUserId();
        try {
            cartService.removeItemFromCart(userId, UUID.fromString(itemId));
            return Map.of(
                    "success", true,
                    "message", "Item successfully removed from your cart."
            );
        } catch (Exception ex) {
            return Map.of(
                    "success", false,
                    "error", ex.getMessage()
            );
        }
    }

    @Tool(description = "Remove all items from the customer's shopping cart")
    public Map<String, Object> clearCart() {
        UUID userId = SecurityUtils.getCurrentUserId();
        try {
            cartService.clearCart(userId);
            return Map.of(
                    "success", true,
                    "message", "Shopping cart has been completely cleared."
            );
        } catch (Exception ex) {
            return Map.of(
                    "success", false,
                    "error", ex.getMessage()
            );
        }
    }

    @Tool(description = "Validate cart contents, product availability, active status, and real-time stock before proceeding to checkout")
    public Map<String, Object> validateCart() {
        UUID userId = SecurityUtils.getCurrentUserId();
        Optional<Cart> cartOpt = cartRepository.findWithItemsByUserId(userId);
        if (cartOpt.isEmpty() || cartOpt.get().getItems() == null || cartOpt.get().getItems().isEmpty()) {
            return Map.of(
                    "valid", false,
                    "reason", "Cart is empty. Please add items before checking out."
            );
        }
        Cart cart = cartOpt.get();
        List<String> issues = getIssues(cart);
        boolean isValid = issues.isEmpty();
        return Map.of(
                "valid", isValid,
                "issues", issues,
                "totalItems", cart.getItems().stream().mapToInt(CartItem::getQuantity).sum()
        );
    }

    private static List<String> getIssues(Cart cart) {
        List<String> issues = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.isActive()) {
                issues.add("Product '" + product.getName() + "' is no longer available.");
            } else if (product.getStockQuantity() <= 0) {
                issues.add("Product '" + product.getName() + "' is currently out of stock.");
            } else if (product.getStockQuantity() < item.getQuantity()) {
                issues.add("Product '" + product.getName() + "' only has " + product.getStockQuantity() + " available, but your cart has " + item.getQuantity() + ".");
            }
        }
        return issues;
    }
}
