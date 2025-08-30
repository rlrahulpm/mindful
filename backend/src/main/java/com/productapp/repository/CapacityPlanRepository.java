package com.productapp.repository;

import com.productapp.entity.CapacityPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CapacityPlanRepository extends JpaRepository<CapacityPlan, Long> {
    
    @Query("SELECT cp FROM CapacityPlan cp WHERE cp.productId = :productId AND cp.year = :year AND cp.quarter = :quarter")
    Optional<CapacityPlan> findByProductIdAndYearAndQuarter(@Param("productId") Long productId, @Param("year") Integer year, @Param("quarter") Integer quarter);
    
    @Query("SELECT cp FROM CapacityPlan cp WHERE cp.productId = :productId ORDER BY cp.year DESC, cp.quarter DESC")
    List<CapacityPlan> findByProductIdOrderByYearDescQuarterDesc(@Param("productId") Long productId);
    
    @Query("SELECT cp FROM CapacityPlan cp WHERE cp.productId = :productId AND cp.year = :year ORDER BY cp.quarter")
    List<CapacityPlan> findByProductIdAndYearOrderByQuarter(@Param("productId") Long productId, @Param("year") Integer year);
}