package com.aarav.ecommercebackend.catalog.service;

import com.aarav.ecommercebackend.catalog.dto.request.RecordViewRequestDTO;
import com.aarav.ecommercebackend.catalog.dto.response.GetAllProductsResponseDTO;
import com.aarav.ecommercebackend.catalog.dto.response.ProductResponseDTO;
import com.aarav.ecommercebackend.catalog.entity.Product;
import com.aarav.ecommercebackend.catalog.entity.ProductInteraction;
import com.aarav.ecommercebackend.catalog.repository.ProductInteractionRepository;
import com.aarav.ecommercebackend.catalog.repository.ProductRepository;
import com.aarav.ecommercebackend.shared.exception.ResourceNotFoundException;
import com.aarav.ecommercebackend.user.entity.User;
import com.aarav.ecommercebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductInteractionService {

    private final ProductInteractionRepository productInteractionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    @Transactional
    public void recordView(UUID userId, UUID productId, RecordViewRequestDTO reqBody) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        ProductInteraction interaction = productInteractionRepository
                .findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                    return ProductInteraction.builder()
                            .user(user)
                            .product(product)
                            .build();
                });

        long durationMs = reqBody != null && reqBody.getDurationMs() != null ? reqBody.getDurationMs() : 0L;
        interaction.recordView(durationMs);
        productInteractionRepository.save(interaction);
    }

    @Transactional
    public void likeProduct(UUID userId, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        ProductInteraction interaction = productInteractionRepository
                .findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                    return ProductInteraction.builder()
                            .user(user)
                            .product(product)
                            .build();
                });

        interaction.setLiked(true);
        if (!interaction.isHasSeen()) {
            OffsetDateTime now = OffsetDateTime.now();
            interaction.setHasSeen(true);
            if (interaction.getFirstSeenAt() == null) {
                interaction.setFirstSeenAt(now);
            }
            interaction.setLastSeenAt(now);
        }

        productInteractionRepository.save(interaction);
    }

    @Transactional
    public void dislikeProduct(UUID userId, UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        productInteractionRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(interaction -> {
                    if (interaction.isLiked()) {
                        interaction.setLiked(false);
                        productInteractionRepository.save(interaction);
                    }
                });
    }

    @Transactional(readOnly = true)
    public GetAllProductsResponseDTO getLikedProducts(UUID userId, Pageable pageable) {
        Page<ProductInteraction> interactions = productInteractionRepository
                .findLikedProductsByUserId(userId, pageable);

        List<ProductResponseDTO> productDTOs = interactions.getContent()
                .stream()
                .map(interaction -> productService.toProductResponseDTO(interaction.getProduct()))
                .toList();

        return GetAllProductsResponseDTO.builder()
                .products(productDTOs)
                .pageNumber(interactions.getNumber())
                .pageSize(interactions.getSize())
                .totalElements(interactions.getTotalElements())
                .totalPages(interactions.getTotalPages())
                .isLast(interactions.isLast())
                .build();
    }
}
