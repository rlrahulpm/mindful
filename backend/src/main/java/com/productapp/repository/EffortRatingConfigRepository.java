package com.productapp.repository;

import com.productapp.entity.EffortRatingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EffortRatingConfigRepository extends JpaRepository<EffortRatingConfig, Long> {
    
    Optional<EffortRatingConfig> findByProductIdAndUnitType(Long productId, String unitType);
    
    List<EffortRatingConfig> findByProductId(Long productId);
    
    void deleteByProductId(Long productId);
}