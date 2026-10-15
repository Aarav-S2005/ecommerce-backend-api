package com.aarav.ecommercebackend.ai.tools;

import com.aarav.ecommercebackend.catalog.dto.response.ProductResponseDTO;
import com.aarav.ecommercebackend.catalog.entity.Product;
import com.aarav.ecommercebackend.catalog.repository.ProductRepository;
import com.aarav.ecommercebackend.catalog.service.ProductService;
import com.aarav.ecommercebackend.order.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CatalogTools {
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ReviewRepository reviewRepository;

    @Tool(description = "Search for products in the catalog using keywords like product name or description")
    public List<Map<String, Object>> searchProducts(
            @ToolParam(description = "Search keywords, e.g. 'running shoes', 'laptop'") String keyword,
            @ToolParam(description = "Max items to return (default is 5, max is 10)") Integer limit
    ) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        int pageSize = (limit != null && limit > 0) ? Math.min(limit, 10) : 5;
        return productRepository.searchProducts(keyword.trim(), PageRequest.of(0, pageSize))
                .getContent()
                .stream()
                .map(this::toProductSummary)
                .toList();
    }

    @Tool(description = "Retrieve comprehensive details for a specific product including description, brand, and category")
    public ProductResponseDTO getProductDetails(
            @ToolParam(description = "The UUID of the product") String productId
    ) {
        return productService.getProductById(UUID.fromString(productId));
    }

    @Tool(description = "Retrieve specifications, brand, category, and inventory status for a product")
    public Map<String, Object> getProductVariants(
            @ToolParam(description = "The UUID of the product") String productId
    ) {
        Product p = productRepository.findById(UUID.fromString(productId)).orElse(null);
        if (p == null || !p.isActive()) {
            return Map.of("found", false, "message", "Product not found or inactive");
        }
        return Map.of(
                "productId", p.getId().toString(),
                "name", p.getName(),
                "brand", p.getBrand() != null ? p.getBrand().getName() : "N/A",
                "category", p.getCategory() != null ? p.getCategory().getName() : "N/A",
                "availableQuantity", p.getStockQuantity(),
                "inStock", p.getStockQuantity() > 0
        );
    }

    @Tool(description = "Check real-time stock availability and verify if requested quantity can be ordered")
    public Map<String, Object> checkProductAvailability(
            @ToolParam(description = "The UUID of the product") String productId,
            @ToolParam(description = "Desired quantity to purchase (default is 1)") Integer quantity
    ) {
        Product p = productRepository.findById(UUID.fromString(productId)).orElse(null);
        if (p == null || !p.isActive()) {
            return Map.of("available", false, "reason", "Product does not exist or is inactive");
        }
        int requestedQty = (quantity != null && quantity > 0) ? quantity : 1;
        boolean canFulfill = p.getStockQuantity() >= requestedQty;
        return Map.of(
                "productId", p.getId().toString(),
                "productName", p.getName(),
                "available", canFulfill,
                "stockQuantity", p.getStockQuantity(),
                "reason", canFulfill ? "In stock" : "Insufficient stock available"
        );
    }

    @Tool(description = "Retrieve the current price of a specific product")
    public Map<String, Object> getProductPrice(
            @ToolParam(description = "The UUID of the product") String productId
    ) {
        Product p = productRepository.findById(UUID.fromString(productId)).orElse(null);
        if (p == null || !p.isActive()) {
            return Map.of("found", false, "message", "Product not found");
        }
        return Map.of(
                "productId", p.getId().toString(),
                "productName", p.getName(),
                "price", p.getPrice(),
                "currency", "USD"
        );
    }

    @Tool(description = "Retrieve customer ratings and recent reviews for a product")
    public Map<String, Object> getProductRatingsAndReviews(
            @ToolParam(description = "The UUID of the product") String productId,
            @ToolParam(description = "Max reviews to return (default is 3, max is 5)") Integer limit
    ) {
        UUID id = UUID.fromString(productId);
        int pageSize = (limit != null && limit > 0) ? Math.min(limit, 5) : 3;
        Double avgRating = reviewRepository.findAverageRatingByProductId(id);
        long totalReviews = reviewRepository.countByProductId(id);
        List<Map<String, Object>> reviews = reviewRepository.findByProductId(id, PageRequest.of(0, pageSize))
                .getContent()
                .stream()
                .map(r -> Map.<String, Object>of(
                        "rating", r.getRating(),
                        "title", r.getTitle() != null ? r.getTitle() : "",
                        "comment", r.getComment() != null ? r.getComment() : ""
                ))
                .toList();
        return Map.of(
                "productId", productId,
                "averageRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0,
                "totalReviews", totalReviews,
                "reviews", reviews
        );
    }

    @Tool(description = "Retrieve similar or alternative products in the same category or brand")
    public List<Map<String, Object>> getRelatedProducts(
            @ToolParam(description = "The UUID of the base product") String productId,
            @ToolParam(description = "Max items to return (default is 4, max is 8)") Integer limit
    ) {
        Product p = productRepository.findById(UUID.fromString(productId)).orElse(null);
        if (p == null) {
            return List.of();
        }
        int pageSize = (limit != null && limit > 0) ? Math.min(limit, 8) : 4;
        UUID categoryId = p.getCategory() != null ? p.getCategory().getId() : null;
        UUID brandId = p.getBrand() != null ? p.getBrand().getId() : null;
        return productRepository.findRelatedProducts(p.getId(), categoryId, brandId, PageRequest.of(0, pageSize))
                .getContent()
                .stream()
                .map(this::toProductSummary)
                .toList();
    }

    @Tool(description = "Filter catalog products within a specific price budget range")
    public List<Map<String, Object>> filterProducts(
            @ToolParam(description = "Minimum price in USD (optional)") Double minPrice,
            @ToolParam(description = "Maximum price in USD (optional)") Double maxPrice,
            @ToolParam(description = "Max items to return (default is 5, max is 10)") Integer limit
    ) {
        int pageSize = (limit != null && limit > 0) ? Math.min(limit, 10) : 5;
        BigDecimal min = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal max = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;
        return productRepository.filterByPriceRange(min, max, PageRequest.of(0, pageSize))
                .getContent()
                .stream()
                .map(this::toProductSummary)
                .toList();
    }

    @Tool(description = "Compare 2 or more products side by side by price, brand, category, and specifications")
    public List<Map<String, Object>> compareProducts(
            @ToolParam(description = "List of product UUIDs to compare") List<String> productIds
    ) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        List<UUID> uuids = productIds.stream().map(UUID::fromString).toList();
        return productRepository.findAllById(uuids)
                .stream()
                .filter(Product::isActive)
                .map(p -> Map.<String, Object>of(
                        "id", p.getId().toString(),
                        "name", p.getName(),
                        "price", p.getPrice(),
                        "brand", p.getBrand() != null ? p.getBrand().getName() : "N/A",
                        "category", p.getCategory() != null ? p.getCategory().getName() : "N/A",
                        "stockQuantity", p.getStockQuantity(),
                        "inStock", p.getStockQuantity() > 0
                ))
                .toList();
    }
    


    private Map<String, Object> toProductSummary(Product p) {
        return Map.of(
                "id", p.getId().toString(),
                "name", p.getName(),
                "price", p.getPrice(),
                "brand", p.getBrand() != null ? p.getBrand().getName() : "Unknown",
                "category", p.getCategory() != null ? p.getCategory().getName() : "Unknown",
                "inStock", p.getStockQuantity() > 0
        );
    }
}
