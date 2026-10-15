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
public class BrandAffinityDTO implements Serializable {

    private UUID brandId;
    private String brandName;
    private double affinityScore;
    private int totalViews;
    private long totalDurationMs;
    private int likedProductsCount;
}
