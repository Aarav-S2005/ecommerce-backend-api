package com.aarav.ecommercebackend.catalog.repository;

import com.aarav.ecommercebackend.catalog.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsByBrandId(UUID brandId);

    boolean existsByCategoryId(UUID categoryId);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @EntityGraph(attributePaths = {"brand", "category", "images"})
    Optional<Product> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"brand", "category"})
    Page<Product> findByActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"brand", "category"})
    Page<Product> findByCategoryIdAndActiveTrue(UUID categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"brand", "category"})
    Page<Product> findByBrandIdAndActiveTrue(UUID brandId, Pageable pageable);

    @EntityGraph(attributePaths = {"brand", "category"})
    Page<Product> findByCategoryIdAndBrandIdAndActiveTrue(UUID categoryId, UUID brandId, Pageable pageable);
}
