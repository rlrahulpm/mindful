package com.productapp.repository;

import com.productapp.entity.ProductBacklog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductBacklogRepository extends JpaRepository<ProductBacklog, Long> {
    Optional<ProductBacklog> findByProductId(Long productId);
}