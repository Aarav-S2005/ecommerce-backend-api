package com.aarav.ecommercebackend.catalog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO implements Serializable {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private BrandResponseDTO brand;
    private CategoryResponseDTO category;
    private List<ProductImageResponseDTO> images;
}
