package com.aarav.ecommercebackend.order.service;

import com.aarav.ecommercebackend.catalog.entity.Product;
import com.aarav.ecommercebackend.catalog.repository.ProductRepository;
import com.aarav.ecommercebackend.order.dto.request.CreateReviewRequestDTO;
import com.aarav.ecommercebackend.order.dto.response.ProductReviewsResponseDTO;
import com.aarav.ecommercebackend.order.dto.response.ReviewResponseDTO;
import com.aarav.ecommercebackend.order.entity.Order;
import com.aarav.ecommercebackend.order.entity.OrderStatus;
import com.aarav.ecommercebackend.order.entity.Review;
import com.aarav.ecommercebackend.order.repository.OrderRepository;
import com.aarav.ecommercebackend.order.repository.ReviewRepository;
import com.aarav.ecommercebackend.shared.exception.DuplicateResourceException;
import com.aarav.ecommercebackend.shared.exception.ResourceNotFoundException;
import com.aarav.ecommercebackend.user.entity.User;
import com.aarav.ecommercebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponseDTO createReview(UUID userId, UUID orderId, UUID productId, CreateReviewRequestDTO request) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Reviews can only be submitted for delivered orders.");
        }

        boolean hasProduct = order.getItems() != null && order.getItems().stream()
                .anyMatch(i -> i.getProduct().getId().equals(productId));

        if (!hasProduct) {
            throw new IllegalArgumentException("Product was not purchased as part of this order.");
        }

        if (reviewRepository.existsByUserIdAndProductIdAndOrderId(userId, productId, orderId)) {
            throw new DuplicateResourceException("You have already reviewed this product for this order.");
        }

        User user = userRepository.getReferenceById(userId);
        Product product = productRepository.getReferenceById(productId);

        Review review = Review.builder()
                .user(user)
                .product(product)
                .order(order)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        return toReviewResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public ProductReviewsResponseDTO getProductReviews(UUID productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageable);
        Double avgRating = reviewRepository.findAverageRatingByProductId(productId);
        long totalCount = reviewRepository.countByProductId(productId);

        List<ReviewResponseDTO> reviewDTOs = reviewPage.getContent().stream()
                .map(this::toReviewResponseDTO)
                .toList();

        return ProductReviewsResponseDTO.builder()
                .productId(productId)
                .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
                .totalReviews(totalCount)
                .reviews(reviewDTOs)
                .pageNumber(reviewPage.getNumber())
                .pageSize(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .isLast(reviewPage.isLast())
                .build();
    }

    private ReviewResponseDTO toReviewResponseDTO(Review review) {
        String reviewerName = "Anonymous";
        if (review.getUser() != null) {
            String firstName = review.getUser().getFirstName() != null ? review.getUser().getFirstName() : "";
            String lastName = review.getUser().getLastName() != null ? review.getUser().getLastName() : "";
            reviewerName = (firstName + " " + lastName).trim();
            if (reviewerName.isEmpty() && review.getUser().getEmail() != null) {
                reviewerName = review.getUser().getEmail().split("@")[0];
            }
        }

        return ReviewResponseDTO.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .orderId(review.getOrder().getId())
                .reviewerName(reviewerName)
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
