package com.productapp.controller;

import com.productapp.dto.*;
import com.productapp.entity.*;
import com.productapp.repository.*;
import com.productapp.security.UserPrincipal;
import com.productapp.exception.ResourceNotFoundException;
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
import java.util.Set;
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
    private RoadmapItemRepository roadmapItemRepository;
    
    @Autowired
    private EffortRatingConfigRepository effortRatingConfigRepository;
    
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
            
            if (!hasProductAccess(productId, userPrincipal.getId())) {
                logger.warn("User {} attempted to access teams for product {} without permission", userPrincipal.getId(), productId);
                return ResponseEntity.notFound().build();
            }
            
            List<Team> teams = teamRepository.findByProductIdAndIsActiveTrue(productId);
            List<TeamResponse> teamResponses = teams.stream()
                    .map(TeamResponse::new)
                    .collect(Collectors.toList());
            
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
                // Sync existing capacity plan with current roadmap (in case new epics were added)
                syncCapacityPlanWithRoadmap(capacityPlan, productId, year, quarter);
            }
            
            // Get epic efforts
            List<EpicEffort> epicEfforts = epicEffortRepository.findByCapacityPlanIdOrderByEpicNameTeamId(capacityPlan.getId());
            
            CapacityPlanResponse response = new CapacityPlanResponse(capacityPlan, epicEfforts);
            response.setTeams(teams.stream().map(TeamResponse::new).collect(Collectors.toList()));
            
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
            
            return ResponseEntity.ok().body("Capacity plan saved successfully");
            
        } catch (Exception e) {
            logger.error("Error saving capacity plan for product ID: {}, Q{} {}", productId, quarter, year, e);
            return ResponseEntity.internalServerError().body("Error saving capacity plan");
        }
    }
    
    // Get effort rating configurations for a product
    @GetMapping("/effort-rating-configs")
    public ResponseEntity<?> getEffortRatingConfigs(@PathVariable Long productId, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            if (!hasProductAccess(productId, userPrincipal.getId())) {
                logger.warn("User {} attempted to access effort rating configs for product {} without permission", userPrincipal.getId(), productId);
                return ResponseEntity.notFound().build();
            }
            
            List<EffortRatingConfig> configs = effortRatingConfigRepository.findByProductId(productId);
            // If no configs exist, create default ones
            if (configs.isEmpty()) {
                EffortRatingConfig sprintsConfig = EffortRatingConfig.createDefaultForSprints(productId);
                EffortRatingConfig daysConfig = EffortRatingConfig.createDefaultForDays(productId);
                
                sprintsConfig = effortRatingConfigRepository.save(sprintsConfig);
                daysConfig = effortRatingConfigRepository.save(daysConfig);
                
                configs = List.of(sprintsConfig, daysConfig);
            }
            
            List<EffortRatingConfigResponse> responses = configs.stream()
                    .map(EffortRatingConfigResponse::new)
                    .collect(Collectors.toList());
            
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            logger.error("Error fetching effort rating configs for product ID: {}", productId, e);
            return ResponseEntity.internalServerError().body("Error fetching effort rating configs");
        }
    }
    
    // Update effort rating configuration
    @PutMapping("/effort-rating-configs/{unitType}")
    public ResponseEntity<?> updateEffortRatingConfig(@PathVariable Long productId, @PathVariable String unitType, 
            @Valid @RequestBody EffortRatingConfigRequest request, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            if (!hasProductAccess(productId, userPrincipal.getId())) {
                logger.warn("User {} attempted to update effort rating config for product {} without permission", userPrincipal.getId(), productId);
                return ResponseEntity.notFound().build();
            }
            
            // Validate unit type
            if (!unitType.equals("SPRINTS") && !unitType.equals("DAYS")) {
                return ResponseEntity.badRequest().body("Unit type must be SPRINTS or DAYS");
            }
            
            // Get existing config or create new one
            Optional<EffortRatingConfig> configOpt = effortRatingConfigRepository.findByProductIdAndUnitType(productId, unitType);
            EffortRatingConfig config;
            
            if (configOpt.isPresent()) {
                config = configOpt.get();
            } else {
                config = new EffortRatingConfig();
                config.setProductId(productId);
                config.setUnitType(unitType);
            }
            
            // Update configuration values
            config.setStar1Max(request.getStar1Max());
            config.setStar2Min(request.getStar2Min());
            config.setStar2Max(request.getStar2Max());
            config.setStar3Min(request.getStar3Min());
            config.setStar3Max(request.getStar3Max());
            config.setStar4Min(request.getStar4Min());
            config.setStar4Max(request.getStar4Max());
            config.setStar5Min(request.getStar5Min());
            
            config = effortRatingConfigRepository.save(config);
            
            return ResponseEntity.ok(new EffortRatingConfigResponse(config));
            
        } catch (Exception e) {
            logger.error("Error updating effort rating config for product ID: {}, unitType: {}", productId, unitType, e);
            return ResponseEntity.internalServerError().body("Error updating effort rating config");
        }
    }

    private void createDefaultEpicEffortsFromRoadmap(CapacityPlan capacityPlan, Long productId, Integer year, Integer quarter) {
        try {
            // Get roadmap for this quarter
            Optional<QuarterlyRoadmap> roadmapOpt = roadmapRepository.findByProductIdAndYearAndQuarter(productId, year, quarter);
            if (roadmapOpt.isEmpty()) {
                return;
            }
            
            QuarterlyRoadmap roadmap = roadmapOpt.get();
            // Get roadmap items from normalized table using explicit query
            List<RoadmapItem> roadmapItems = roadmapItemRepository.findByRoadmapId(roadmap.getId());
            if (roadmapItems == null || roadmapItems.isEmpty()) {
                return;
            }
            
            // Get all active teams for this product
            List<Team> teams = teamRepository.findByProductIdAndIsActiveTrue(productId);
            
            // Create epic efforts for each epic and each team (with 0 effort initially)
            for (RoadmapItem item : roadmapItems) {
                for (Team team : teams) {
                    EpicEffort epicEffort = new EpicEffort(
                        capacityPlan.getId(),
                        item.getEpicId(),
                        item.getEpicName(),
                        team.getId(),
                        0 // Default to 0 effort days
                    );
                    epicEffortRepository.save(epicEffort);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error creating default epic efforts from roadmap for capacity plan ID: {}", capacityPlan.getId(), e);
        }
    }

    private void syncCapacityPlanWithRoadmap(CapacityPlan capacityPlan, Long productId, Integer year, Integer quarter) {
        try {
            
            // Get roadmap for this quarter
            Optional<QuarterlyRoadmap> roadmapOpt = roadmapRepository.findByProductIdAndYearAndQuarter(productId, year, quarter);
            if (roadmapOpt.isEmpty()) {
                return;
            }
            
            QuarterlyRoadmap roadmap = roadmapOpt.get();
            List<RoadmapItem> roadmapItems = roadmapItemRepository.findByRoadmapId(roadmap.getId());
            
            if (roadmapItems == null || roadmapItems.isEmpty()) {
                return;
            }
            
            // Get existing epic efforts
            List<EpicEffort> existingEfforts = epicEffortRepository.findByCapacityPlanIdOrderByEpicNameTeamId(capacityPlan.getId());
            Set<String> existingEpicIds = existingEfforts.stream()
                    .map(EpicEffort::getEpicId)
                    .collect(Collectors.toSet());
            
            // Get all active teams for this product
            List<Team> teams = teamRepository.findByProductIdAndIsActiveTrue(productId);
            
            // Add epic efforts for new epics that don't already exist
            int newEffortsCreated = 0;
            for (RoadmapItem item : roadmapItems) {
                if (!existingEpicIds.contains(item.getEpicId())) {
                    // This is a new epic, create efforts for all teams
                    for (Team team : teams) {
                        EpicEffort epicEffort = new EpicEffort(
                            capacityPlan.getId(),
                            item.getEpicId(),
                            item.getEpicName(),
                            team.getId(),
                            0 // Default to 0 effort days
                        );
                        epicEffortRepository.save(epicEffort);
                        newEffortsCreated++;
                    }
                }
            }
            
            
        } catch (Exception e) {
            logger.error("Error syncing capacity plan ID {} with roadmap", capacityPlan.getId(), e);
        }
    }
}