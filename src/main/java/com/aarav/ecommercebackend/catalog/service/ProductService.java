package com.aarav.ecommercebackend.catalog.service;

import com.aarav.ecommercebackend.catalog.dto.request.ProductRequestDTO;
import com.aarav.ecommercebackend.catalog.dto.response.*;
import com.aarav.ecommercebackend.catalog.entity.Brand;
import com.aarav.ecommercebackend.catalog.entity.Category;
import com.aarav.ecommercebackend.catalog.entity.Product;
import com.aarav.ecommercebackend.catalog.entity.ProductImage;
import com.aarav.ecommercebackend.catalog.repository.BrandRepository;
import com.aarav.ecommercebackend.catalog.repository.CategoryRepository;
import com.aarav.ecommercebackend.catalog.repository.ProductRepository;
import com.aarav.ecommercebackend.shared.exception.DuplicateResourceException;
import com.aarav.ecommercebackend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    @Cacheable(
            value = "product_pages",
            key = "'p_' + #pageable.pageNumber + '_s_' + #pageable.pageSize + '_sort_' + #pageable.sort.toString() + '_cat_' + #categoryId + '_b_' + #brandId"
    )
    public GetAllProductsResponseDTO getAllProducts(UUID categoryId, UUID brandId, Pageable pageable) {
        Page<Product> productPage;

        if (categoryId != null && brandId != null) {
            productPage = productRepository.findByCategoryIdAndBrandIdAndActiveTrue(categoryId, brandId, pageable);
        } else if (categoryId != null) {
            productPage = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        } else if (brandId != null) {
            productPage = productRepository.findByBrandIdAndActiveTrue(brandId, pageable);
        } else {
            productPage = productRepository.findByActiveTrue(pageable);
        }

        List<ProductResponseDTO> dtos = productPage.getContent()
                .stream()
                .map(this::toProductResponseDTO)
                .toList();

        return GetAllProductsResponseDTO.builder()
                .products(dtos)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .isLast(productPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#productId")
    public ProductResponseDTO getProductById(UUID productId) {
        Product product = productRepository.findWithDetailsById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        return toProductResponseDTO(product);
    }

    @Transactional
    @CacheEvict(value = "product_pages", allEntries = true)
    public void addProduct(ProductRequestDTO request) {
        String trimmedName = request.getName().trim();
        if (productRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new DuplicateResourceException("Product already exists with name: " + trimmedName);
        }

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        boolean isActive = request.getActive() != null ? request.getActive() : true;

        Product product = Product.builder()
                .name(trimmedName)
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .brand(brand)
                .category(category)
                .active(isActive)
                .build();

        if (request.getImageUrls() != null) {
            int order = 0;
            for (String url : request.getImageUrls()) {
                if (url != null && !url.isBlank()) {
                    ProductImage image = ProductImage.builder()
                            .imageUrl(url.trim())
                            .displayOrder(order++)
                            .build();
                    product.addImage(image);
                }
            }
        }

        productRepository.save(product);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "product_pages", allEntries = true)
    })
    public void updateProduct(UUID productId, ProductRequestDTO request) {
        Product product = productRepository.findWithDetailsById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        String trimmedName = request.getName().trim();
        if (productRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, productId)) {
            throw new DuplicateResourceException("Another product already exists with name: " + trimmedName);
        }

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        product.setName(trimmedName);
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setBrand(brand);
        product.setCategory(category);

        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        if (request.getImageUrls() != null) {
            product.getImages().clear();
            int order = 0;
            for (String url : request.getImageUrls()) {
                if (url != null && !url.isBlank()) {
                    ProductImage image = ProductImage.builder()
                            .imageUrl(url.trim())
                            .displayOrder(order++)
                            .build();
                    product.addImage(image);
                }
            }
        }

        productRepository.save(product);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "product_pages", allEntries = true)
    })
    public void deleteProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        productRepository.delete(product);
    }

    private ProductResponseDTO toProductResponseDTO(Product product) {
        List<ProductImageResponseDTO> imageDTOs = product.getImages() != null
                ? product.getImages().stream()
                .map(img -> ProductImageResponseDTO.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .displayOrder(img.getDisplayOrder())
                        .build())
                .toList()
                : List.of();

        BrandResponseDTO brandDTO = product.getBrand() != null
                ? BrandResponseDTO.builder()
                .id(product.getBrand().getId())
                .name(product.getBrand().getName())
                .description(product.getBrand().getDescription())
                .active(product.getBrand().isActive())
                .createdAt(product.getBrand().getCreatedAt())
                .build()
                : null;

        CategoryResponseDTO categoryDTO = product.getCategory() != null
                ? CategoryResponseDTO.builder()
                .id(product.getCategory().getId())
                .name(product.getCategory().getName())
                .description(product.getCategory().getDescription())
                .active(product.getCategory().isActive())
                .createdAt(product.getCategory().getCreatedAt())
                .build()
                : null;

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .brand(brandDTO)
                .category(categoryDTO)
                .images(imageDTOs)
                .build();
    }
}
