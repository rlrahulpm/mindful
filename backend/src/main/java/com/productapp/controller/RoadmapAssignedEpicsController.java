package com.productapp.controller;

import com.productapp.entity.QuarterlyRoadmap;
import com.productapp.entity.RoadmapItem;
import com.productapp.repository.QuarterlyRoadmapRepository;
import com.productapp.repository.RoadmapItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/roadmap")
@CrossOrigin(origins = "http://localhost:3000")
public class RoadmapAssignedEpicsController {

    private static final Logger logger = LoggerFactory.getLogger(RoadmapAssignedEpicsController.class);

    @Autowired
    private QuarterlyRoadmapRepository quarterlyRoadmapRepository;
    
    @Autowired
    private RoadmapItemRepository roadmapItemRepository;

    @GetMapping("/assigned-epics")
    public ResponseEntity<List<String>> getAssignedEpicIds(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer excludeYear,
            @RequestParam(required = false) Integer excludeQuarter) {
        
        
        try {
            List<String> assignedEpicIds = new ArrayList<>();
            
            if (excludeYear != null && excludeQuarter != null) {
                // Find all roadmap items for this product excluding the specified year/quarter
                List<QuarterlyRoadmap> roadmaps = quarterlyRoadmapRepository
                    .findByProductIdExcludingQuarter(productId, excludeYear, excludeQuarter);
                
                for (QuarterlyRoadmap roadmap : roadmaps) {
                    List<RoadmapItem> items = roadmapItemRepository.findByRoadmapId(roadmap.getId());
                    for (RoadmapItem item : items) {
                        assignedEpicIds.add(item.getEpicId());
                    }
                }
            } else {
                // Find all assigned epic IDs for this product
                List<QuarterlyRoadmap> roadmaps = quarterlyRoadmapRepository.findByProductId(productId);
                for (QuarterlyRoadmap roadmap : roadmaps) {
                    List<RoadmapItem> items = roadmapItemRepository.findByRoadmapId(roadmap.getId());
                    for (RoadmapItem item : items) {
                        assignedEpicIds.add(item.getEpicId());
                    }
                }
            }
            
            return ResponseEntity.ok(assignedEpicIds);
            
        } catch (Exception e) {
            logger.error("Error fetching assigned epic IDs for product ID: {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}