package com.aarav.ecommercebackend.catalog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllProductsResponseDTO implements Serializable {
    private List<ProductResponseDTO> products;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isLast;
}
