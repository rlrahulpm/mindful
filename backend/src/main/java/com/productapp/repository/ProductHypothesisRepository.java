package com.productapp.repository;

import com.productapp.entity.ProductHypothesis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductHypothesisRepository extends JpaRepository<ProductHypothesis, Long> {
    Optional<ProductHypothesis> findByProductId(Long productId);
}