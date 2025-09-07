package com.productapp.repository;

import com.productapp.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(String name);
    
    @Query("SELECT r FROM Role r ORDER BY r.name")
    List<Role> findAllOrderByName();
    
    @Query("SELECT DISTINCT r FROM Role r " +
           "LEFT JOIN r.productModules pm " +
           "WHERE pm.product.organization.id = :organizationId " +
           "OR r.productModules IS EMPTY " +
           "ORDER BY r.name")
    List<Role> findByOrganizationIdOrderByName(Long organizationId);
    
    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, Long id);
}