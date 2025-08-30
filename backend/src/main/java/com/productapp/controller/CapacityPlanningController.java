package com.productapp.controller;

import com.productapp.dto.*;
import com.productapp.entity.*;
import com.productapp.repository.*;
import com.productapp.security.UserPrincipal;
import com.productapp.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products/{productId}/capacity-planning")
@CrossOrigin(origins = "http://localhost:3000")
public class CapacityPlanningController {
    
    private static final Logger logger = LoggerFactory.getLogger(CapacityPlanningController.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private CapacityPlanRepository capacityPlanRepository;
    
    @Autowired
    private EpicEffortRepository epicEffortRepository;
    
    @Autowired
    private QuarterlyRoadmapRepository roadmapRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private boolean hasProductAccess(Long productId, Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                return false;
            }
            
            Product product = productOpt.get();
            // Check if user owns the product or has organization access
            return (product.getUser() != null && product.getUser().getId().equals(userId)) || 
                   (user.getOrganization() != null && product.getOrganization() != null && 
                    product.getOrganization().getId().equals(user.getOrganization().getId()));
        } catch (Exception e) {
            logger.error("Error checking product access", e);
            return false;
        }
    }
    
    // Get all teams for a product
    @GetMapping("/teams")
    public ResponseEntity<?> getTeams(@PathVariable Long productId, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            logger.info("Fetching teams for product ID: {} by user ID: {}", productId, userPrincipal.getId());
            
            if (!hasProductAccess(productId, userPrincipal.getId())) {
                logger.warn("User {} attempted to access teams for product {} without permission", userPrincipal.getId(), productId);
                return ResponseEntity.notFound().build();
            }
            
            List<Team> teams = teamRepository.findByProductIdAndIsActiveTrue(productId);
            List<TeamResponse> teamResponses = teams.stream()
                    .map(TeamResponse::new)
                    .collect(Collectors.toList());
            
            logger.info("Found {} teams for product ID: {}", teams.size(), productId);
            return ResponseEntity.ok(teamResponses);
            
        } catch (Exception e) {
            logger.error("Error fetching teams for product ID: {}", productId, e);
            return ResponseEntity.internalServerError().body("Error fetching teams");
        }
    }
    
    // Add a new team
    @PostMapping("/teams")
    public ResponseEntity<?> addTeam(@PathVariable Long productId, @Valid @RequestBody TeamRequest request, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            logger.info("Adding team to product ID: {} by user ID: {}", productId, userPrincipal.getId());
            
            if (!hasProductAccess(productId, userPrincipal.getId())) {
                logger.warn("User {} attempted to add team to product {} without permission", userPrincipal.getId(), productId);
                return ResponseEntity.notFound().build();
            }
            
            // Check if team name already exists for this product
            if (teamRepository.existsByProductIdAndNameAndIsActiveTrue(productId, request.getName(), null)) {
                return ResponseEntity.badRequest().body("Team name already exists for this product");
            }
            
            Team team = new Team(request.getName(), request.getDescription(), productId);
            team.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
            team = teamRepository.save(team);
            
            logger.info("Team {} added successfully for product ID: {}", team.getName(), productId);
            return ResponseEntity.ok(new TeamResponse(team));
            
        } catch (Exception e) {
            logger.error("Error adding team to product ID: {}", productId, e);
            return ResponseEntity.internalServerError().body("Error adding team");
        }
    }
    
    // Update a team
    @PutMapping("/teams/{teamId}")
    public ResponseEntity<?> updateTeam(@PathVariable Long productId, @PathVariable Long teamId, @Valid @RequestBody TeamRequest request, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            logger.info("Updating team {} for product ID: {} by user ID: {}", teamId, productId, userPrincipal.getId());
            
            if (!hasProductAccess(productId, userPrincipal.getId())) {
                logger.warn("User {} attempted to update team for product {} without permission", userPrincipal.getId(), productId);
                return ResponseEntity.notFound().build();
            }
            
            Optional<Team> teamOpt = teamRepository.findById(teamId);
            if (teamOpt.isEmpty() || !teamOpt.get().getProductId().equals(productId)) {
                return ResponseEntity.notFound().build();
            }
            
            Team team = teamOpt.get();
            
            // Check if team name already exists for this product (excluding current team)
            if (teamRepository.existsByProductIdAndNameAndIsActiveTrue(productId, request.getName(), teamId)) {
                return ResponseEntity.badRequest().body("Team name already exists for this product");
            }
            
            team.setName(request.getName());
            team.setDescription(request.getDescription());
            team.setIsActive(request.getIsActive() != null ? request.getIsActive() : team.getIsActive());
            team = teamRepository.save(team);
            
            logger.info("Team {} updated successfully for product ID: {}", team.getName(), productId);
            return ResponseEntity.ok(new TeamResponse(team));
            
        } catch (Exception e) {
            logger.error("Error updating team {} for product ID: {}", teamId, productId, e);
            return ResponseEntity.internalServerError().body("Error updating team");
        }
    }
    
    // Delete a team (soft delete)
    @DeleteMapping("/teams/{teamId}")
    public ResponseEntity<?> deleteTeam(@PathVariable Long productId, @PathVariable Long teamId, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            logger.info("Deleting team {} for product ID: {} by user ID: {}", teamId, productId, userPrincipal.getId());
            
            if (!hasProductAccess(productId, userPrincipal.getId())) {
                logger.warn("User {} attempted to delete team for product {} without permission", userPrincipal.getId(), productId);
                return ResponseEntity.notFound().build();
            }
            
            Optional<Team> teamOpt = teamRepository.findById(teamId);
            if (teamOpt.isEmpty() || !teamOpt.get().getProductId().equals(productId)) {
                return ResponseEntity.notFound().build();
            }
            
            Team team = teamOpt.get();
            team.setIsActive(false);
            teamRepository.save(team);
            
            logger.info("Team {} deleted successfully for product ID: {}", team.getName(), productId);
            return ResponseEntity.ok().body("Team deleted successfully");
            
        } catch (Exception e) {
            logger.error("Error deleting team {} for product ID: {}", teamId, productId, e);
            return ResponseEntity.internalServerError().body("Error deleting team");
        }
    }
    
    // Get capacity plan for a specific quarter
    @GetMapping("/{year}/{quarter}")
    public ResponseEntity<?> getCapacityPlan(@PathVariable Long productId, @PathVariable Integer year, @PathVariable Integer quarter, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            logger.info("Fetching capacity plan for product ID: {}, Q{} {} by user ID: {}", productId, quarter, year, userPrincipal.getId());
            
            if (!hasProductAccess(productId, userPrincipal.getId())) {
                logger.warn("User {} attempted to access capacity plan for product {} without permission", userPrincipal.getId(), productId);
                return ResponseEntity.notFound().build();
            }
            
            // Get teams
            List<Team> teams = teamRepository.findByProductIdAndIsActiveTrue(productId);
            
            // Get or create capacity plan
            Optional<CapacityPlan> capacityPlanOpt = capacityPlanRepository.findByProductIdAndYearAndQuarter(productId, year, quarter);
            CapacityPlan capacityPlan;
            
            if (capacityPlanOpt.isEmpty()) {
                // Create new capacity plan
                capacityPlan = new CapacityPlan(productId, year, quarter);
                capacityPlan = capacityPlanRepository.save(capacityPlan);
                
                // Get epics from roadmap for this quarter and create default epic efforts
                createDefaultEpicEffortsFromRoadmap(capacityPlan, productId, year, quarter);
            } else {
                capacityPlan = capacityPlanOpt.get();
            }
            
            // Get epic efforts
            List<EpicEffort> epicEfforts = epicEffortRepository.findByCapacityPlanIdOrderByEpicNameTeamId(capacityPlan.getId());
            
            CapacityPlanResponse response = new CapacityPlanResponse(capacityPlan, epicEfforts);
            response.setTeams(teams.stream().map(TeamResponse::new).collect(Collectors.toList()));
            
            logger.info("Capacity plan retrieved for product ID: {}, Q{} {} with {} epic efforts", productId, quarter, year, epicEfforts.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching capacity plan for product ID: {}, Q{} {}", productId, quarter, year, e);
            return ResponseEntity.internalServerError().body("Error fetching capacity plan");
        }
    }
    
    // Save capacity plan
    @PostMapping("/{year}/{quarter}")
    public ResponseEntity<?> saveCapacityPlan(@PathVariable Long productId, @PathVariable Integer year, @PathVariable Integer quarter, @Valid @RequestBody CapacityPlanRequest request, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            logger.info("Saving capacity plan for product ID: {}, Q{} {} by user ID: {}", productId, quarter, year, userPrincipal.getId());
            
            if (!hasProductAccess(productId, userPrincipal.getId())) {
                logger.warn("User {} attempted to save capacity plan for product {} without permission", userPrincipal.getId(), productId);
                return ResponseEntity.notFound().build();
            }
            
            // Get or create capacity plan
            Optional<CapacityPlan> capacityPlanOpt = capacityPlanRepository.findByProductIdAndYearAndQuarter(productId, year, quarter);
            CapacityPlan capacityPlan;
            
            if (capacityPlanOpt.isEmpty()) {
                capacityPlan = new CapacityPlan(productId, year, quarter);
                capacityPlan = capacityPlanRepository.save(capacityPlan);
            } else {
                capacityPlan = capacityPlanOpt.get();
            }
            
            // Update effort unit if different
            if (request.getEffortUnit() != null && !request.getEffortUnit().equals(capacityPlan.getEffortUnit())) {
                capacityPlan.setEffortUnit(request.getEffortUnit());
                capacityPlan = capacityPlanRepository.save(capacityPlan);
            }
            
            // Update epic efforts
            if (request.getEpicEfforts() != null) {
                for (EpicEffortRequest effortRequest : request.getEpicEfforts()) {
                    Optional<EpicEffort> existingEffortOpt = epicEffortRepository.findByCapacityPlanIdAndEpicIdAndTeamId(
                        capacityPlan.getId(), effortRequest.getEpicId(), effortRequest.getTeamId());
                    
                    if (existingEffortOpt.isPresent()) {
                        EpicEffort existingEffort = existingEffortOpt.get();
                        existingEffort.setEffortDays(effortRequest.getEffortDays());
                        existingEffort.setNotes(effortRequest.getNotes());
                        epicEffortRepository.save(existingEffort);
                    } else {
                        EpicEffort newEffort = new EpicEffort(capacityPlan.getId(), effortRequest.getEpicId(), 
                            effortRequest.getEpicName(), effortRequest.getTeamId(), effortRequest.getEffortDays());
                        newEffort.setNotes(effortRequest.getNotes());
                        epicEffortRepository.save(newEffort);
                    }
                }
            }
            
            logger.info("Capacity plan saved successfully for product ID: {}, Q{} {}", productId, quarter, year);
            return ResponseEntity.ok().body("Capacity plan saved successfully");
            
        } catch (Exception e) {
            logger.error("Error saving capacity plan for product ID: {}, Q{} {}", productId, quarter, year, e);
            return ResponseEntity.internalServerError().body("Error saving capacity plan");
        }
    }
    
    private void createDefaultEpicEffortsFromRoadmap(CapacityPlan capacityPlan, Long productId, Integer year, Integer quarter) {
        try {
            // Get roadmap for this quarter
            Optional<QuarterlyRoadmap> roadmapOpt = roadmapRepository.findByProductIdAndYearAndQuarter(productId, year, quarter);
            if (roadmapOpt.isEmpty()) {
                logger.info("No roadmap found for product ID: {}, Q{} {} - skipping epic creation", productId, quarter, year);
                return;
            }
            
            QuarterlyRoadmap roadmap = roadmapOpt.get();
            if (roadmap.getRoadmapItems() == null || roadmap.getRoadmapItems().isEmpty()) {
                logger.info("No roadmap items found for product ID: {}, Q{} {} - skipping epic creation", productId, quarter, year);
                return;
            }
            
            // Parse roadmap items from JSON
            List<QuarterlyRoadmapRequest.RoadmapItem> roadmapItems = objectMapper.readValue(
                roadmap.getRoadmapItems(),
                new TypeReference<List<QuarterlyRoadmapRequest.RoadmapItem>>() {}
            );
            
            // Get all active teams for this product
            List<Team> teams = teamRepository.findByProductIdAndIsActiveTrue(productId);
            
            // Create epic efforts for each epic and each team (with 0 effort initially)
            for (QuarterlyRoadmapRequest.RoadmapItem item : roadmapItems) {
                for (Team team : teams) {
                    EpicEffort epicEffort = new EpicEffort(
                        capacityPlan.getId(),
                        item.getEpicId(),
                        item.getEpicName(),
                        team.getId(),
                        0 // Default to 0 effort days
                    );
                    epicEffortRepository.save(epicEffort);
                    logger.info("Created default epic effort for epic '{}' and team '{}' in capacity plan ID: {}", 
                               item.getEpicName(), team.getName(), capacityPlan.getId());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error creating default epic efforts from roadmap for capacity plan ID: {}", capacityPlan.getId(), e);
        }
    }
}