package com.productapp.controller;

import com.productapp.dto.QuarterlyRoadmapRequest;
import com.productapp.dto.QuarterlyRoadmapResponse;
import com.productapp.entity.QuarterlyRoadmap;
import com.productapp.entity.RoadmapItem;
import com.productapp.entity.EpicEffort;
import com.productapp.entity.EffortRatingConfig;
import com.productapp.entity.CapacityPlan;
import com.productapp.entity.BacklogEpic;
import com.productapp.repository.QuarterlyRoadmapRepository;
import com.productapp.repository.RoadmapItemRepository;
import com.productapp.repository.EpicEffortRepository;
import com.productapp.repository.EffortRatingConfigRepository;
import com.productapp.repository.CapacityPlanRepository;
import com.productapp.repository.BacklogEpicRepository;
import com.productapp.service.QuarterlyRoadmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/products/{productId}/roadmap")
@CrossOrigin(origins = "http://localhost:3000")
public class QuarterlyRoadmapV2Controller {

    private static final Logger logger = LoggerFactory.getLogger(QuarterlyRoadmapV2Controller.class);

    @Autowired
    private QuarterlyRoadmapRepository quarterlyRoadmapRepository;
    
    @Autowired
    private RoadmapItemRepository roadmapItemRepository;
    
    @Autowired
    private QuarterlyRoadmapService quarterlyRoadmapService;
    
    @Autowired
    private EpicEffortRepository epicEffortRepository;
    
    @Autowired
    private EffortRatingConfigRepository effortRatingConfigRepository;
    
    @Autowired
    private CapacityPlanRepository capacityPlanRepository;
    
    @Autowired
    private BacklogEpicRepository backlogEpicRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @GetMapping("/{year}/{quarter}")
    public ResponseEntity<QuarterlyRoadmapResponse> getRoadmap(
            @PathVariable Long productId,
            @PathVariable Integer year,
            @PathVariable Integer quarter) {
        
        
        try {
            Optional<QuarterlyRoadmap> roadmapOpt = quarterlyRoadmapRepository
                    .findByProductIdAndYearAndQuarter(productId, year, quarter);
            
            if (roadmapOpt.isPresent()) {
                QuarterlyRoadmap roadmap = roadmapOpt.get();
                List<RoadmapItem> items = roadmapItemRepository.findByRoadmapId(roadmap.getId());
                roadmap.setRoadmapItems(items);
                
                
                QuarterlyRoadmapResponse response = convertToResponse(roadmap);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching roadmap for product ID: {}, year: {}, quarter: {}", 
                        productId, year, quarter, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrUpdateRoadmap(
            @PathVariable Long productId,
            @RequestBody QuarterlyRoadmapRequest request) {
        
        if (request == null) {
            logger.error("Request body is null for product ID: {}", productId);
            return ResponseEntity.badRequest().body("Request body cannot be null");
        }
        
        
        try {
            // Use service to handle transactional operations
            QuarterlyRoadmap savedRoadmap = quarterlyRoadmapService.createOrUpdateRoadmap(productId, request);
            
            // Fetch the complete roadmap with items for response
            Optional<QuarterlyRoadmap> updatedRoadmapOpt = quarterlyRoadmapRepository
                    .findByProductIdAndYearAndQuarter(productId, request.getYear(), request.getQuarter());
            
            if (updatedRoadmapOpt.isPresent()) {
                QuarterlyRoadmap updatedRoadmap = updatedRoadmapOpt.get();
                List<RoadmapItem> items = roadmapItemRepository.findByRoadmapId(updatedRoadmap.getId());
                updatedRoadmap.setRoadmapItems(items);
                
                try {
                    QuarterlyRoadmapResponse response = convertToResponse(updatedRoadmap);
                    return ResponseEntity.ok(response);
                } catch (Exception responseEx) {
                    logger.error("Error converting response for roadmap ID: {}", updatedRoadmap.getId(), responseEx);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error preparing response: " + responseEx.getMessage());
                }
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: Could not retrieve saved roadmap");
            }
            
        } catch (Exception e) {
            logger.error("Error saving roadmap for product ID: {}, year: {}, quarter: {}", 
                        productId, request.getYear(), request.getQuarter(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving roadmap: " + e.getMessage());
        }
    }

    @PutMapping("/{year}/{quarter}/epics/{epicId}/effort-rating")
    @Transactional
    public ResponseEntity<?> updateEpicEffortRating(
            @PathVariable Long productId,
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable String epicId,
            @RequestBody EffortRatingUpdateRequest request) {
        
        try {
            Optional<QuarterlyRoadmap> roadmapOpt = quarterlyRoadmapRepository
                    .findByProductIdAndYearAndQuarter(productId, year, quarter);
            
            if (roadmapOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            QuarterlyRoadmap roadmap = roadmapOpt.get();
            List<RoadmapItem> items = roadmapItemRepository.findByRoadmapId(roadmap.getId());
            
            // Find and update the specific item
            Optional<RoadmapItem> itemToUpdate = items.stream()
                    .filter(item -> item.getEpicId().equals(epicId))
                    .findFirst();
            
            if (itemToUpdate.isPresent()) {
                RoadmapItem item = itemToUpdate.get();
                item.setEffortRating(request.getEffortRating());
                roadmapItemRepository.save(item);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error updating effort rating for epic ID: {}", epicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating effort rating: " + e.getMessage());
        }
    }

    private QuarterlyRoadmapResponse convertToResponse(QuarterlyRoadmap roadmap) {
        if (roadmap == null) {
            throw new IllegalArgumentException("Roadmap cannot be null");
        }
        
        QuarterlyRoadmapResponse response = new QuarterlyRoadmapResponse();
        response.setId(roadmap.getId());
        response.setProductId(roadmap.getProductId());
        response.setYear(roadmap.getYear());
        response.setQuarter(roadmap.getQuarter());
        response.setCreatedAt(roadmap.getCreatedAt());
        response.setUpdatedAt(roadmap.getUpdatedAt());
        
        // Convert entity items to DTO items with auto-filled effort ratings
        List<QuarterlyRoadmapRequest.RoadmapItem> dtoItems = new ArrayList<>();
        if (roadmap.getRoadmapItems() != null) {
            // Get effort ratings from capacity planning
            Map<String, Integer> epicEffortRatings = getAutoFilledEffortRatings(roadmap.getProductId(), roadmap.getYear(), roadmap.getQuarter());
            
            for (RoadmapItem item : roadmap.getRoadmapItems()) {
                QuarterlyRoadmapRequest.RoadmapItem dtoItem = new QuarterlyRoadmapRequest.RoadmapItem();
                dtoItem.setEpicId(item.getEpicId());
                dtoItem.setEpicName(item.getEpicName());
                dtoItem.setEpicDescription(item.getEpicDescription());
                dtoItem.setPriority(item.getPriority());
                dtoItem.setStatus(item.getStatus());
                dtoItem.setEstimatedEffort(item.getEstimatedEffort());
                dtoItem.setAssignedTeam(item.getAssignedTeam());
                dtoItem.setReach(item.getReach());
                dtoItem.setImpact(item.getImpact());
                dtoItem.setConfidence(item.getConfidence());
                dtoItem.setRiceScore(item.getRiceScore());
                
                // Use auto-filled effort rating from capacity planning if available, otherwise use stored value
                Integer autoFilledRating = epicEffortRatings.get(item.getEpicId());
                if (autoFilledRating != null && autoFilledRating > 0) {
                    dtoItem.setEffortRating(autoFilledRating);
                } else {
                    dtoItem.setEffortRating(item.getEffortRating());
                }
                
                // Set initiative and theme information from stored values or fetch from backlog
                if (item.getInitiativeName() != null && item.getThemeName() != null) {
                    // Use stored values if available
                    dtoItem.setInitiativeName(item.getInitiativeName());
                    dtoItem.setThemeName(item.getThemeName());
                    dtoItem.setThemeColor(item.getThemeColor());
                } else {
                    // Fetch from backlog epic if not stored
                    BacklogEpic epicDetails = getEpicDetails(roadmap.getProductId(), item.getEpicId());
                    if (epicDetails != null) {
                        dtoItem.setInitiativeName(epicDetails.getInitiativeName());
                        dtoItem.setThemeName(epicDetails.getThemeName());
                        dtoItem.setThemeColor(epicDetails.getThemeColor());
                    }
                }
                
                // Format dates for response
                if (item.getStartDate() != null) {
                    dtoItem.setStartDate(item.getStartDate().format(DATE_FORMATTER));
                }
                if (item.getEndDate() != null) {
                    dtoItem.setEndDate(item.getEndDate().format(DATE_FORMATTER));
                }
                
                dtoItems.add(dtoItem);
            }
        }
        response.setRoadmapItems(dtoItems);
        
        return response;
    }

    /**
     * Get auto-filled effort ratings for epics based on capacity planning data
     */
    private Map<String, Integer> getAutoFilledEffortRatings(Long productId, Integer year, Integer quarter) {
        Map<String, Integer> effortRatings = new HashMap<>();
        
        try {
            // Get capacity plan for this quarter
            Optional<CapacityPlan> capacityPlanOpt = capacityPlanRepository.findByProductIdAndYearAndQuarter(productId, year, quarter);
            if (capacityPlanOpt.isEmpty()) {
                return effortRatings;
            }
            
            CapacityPlan capacityPlan = capacityPlanOpt.get();
            
            // Get effort rating configurations for this product
            List<EffortRatingConfig> configs = effortRatingConfigRepository.findByProductId(productId);
            if (configs.isEmpty()) {
                return effortRatings;
            }
            
            // Get epic efforts from capacity planning
            List<EpicEffort> epicEfforts = epicEffortRepository.findByCapacityPlanIdOrderByEpicNameTeamId(capacityPlan.getId());
            if (epicEfforts.isEmpty()) {
                return effortRatings;
            }
            
            // Choose effort rating config based on capacity plan's effort unit
            EffortRatingConfig config = configs.stream()
                    .filter(c -> c.getUnitType().equals(capacityPlan.getEffortUnit()))
                    .findFirst()
                    .orElse(configs.get(0)); // fallback to first config
            
            // Group efforts by epic ID and sum total effort across all teams
            Map<String, Integer> epicTotalEfforts = epicEfforts.stream()
                    .collect(Collectors.groupingBy(
                        EpicEffort::getEpicId,
                        Collectors.summingInt(EpicEffort::getEffortDays)
                    ));
            
            // Calculate star ratings for each epic
            for (Map.Entry<String, Integer> entry : epicTotalEfforts.entrySet()) {
                String epicId = entry.getKey();
                Integer totalEffort = entry.getValue();
                
                if (totalEffort != null && totalEffort > 0) {
                    Integer starRating = calculateStarRating(totalEffort, config);
                    effortRatings.put(epicId, starRating);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error calculating auto-filled effort ratings for product ID: {}, Q{} {}", productId, quarter, year, e);
        }
        
        return effortRatings;
    }

    /**
     * Calculate star rating based on effort value and configuration
     */
    private Integer calculateStarRating(Integer effortValue, EffortRatingConfig config) {
        if (effortValue <= config.getStar1Max()) {
            return 1;
        } else if (effortValue <= config.getStar2Max()) {
            return 2;
        } else if (effortValue <= config.getStar3Max()) {
            return 3;
        } else if (effortValue <= config.getStar4Max()) {
            return 4;
        } else {
            return 5;
        }
    }

    /**
     * Get epic details (initiative and theme information) from backlog
     */
    private BacklogEpic getEpicDetails(Long productId, String epicId) {
        try {
            Optional<BacklogEpic> epicOpt = backlogEpicRepository.findByProductIdAndEpicId(productId, epicId);
            return epicOpt.orElse(null);
        } catch (Exception e) {
            logger.error("Error fetching epic details for productId: {}, epicId: {}", productId, epicId, e);
            return null;
        }
    }
    

    public static class EffortRatingUpdateRequest {
        private Integer effortRating;
        
        public Integer getEffortRating() {
            return effortRating;
        }
        
        public void setEffortRating(Integer effortRating) {
            this.effortRating = effortRating;
        }
    }
}