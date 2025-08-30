package com.productapp.repository;

import com.productapp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByUserId(Long userId);
    List<Product> findByUserIdAndOrganizationId(Long userId, Long organizationId);
    List<Product> findByOrganizationId(Long organizationId);
    Optional<Product> findByProductNameAndOrganizationId(String productName, Long organizationId);
}