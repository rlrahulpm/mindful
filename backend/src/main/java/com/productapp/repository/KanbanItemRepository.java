package com.productapp.repository;

import com.productapp.entity.KanbanItem;
import com.productapp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanbanItemRepository extends JpaRepository<KanbanItem, Long> {
    
    List<KanbanItem> findByProductOrderByStatusAscPositionAsc(Product product);
    
    List<KanbanItem> findByProductAndStatusOrderByPositionAsc(Product product, String status);
    
    @Query("SELECT COALESCE(MAX(k.position), 0) FROM KanbanItem k WHERE k.product = :product AND k.status = :status")
    Integer findMaxPositionByProductAndStatus(@Param("product") Product product, @Param("status") String status);
    
    @Query("SELECT k FROM KanbanItem k WHERE k.product.id = :productId ORDER BY k.status, k.position")
    List<KanbanItem> findByProductId(@Param("productId") Long productId);
    
    List<KanbanItem> findByEpicIdAndProduct(String epicId, Product product);
    
    void deleteByProductAndId(Product product, Long id);
}