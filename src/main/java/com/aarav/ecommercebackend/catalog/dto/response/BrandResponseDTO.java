package com.aarav.ecommercebackend.catalog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private boolean active;
    private OffsetDateTime createdAt;
}
