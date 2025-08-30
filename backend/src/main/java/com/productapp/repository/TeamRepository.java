package com.productapp.repository;

import com.productapp.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    
    @Query("SELECT t FROM Team t WHERE t.productId = :productId AND t.isActive = true ORDER BY t.name")
    List<Team> findByProductIdAndIsActiveTrue(@Param("productId") Long productId);
    
    @Query("SELECT t FROM Team t WHERE t.productId = :productId ORDER BY t.name")
    List<Team> findByProductIdOrderByName(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(t) > 0 FROM Team t WHERE t.productId = :productId AND t.name = :name AND t.isActive = true AND (:excludeId IS NULL OR t.id != :excludeId)")
    boolean existsByProductIdAndNameAndIsActiveTrue(@Param("productId") Long productId, @Param("name") String name, @Param("excludeId") Long excludeId);
}