package com.aarav.ecommercebackend.catalog.service;

import com.aarav.ecommercebackend.catalog.dto.request.CategoryRequestDTO;
import com.aarav.ecommercebackend.catalog.dto.response.CategoryResponseDTO;
import com.aarav.ecommercebackend.catalog.dto.response.GetAllCategoriesResponseDTO;
import com.aarav.ecommercebackend.catalog.entity.Category;
import com.aarav.ecommercebackend.catalog.repository.CategoryRepository;
import com.aarav.ecommercebackend.catalog.repository.ProductRepository;
import com.aarav.ecommercebackend.shared.exception.DuplicateResourceException;
import com.aarav.ecommercebackend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public GetAllCategoriesResponseDTO getAllCategories() {
        List<CategoryResponseDTO> categoryList = categoryRepository.findByActiveTrue()
                .stream()
                .map(this::toResponseDTO)
                .toList();

        return new GetAllCategoriesResponseDTO(categoryList);
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        return toResponseDTO(category);
    }

    @Transactional
    public void addCategory(CategoryRequestDTO request) {
        String trimmedName = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new DuplicateResourceException("Category already exists with name: " + trimmedName);
        }

        boolean isActive = request.getActive() != null ? request.getActive() : true;

        Category category = Category.builder()
                .name(trimmedName)
                .description(request.getDescription())
                .active(isActive)
                .build();

        categoryRepository.save(category);
    }

    @Transactional
    public void updateCategory(UUID categoryId, CategoryRequestDTO request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        String trimmedName = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, categoryId)) {
            throw new DuplicateResourceException("Another category already exists with name: " + trimmedName);
        }

        category.setName(trimmedName);
        category.setDescription(request.getDescription());
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        if (productRepository.existsByCategoryId(categoryId)) {
            throw new DuplicateResourceException("Cannot delete category because products are associated with it. Disable the category instead.");
        }

        categoryRepository.delete(category);
    }

    private CategoryResponseDTO toResponseDTO(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
