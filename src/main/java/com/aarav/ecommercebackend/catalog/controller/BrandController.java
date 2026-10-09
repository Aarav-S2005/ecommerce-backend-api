package com.aarav.ecommercebackend.catalog.controller;

import com.aarav.ecommercebackend.catalog.dto.request.BrandRequestDTO;
import com.aarav.ecommercebackend.catalog.dto.response.BrandResponseDTO;
import com.aarav.ecommercebackend.catalog.dto.response.GetAllBrandsResponseDTO;
import com.aarav.ecommercebackend.catalog.service.BrandService;
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
public class BrandController {

    private final BrandService brandService;

    @GetMapping("/brands")
    public ResponseEntity<GetAllBrandsResponseDTO> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    @GetMapping("/brands/{brandId}")
    public ResponseEntity<BrandResponseDTO> getBrandById(@PathVariable UUID brandId) {
        return ResponseEntity.ok(brandService.getBrandById(brandId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/brands")
    @ResponseStatus(HttpStatus.CREATED)
    public void addBrand(@RequestBody @Valid BrandRequestDTO request) {
        brandService.addBrand(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/brands/{brandId}")
    public void updateBrand(@PathVariable UUID brandId, @RequestBody @Valid BrandRequestDTO request) {
        brandService.updateBrand(brandId, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/brands/{brandId}")
    public ResponseEntity<Void> deleteBrand(@PathVariable UUID brandId) {
        brandService.deleteBrand(brandId);
        return ResponseEntity.noContent().build();
    }
}
