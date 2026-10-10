package com.aarav.ecommercebackend.catalog.controller;

import com.aarav.ecommercebackend.catalog.dto.request.ProductRequestDTO;
import com.aarav.ecommercebackend.catalog.dto.response.GetAllProductsResponseDTO;
import com.aarav.ecommercebackend.catalog.dto.response.ProductResponseDTO;
import com.aarav.ecommercebackend.catalog.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/catalog/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping({"", "/"})
    public ResponseEntity<GetAllProductsResponseDTO> getAllProducts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID brandId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(productService.getAllProducts(categoryId, brandId, pageable));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping({"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    public void addProduct(@RequestBody @Valid ProductRequestDTO request) {
        productService.addProduct(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}")
    public void updateProduct(
            @PathVariable UUID productId,
            @RequestBody @Valid ProductRequestDTO request
    ) {
        productService.updateProduct(productId, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}
