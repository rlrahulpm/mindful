package com.productapp.repository;

import com.productapp.entity.Assumption;
import com.productapp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssumptionRepository extends JpaRepository<Assumption, Long> {
    
    List<Assumption> findByProduct(Product product);
    
    List<Assumption> findByProductId(Long productId);
    
    Optional<Assumption> findByIdAndProductId(Long id, Long productId);
    
    void deleteByProductId(Long productId);
}