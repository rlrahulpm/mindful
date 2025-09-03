package com.productapp.repository;

import com.productapp.entity.Product;
import com.productapp.entity.Module;
import com.productapp.entity.ProductModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductModuleRepository extends JpaRepository<ProductModule, Long> {
    
    @Query("SELECT pm FROM ProductModule pm " +
           "JOIN FETCH pm.module m " +
           "WHERE pm.product.id = :productId " +
           "AND pm.isEnabled = true " +
           "AND m.isActive = true " +
           "ORDER BY m.displayOrder")
    List<ProductModule> findEnabledModulesByProductId(@Param("productId") Long productId);
    
    @Query("SELECT pm FROM ProductModule pm " +
           "WHERE pm.product.id = :productId " +
           "AND pm.module.id = :moduleId")
    ProductModule findByProductIdAndModuleId(@Param("productId") Long productId, @Param("moduleId") Long moduleId);
    
    @Query("SELECT pm FROM ProductModule pm " +
           "JOIN FETCH pm.product p " +
           "JOIN FETCH pm.module m " +
           "WHERE p.organization.id = :organizationId " +
           "ORDER BY p.productName, m.displayOrder")
    List<ProductModule> findByProductOrganizationId(@Param("organizationId") Long organizationId);
    
    Optional<ProductModule> findByProductAndModule(Product product, Module module);
}