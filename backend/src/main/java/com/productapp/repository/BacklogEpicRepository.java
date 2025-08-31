package com.productapp.repository;

import com.productapp.entity.BacklogEpic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BacklogEpicRepository extends JpaRepository<BacklogEpic, Long> {
    List<BacklogEpic> findByProductId(Long productId);
    void deleteByProductId(Long productId);
}