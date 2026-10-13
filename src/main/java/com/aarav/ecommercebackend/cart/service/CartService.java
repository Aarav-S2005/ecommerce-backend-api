package com.aarav.ecommercebackend.cart.service;

import com.aarav.ecommercebackend.cart.dto.request.AddToCartRequestDTO;
import com.aarav.ecommercebackend.cart.dto.request.UpdateCartItemRequestDTO;
import com.aarav.ecommercebackend.cart.dto.response.CartItemResponseDTO;
import com.aarav.ecommercebackend.cart.dto.response.CartResponseDTO;
import com.aarav.ecommercebackend.cart.entity.Cart;
import com.aarav.ecommercebackend.cart.entity.CartItem;
import com.aarav.ecommercebackend.cart.repository.CartRepository;
import com.aarav.ecommercebackend.catalog.entity.Product;
import com.aarav.ecommercebackend.catalog.repository.ProductRepository;
import com.aarav.ecommercebackend.shared.exception.ResourceNotFoundException;
import com.aarav.ecommercebackend.user.entity.User;
import com.aarav.ecommercebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CartResponseDTO getCart(UUID userId) {
        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseGet(() -> getOrCreateCart(userId));

        return toCartResponseDTO(cart);
    }

    @Transactional
    public void addItemToCart(UUID userId, AddToCartRequestDTO request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product not found with id: " + request.getProductId());
        }

        if (product.getStockQuantity() <= 0) {
            throw new IllegalArgumentException("Product is out of stock");
        }

        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseGet(() -> getOrCreateCart(userId));

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.addItem(newItem);
        }

        cartRepository.save(cart);
    }

    @Transactional
    public void updateCartItemQuantity(UUID userId, UUID itemId, UpdateCartItemRequestDTO request) {
        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (request.getQuantity() == 0) {
            cart.removeItem(item);
            cartRepository.save(cart);
            return;
        }

        if (!item.getProduct().isActive()) {
            throw new ResourceNotFoundException("Product is no longer available");
        }

        if (item.getProduct().getStockQuantity() <= 0) {
            throw new IllegalArgumentException("Product is out of stock");
        }

        item.setQuantity(request.getQuantity());
        cartRepository.save(cart);
    }

    @Transactional
    public void removeItemFromCart(UUID userId, UUID itemId) {
        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        cart.removeItem(item);
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(UUID userId) {
        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));

        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                    return cartRepository.save(Cart.builder().user(user).build());
                });
    }

    private CartResponseDTO toCartResponseDTO(Cart cart) {
        List<CartItemResponseDTO> itemDTOs = cart.getItems() != null
                ? cart.getItems().stream()
                .filter(item -> item.getProduct().isActive())
                .map(this::toCartItemResponseDTO)
                .toList()
                : List.of();

        BigDecimal subtotal = itemDTOs.stream()
                .map(CartItemResponseDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = itemDTOs.stream()
                .mapToInt(CartItemResponseDTO::getQuantity)
                .sum();

        return CartResponseDTO.builder()
                .id(cart.getId())
                .items(itemDTOs)
                .subtotal(subtotal)
                .totalItems(totalItems)
                .build();
    }

    private CartItemResponseDTO toCartItemResponseDTO(CartItem item) {
        Product product = item.getProduct();
        String imageUrl = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            imageUrl = product.getImages().get(0).getImageUrl();
        }

        BigDecimal unitPrice = product.getPrice();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponseDTO.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(imageUrl)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .quantity(item.getQuantity())
                .build();
    }
}
