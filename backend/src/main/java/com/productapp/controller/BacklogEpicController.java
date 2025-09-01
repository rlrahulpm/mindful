package com.productapp.controller;

import com.productapp.dto.BacklogEpicRequest;
import com.productapp.dto.BacklogEpicResponse;
import com.productapp.entity.BacklogEpic;
import com.productapp.entity.Product;
import com.productapp.repository.BacklogEpicRepository;
import com.productapp.repository.ProductRepository;
import com.productapp.repository.RoadmapItemRepository;
import com.productapp.repository.EpicEffortRepository;
import com.productapp.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v3/products/{productId}/backlog")
@Tag(name = "Backlog Epic V3", description = "Simplified backlog epic management without intermediate product_backlog table")
public class BacklogEpicController {
    
    private static final Logger logger = LoggerFactory.getLogger(BacklogEpicController.class);
    
    @Autowired
    private BacklogEpicRepository backlogEpicRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RoadmapItemRepository roadmapItemRepository;
    
    @Autowired
    private EpicEffortRepository epicEffortRepository;
    
    @GetMapping
    @Operation(summary = "Get product backlog epics", description = "Retrieve all epics for a specific product")
    public ResponseEntity<?> getProductBacklogEpics(@PathVariable Long productId, HttpServletRequest request) {
        try {
            // Get user ID from JWT token
            String token = request.getHeader("Authorization").substring(7);
            Long userId = jwtUtil.getUserIdFromJwtToken(token);
            
            
            // Check if user owns the product
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Product product = productOpt.get();
            if (!product.getUser().getId().equals(userId)) {
                logger.warn("User ID: {} attempted to access backlog epics for product ID: {} they don't own", userId, productId);
                return ResponseEntity.status(403).body("Access denied");
            }
            
            // Get epics directly from product
            List<BacklogEpic> epics = backlogEpicRepository.findByProductId(productId);
            
            BacklogEpicResponse response = convertToResponse(productId, epics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving backlog epics for product ID: {}", productId, e);
            return ResponseEntity.status(500).body("Error retrieving backlog epics");
        }
    }
    
    @PostMapping
    @Transactional
    @Operation(summary = "Update product backlog epics", description = "Update epics with cascade deletion across modules")
    public ResponseEntity<?> updateProductBacklogEpics(@PathVariable Long productId, @RequestBody BacklogEpicRequest request, HttpServletRequest httpRequest) {
        try {
            // Get user ID from JWT token
            String token = httpRequest.getHeader("Authorization").substring(7);
            Long userId = jwtUtil.getUserIdFromJwtToken(token);
            
            
            // Check if user owns the product
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Product product = productOpt.get();
            if (!product.getUser().getId().equals(userId)) {
                logger.warn("User ID: {} attempted to update backlog epics for product ID: {} they don't own", userId, productId);
                return ResponseEntity.status(403).body("Access denied");
            }
            
            // Get current epic IDs for cascade deletion
            List<BacklogEpic> currentEpics = backlogEpicRepository.findByProductId(productId);
            Set<String> currentEpicIds = currentEpics.stream()
                    .map(BacklogEpic::getEpicId)
                    .collect(Collectors.toSet());
            
            // Use native query to delete all epics for this product
            // This won't fail even if no records exist
            backlogEpicRepository.deleteAllByProductIdNative(productId);
            
            // Parse and create new epics
            List<BacklogEpic> newEpics = new ArrayList<>();
            Set<String> newEpicIds = new HashSet<>();
            
            if (request.getEpics() != null && !request.getEpics().isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.core.type.TypeReference<List<EpicDto>> typeRef = new com.fasterxml.jackson.core.type.TypeReference<List<EpicDto>>() {};
                List<EpicDto> epicDtos = mapper.readValue(request.getEpics(), typeRef);
                
                for (EpicDto epicDto : epicDtos) {
                    BacklogEpic epic = new BacklogEpic();
                    epic.setProduct(product);
                    epic.setEpicId(epicDto.getId());
                    epic.setEpicName(epicDto.getName());
                    epic.setEpicDescription(epicDto.getDescription());
                    epic.setThemeId(epicDto.getThemeId());
                    epic.setThemeName(epicDto.getThemeName());
                    epic.setThemeColor(epicDto.getThemeColor());
                    epic.setInitiativeId(epicDto.getInitiativeId());
                    epic.setInitiativeName(epicDto.getInitiativeName());
                    epic.setTrack(epicDto.getTrack());
                    
                    newEpics.add(epic);
                    newEpicIds.add(epicDto.getId());
                }
                
                // Save all new epics
                backlogEpicRepository.saveAll(newEpics);
            }
            
            // Find deleted epics and cascade delete
            Set<String> deletedEpicIds = new HashSet<>(currentEpicIds);
            deletedEpicIds.removeAll(newEpicIds);
            
            for (String deletedEpicId : deletedEpicIds) {
                roadmapItemRepository.deleteByEpicIdAndProductId(deletedEpicId, productId);
                epicEffortRepository.deleteByEpicIdAndProductId(deletedEpicId, productId);
            }
            
            BacklogEpicResponse response = convertToResponse(productId, newEpics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating backlog epics for product ID: {}", productId, e);
            return ResponseEntity.status(500).body("Error updating backlog epics");
        }
    }
    
    private BacklogEpicResponse convertToResponse(Long productId, List<BacklogEpic> epics) {
        BacklogEpicResponse response = new BacklogEpicResponse();
        response.setProductId(productId);
        
        // Convert epics to JSON format for compatibility
        List<EpicDto> epicDtos = new ArrayList<>();
        for (BacklogEpic epic : epics) {
            EpicDto epicDto = new EpicDto();
            epicDto.setId(epic.getEpicId());
            epicDto.setName(epic.getEpicName());
            epicDto.setDescription(epic.getEpicDescription());
            epicDto.setThemeId(epic.getThemeId());
            epicDto.setThemeName(epic.getThemeName());
            epicDto.setThemeColor(epic.getThemeColor());
            epicDto.setInitiativeId(epic.getInitiativeId());
            epicDto.setInitiativeName(epic.getInitiativeName());
            epicDto.setTrack(epic.getTrack());
            epicDtos.add(epicDto);
        }
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String epicsJson = mapper.writeValueAsString(epicDtos);
            response.setEpics(epicsJson);
        } catch (Exception e) {
            logger.error("Error converting epics to JSON", e);
            response.setEpics("[]");
        }
        
        // Set timestamps from the first epic if available
        if (!epics.isEmpty()) {
            BacklogEpic firstEpic = epics.get(0);
            response.setCreatedAt(firstEpic.getCreatedAt());
            response.setUpdatedAt(firstEpic.getUpdatedAt());
        }
        
        return response;
    }
    
    // DTO class for epic data
    public static class EpicDto {
        private String id;
        private String name;
        private String description;
        private String themeId;
        private String themeName;
        private String themeColor;
        private String initiativeId;
        private String initiativeName;
        private String track;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getThemeId() { return themeId; }
        public void setThemeId(String themeId) { this.themeId = themeId; }
        
        public String getThemeName() { return themeName; }
        public void setThemeName(String themeName) { this.themeName = themeName; }
        
        public String getThemeColor() { return themeColor; }
        public void setThemeColor(String themeColor) { this.themeColor = themeColor; }
        
        public String getInitiativeId() { return initiativeId; }
        public void setInitiativeId(String initiativeId) { this.initiativeId = initiativeId; }
        
        public String getInitiativeName() { return initiativeName; }
        public void setInitiativeName(String initiativeName) { this.initiativeName = initiativeName; }
        
        public String getTrack() { return track; }
        public void setTrack(String track) { this.track = track; }
    }
}