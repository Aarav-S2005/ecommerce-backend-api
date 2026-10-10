package com.aarav.ecommercebackend.catalog.repository;

import com.aarav.ecommercebackend.catalog.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {
}
