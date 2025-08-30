package com.productapp.repository;

import com.productapp.entity.QuarterlyRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuarterlyRoadmapRepository extends JpaRepository<QuarterlyRoadmap, Long> {
    
    List<QuarterlyRoadmap> findByProductId(Long productId);
    
    Optional<QuarterlyRoadmap> findByProductIdAndYearAndQuarter(Long productId, Integer year, Integer quarter);
    
    @Query("SELECT DISTINCT q.year FROM QuarterlyRoadmap q WHERE q.productId = :productId ORDER BY q.year DESC")
    List<Integer> findDistinctYearsByProductId(@Param("productId") Long productId);
    
    @Query("SELECT DISTINCT q.quarter FROM QuarterlyRoadmap q WHERE q.productId = :productId AND q.year = :year ORDER BY q.quarter")
    List<Integer> findDistinctQuartersByProductIdAndYear(@Param("productId") Long productId, @Param("year") Integer year);
    
    @Query("SELECT q FROM QuarterlyRoadmap q WHERE q.productId = :productId AND NOT (q.year = :excludeYear AND q.quarter = :excludeQuarter)")
    List<QuarterlyRoadmap> findByProductIdExcludingQuarter(@Param("productId") Long productId, @Param("excludeYear") Integer excludeYear, @Param("excludeQuarter") Integer excludeQuarter);
}