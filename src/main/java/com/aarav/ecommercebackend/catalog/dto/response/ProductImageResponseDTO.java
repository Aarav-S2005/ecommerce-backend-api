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
public class ProductImageResponseDTO implements Serializable {
    private UUID id;
    private String imageUrl;
    private Integer displayOrder;
}
