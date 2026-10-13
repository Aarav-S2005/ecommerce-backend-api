package com.aarav.ecommercebackend.order.dto.response;

import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewsResponseDTO implements Serializable {
    private UUID productId;
    private double averageRating;
    private long totalReviews;
    private List<ReviewResponseDTO> reviews;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isLast;
}
