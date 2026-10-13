package com.aarav.ecommercebackend.order.dto.response;

import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO implements Serializable {
    private UUID id;
    private UUID productId;
    private UUID orderId;
    private String reviewerName;
    private Short rating;
    private String title;
    private String comment;
    private OffsetDateTime createdAt;
}
