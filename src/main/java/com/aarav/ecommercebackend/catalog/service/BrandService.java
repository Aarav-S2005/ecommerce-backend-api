package com.aarav.ecommercebackend.catalog.service;

import com.aarav.ecommercebackend.catalog.dto.request.BrandRequestDTO;
import com.aarav.ecommercebackend.catalog.dto.response.BrandResponseDTO;
import com.aarav.ecommercebackend.catalog.dto.response.GetAllBrandsResponseDTO;
import com.aarav.ecommercebackend.catalog.entity.Brand;
import com.aarav.ecommercebackend.catalog.repository.BrandRepository;
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
public class BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public GetAllBrandsResponseDTO getAllBrands() {
        List<BrandResponseDTO> brandList = brandRepository.findByActiveTrue()
                .stream()
                .map(this::toResponseDTO)
                .toList();

        return new GetAllBrandsResponseDTO(brandList);
    }

    @Transactional(readOnly = true)
    public BrandResponseDTO getBrandById(UUID brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + brandId));
        return toResponseDTO(brand);
    }

    @Transactional
    public void addBrand(BrandRequestDTO request) {
        String trimmedName = request.getName().trim();
        if (brandRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new DuplicateResourceException("Brand already exists with name: " + trimmedName);
        }

        boolean isActive = request.getActive() != null ? request.getActive() : true;

        Brand brand = Brand.builder()
                .name(trimmedName)
                .description(request.getDescription())
                .active(isActive)
                .build();

        brandRepository.save(brand);
    }

    @Transactional
    public void updateBrand(UUID brandId, BrandRequestDTO request) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + brandId));

        String trimmedName = request.getName().trim();
        if (brandRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, brandId)) {
            throw new DuplicateResourceException("Another brand already exists with name: " + trimmedName);
        }

        brand.setName(trimmedName);
        brand.setDescription(request.getDescription());
        if (request.getActive() != null) {
            brand.setActive(request.getActive());
        }

        brandRepository.save(brand);
    }

    @Transactional
    public void deleteBrand(UUID brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + brandId));

        if (productRepository.existsByBrandId(brandId)) {
            throw new DuplicateResourceException("Cannot delete brand because products are associated with it. Disable the brand instead.");
        }

        brandRepository.delete(brand);
    }

    private BrandResponseDTO toResponseDTO(Brand brand) {
        return BrandResponseDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .active(brand.isActive())
                .createdAt(brand.getCreatedAt())
                .build();
    }
}
