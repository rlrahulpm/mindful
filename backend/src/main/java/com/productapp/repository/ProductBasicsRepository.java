package com.productapp.repository;

import com.productapp.entity.ProductBasics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductBasicsRepository extends JpaRepository<ProductBasics, Long> {
    
    Optional<ProductBasics> findByProductId(Long productId);
    
    boolean existsByProductId(Long productId);
    
    void deleteByProductId(Long productId);
}