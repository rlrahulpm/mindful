package com.productapp.repository;

import com.productapp.entity.Theme;
import com.productapp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {
    
    List<Theme> findByProduct(Product product);
    
    List<Theme> findByProductId(Long productId);
    
    Optional<Theme> findByProductAndName(Product product, String name);
    
    Optional<Theme> findByIdAndProductId(Long id, Long productId);
    
    void deleteByProductId(Long productId);
    
    boolean existsByProductIdAndName(Long productId, String name);
}