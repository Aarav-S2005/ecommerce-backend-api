package com.aarav.ecommercebackend.catalog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAffinityDTO implements Serializable {

    private UUID categoryId;
    private String categoryName;
    private double affinityScore;
    private int totalViews;
    private long totalDurationMs;
    private int likedProductsCount;
}
