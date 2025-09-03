package com.productapp.repository;

import com.productapp.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    
    List<Module> findByIsActiveTrueOrderByDisplayOrder();
    
    @Query("SELECT m FROM Module m WHERE m.isActive = true ORDER BY m.displayOrder")
    List<Module> findActiveModulesOrdered();
    
    Optional<Module> findByName(String name);
}