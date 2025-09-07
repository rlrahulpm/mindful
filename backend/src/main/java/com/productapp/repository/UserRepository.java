package com.productapp.repository;

import com.productapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByOrganizationId(Long organizationId);
    List<User> findByOrganizationIdAndIsSuperadminTrue(Long organizationId);
}