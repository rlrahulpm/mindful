package com.productapp.repository;

import com.productapp.entity.RoadmapItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RoadmapItemRepository extends JpaRepository<RoadmapItem, Long> {
    List<RoadmapItem> findByRoadmapId(Long roadmapId);
    
    @Query("SELECT ri FROM RoadmapItem ri WHERE ri.roadmap.productId = :productId AND ri.roadmap.published = true")
    List<RoadmapItem> findPublishedByProductId(@Param("productId") Long productId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM RoadmapItem ri WHERE ri.epicId = :epicId AND ri.roadmap.productId = :productId")
    void deleteByEpicIdAndProductId(@Param("epicId") String epicId, @Param("productId") Long productId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM RoadmapItem ri WHERE ri.roadmap.id = :roadmapId")
    void deleteByRoadmapId(@Param("roadmapId") Long roadmapId);
}