package com.aarav.ecommercebackend.ai.dto;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageContextDTO {
    private String pageType;
    private UUID productId;
    private UUID categoryId;
    private String searchQuery;
}
