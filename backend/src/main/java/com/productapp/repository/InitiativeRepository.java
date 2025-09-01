package com.productapp.repository;

import com.productapp.entity.Initiative;
import com.productapp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InitiativeRepository extends JpaRepository<Initiative, Long> {
    
    List<Initiative> findByProduct(Product product);
    
    List<Initiative> findByProductId(Long productId);
    
    Optional<Initiative> findByProductAndTitle(Product product, String title);
    
    Optional<Initiative> findByIdAndProductId(Long id, Long productId);
    
    void deleteByProductId(Long productId);
    
    boolean existsByProductIdAndTitle(Long productId, String title);
}