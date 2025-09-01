package com.productapp.repository;

import com.productapp.entity.BacklogEpic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BacklogEpicRepository extends JpaRepository<BacklogEpic, Long> {
    List<BacklogEpic> findByProductId(Long productId);
    void deleteByProductId(Long productId);
    Optional<BacklogEpic> findByProductIdAndEpicId(Long productId, String epicId);
    
    @Modifying
    @Query(value = "DELETE FROM backlog_epics WHERE product_id = :productId", nativeQuery = true)
    void deleteAllByProductIdNative(@Param("productId") Long productId);
}