package com.aarav.ecommercebackend.catalog.controller;

import com.aarav.ecommercebackend.catalog.dto.request.CategoryRequestDTO;
import com.aarav.ecommercebackend.catalog.dto.response.CategoryResponseDTO;
import com.aarav.ecommercebackend.catalog.dto.response.GetAllCategoriesResponseDTO;
import com.aarav.ecommercebackend.catalog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<GetAllCategoriesResponseDTO> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> getCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public void addCategory(@RequestBody @Valid CategoryRequestDTO request) {
        categoryService.addCategory(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/categories/{categoryId}")
    public void updateCategory(@PathVariable UUID categoryId, @RequestBody @Valid CategoryRequestDTO request) {
        categoryService.updateCategory(categoryId, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
