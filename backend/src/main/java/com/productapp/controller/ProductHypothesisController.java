package com.productapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.productapp.dto.ProductHypothesisRequest;
import com.productapp.dto.ProductHypothesisResponse;
import com.productapp.entity.*;
import com.productapp.exception.ResourceNotFoundException;
import com.productapp.exception.UnauthorizedException;
import com.productapp.repository.ProductHypothesisRepository;
import com.productapp.repository.ProductRepository;
import com.productapp.repository.UserRepository;
import com.productapp.repository.ThemeRepository;
import com.productapp.repository.InitiativeRepository;
import com.productapp.repository.AssumptionRepository;
import com.productapp.repository.BacklogEpicRepository;
import com.productapp.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/products/{productId}/hypothesis")
@Tag(name = "Product Hypothesis", description = "Product Hypothesis module management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProductHypothesisController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductHypothesisController.class);
    
    @Autowired
    private ProductHypothesisRepository productHypothesisRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ThemeRepository themeRepository;
    
    @Autowired
    private InitiativeRepository initiativeRepository;
    
    @Autowired
    private AssumptionRepository assumptionRepository;
    
    @Autowired
    private BacklogEpicRepository backlogEpicRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    
    @GetMapping
    @Operation(summary = "Get product hypothesis", description = "Get product hypothesis data including initiatives and themes for a specific product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product hypothesis retrieved successfully",
                content = @Content(schema = @Schema(implementation = ProductHypothesisResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - No access to this product", content = @Content),
        @ApiResponse(responseCode = "404", description = "Product or hypothesis not found", content = @Content)
    })
    public ResponseEntity<?> getProductHypothesis(
            @PathVariable Long productId,
            Authentication authentication) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Verify user has access to the product
        if (!hasProductAccess(productId, userPrincipal.getId())) {
            logger.warn("Unauthorized access attempt - User ID: {} tried to access product ID: {}", 
                       userPrincipal.getId(), productId);
            throw new UnauthorizedException("You are not authorized to access this product");
        }
        
        ProductHypothesis productHypothesis = productHypothesisRepository.findByProductId(productId)
                .orElse(null);
        
        if (productHypothesis == null) {
            // Return empty response if no data exists yet
            return ResponseEntity.ok(new ProductHypothesisResponse(null, productId, null, null, null, null, null, null, null));
        }
        
        // Fetch data from normalized tables and convert to JSON for backward compatibility
        String themesJson = "[]";
        String initiativesJson = "[]";
        String assumptionsJson = "[]";
        
        try {
            // Fetch themes
            List<Theme> themes = themeRepository.findByProductId(productId);
            List<Map<String, Object>> themeList = themes.stream().map(theme -> {
                Map<String, Object> themeMap = new HashMap<>();
                themeMap.put("id", theme.getId().toString());
                themeMap.put("name", theme.getName());
                themeMap.put("description", theme.getDescription() != null ? theme.getDescription() : "");
                themeMap.put("color", theme.getColor());
                return themeMap;
            }).toList();
            themesJson = objectMapper.writeValueAsString(themeList);
            
            // Fetch initiatives
            List<Initiative> initiatives = initiativeRepository.findByProductId(productId);
            List<Map<String, Object>> initiativeList = initiatives.stream().map(initiative -> {
                Map<String, Object> initiativeMap = new HashMap<>();
                initiativeMap.put("id", initiative.getId().toString());
                initiativeMap.put("title", initiative.getTitle());
                initiativeMap.put("description", initiative.getDescription() != null ? initiative.getDescription() : "");
                initiativeMap.put("priority", initiative.getPriority() != null ? initiative.getPriority() : "Medium");
                initiativeMap.put("timeline", initiative.getTimeline() != null ? initiative.getTimeline() : "");
                initiativeMap.put("owner", initiative.getOwner() != null ? initiative.getOwner() : "");
                return initiativeMap;
            }).toList();
            initiativesJson = objectMapper.writeValueAsString(initiativeList);
            
            // Fetch assumptions
            List<Assumption> assumptions = assumptionRepository.findByProductId(productId);
            List<Map<String, Object>> assumptionList = assumptions.stream().map(assumption -> {
                Map<String, Object> assumptionMap = new HashMap<>();
                assumptionMap.put("id", assumption.getId().toString());
                assumptionMap.put("assumption", assumption.getAssumption());
                assumptionMap.put("confidence", assumption.getConfidence() != null ? assumption.getConfidence() : "Medium");
                assumptionMap.put("impact", assumption.getImpact() != null ? assumption.getImpact() : "Medium");
                return assumptionMap;
            }).toList();
            assumptionsJson = objectMapper.writeValueAsString(assumptionList);
            
        } catch (Exception e) {
            logger.error("Error converting normalized data to JSON for product ID: {}", productId, e);
        }
        
        ProductHypothesisResponse response = new ProductHypothesisResponse(
                productHypothesis.getId(),
                productHypothesis.getProduct().getId(),
                productHypothesis.getHypothesisStatement(),
                productHypothesis.getSuccessMetrics(),
                assumptionsJson,
                initiativesJson,
                themesJson,
                productHypothesis.getCreatedAt(),
                productHypothesis.getUpdatedAt()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(summary = "Create or update product hypothesis", description = "Create or update product hypothesis data including initiatives and themes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product hypothesis saved successfully",
                content = @Content(schema = @Schema(implementation = ProductHypothesisResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - No access to this product", content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<?> saveProductHypothesis(
            @PathVariable Long productId,
            @Valid @RequestBody ProductHypothesisRequest request,
            Authentication authentication) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Verify user has access to the product
        if (!hasProductAccess(productId, userPrincipal.getId())) {
            logger.warn("Unauthorized access attempt - User ID: {} tried to modify product ID: {}", 
                       userPrincipal.getId(), productId);
            throw new UnauthorizedException("You are not authorized to modify this product");
        }
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        ProductHypothesis productHypothesis = productHypothesisRepository.findByProductId(productId)
                .orElse(new ProductHypothesis(product));
        
        // Update the fields
        productHypothesis.setHypothesisStatement(request.getHypothesisStatement());
        productHypothesis.setSuccessMetrics(request.getSuccessMetrics());
        
        // Parse JSON and save to normalized tables
        try {
            // Save themes
            if (request.getThemes() != null && !request.getThemes().trim().isEmpty() && !"null".equals(request.getThemes())) {
                saveThemesToNormalizedTable(product, request.getThemes());
            }
            
            // Save initiatives  
            if (request.getInitiatives() != null && !request.getInitiatives().trim().isEmpty() && !"null".equals(request.getInitiatives())) {
                saveInitiativesToNormalizedTable(product, request.getInitiatives());
            }
            
            // Save assumptions
            if (request.getAssumptions() != null && !request.getAssumptions().trim().isEmpty() && !"null".equals(request.getAssumptions())) {
                saveAssumptionsToNormalizedTable(product, request.getAssumptions());
            }
        } catch (Exception e) {
            logger.error("Error saving to normalized tables for product ID: {}", productId, e);
        }
        
        ProductHypothesis savedProductHypothesis = productHypothesisRepository.save(productHypothesis);
        
        // Return the updated data from normalized tables
        String savedThemesJson = "[]";
        String savedInitiativesJson = "[]";
        String savedAssumptionsJson = "[]";
        
        try {
            // Fetch updated themes
            List<Theme> themes = themeRepository.findByProductId(productId);
            List<Map<String, Object>> themeList = themes.stream().map(theme -> {
                Map<String, Object> themeMap = new HashMap<>();
                themeMap.put("id", theme.getId().toString());
                themeMap.put("name", theme.getName());
                themeMap.put("description", theme.getDescription() != null ? theme.getDescription() : "");
                themeMap.put("color", theme.getColor());
                return themeMap;
            }).toList();
            savedThemesJson = objectMapper.writeValueAsString(themeList);
            
            // Fetch updated initiatives
            List<Initiative> initiatives = initiativeRepository.findByProductId(productId);
            List<Map<String, Object>> initiativeList = initiatives.stream().map(initiative -> {
                Map<String, Object> initiativeMap = new HashMap<>();
                initiativeMap.put("id", initiative.getId().toString());
                initiativeMap.put("title", initiative.getTitle());
                initiativeMap.put("description", initiative.getDescription() != null ? initiative.getDescription() : "");
                initiativeMap.put("priority", initiative.getPriority() != null ? initiative.getPriority() : "Medium");
                initiativeMap.put("timeline", initiative.getTimeline() != null ? initiative.getTimeline() : "");
                initiativeMap.put("owner", initiative.getOwner() != null ? initiative.getOwner() : "");
                return initiativeMap;
            }).toList();
            savedInitiativesJson = objectMapper.writeValueAsString(initiativeList);
            
            // Fetch updated assumptions
            List<Assumption> assumptions = assumptionRepository.findByProductId(productId);
            List<Map<String, Object>> assumptionList = assumptions.stream().map(assumption -> {
                Map<String, Object> assumptionMap = new HashMap<>();
                assumptionMap.put("id", assumption.getId().toString());
                assumptionMap.put("assumption", assumption.getAssumption());
                assumptionMap.put("confidence", assumption.getConfidence() != null ? assumption.getConfidence() : "Medium");
                assumptionMap.put("impact", assumption.getImpact() != null ? assumption.getImpact() : "Medium");
                return assumptionMap;
            }).toList();
            savedAssumptionsJson = objectMapper.writeValueAsString(assumptionList);
            
        } catch (Exception e) {
            logger.error("Error converting saved normalized data to JSON for product ID: {}", productId, e);
        }
        
        ProductHypothesisResponse response = new ProductHypothesisResponse(
                savedProductHypothesis.getId(),
                savedProductHypothesis.getProduct().getId(),
                savedProductHypothesis.getHypothesisStatement(),
                savedProductHypothesis.getSuccessMetrics(),
                savedAssumptionsJson,
                savedInitiativesJson,
                savedThemesJson,
                savedProductHypothesis.getCreatedAt(),
                savedProductHypothesis.getUpdatedAt()
        );
        
        return ResponseEntity.ok(response);
    }
    
    private boolean hasProductAccess(Long productId, Long userId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            
            // Check if user owns the product
            if (product.getUser().getId().equals(userId)) {
                return true;
            }
            
            // Check if user has role-based access to the product
            if (user.getRole() != null) {
                return user.getRole().getProductModules().stream()
                        .anyMatch(pm -> pm.getProduct().getId().equals(productId));
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Error checking product access for user ID: {} and product ID: {}", userId, productId, e);
            return false;
        }
    }
    
    private void saveThemesToNormalizedTable(Product product, String themesJson) throws Exception {
        if (themesJson == null || themesJson.trim().isEmpty() || "[]".equals(themesJson.trim())) {
            return;
        }
        
        // Get existing themes for this product
        List<Theme> existingThemes = themeRepository.findByProductId(product.getId());
        
        // Parse new themes from JSON
        List<Map<String, Object>> themes = objectMapper.readValue(themesJson, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        
        for (Map<String, Object> themeData : themes) {
            String name = (String) themeData.get("name");
            String description = (String) themeData.get("description");
            String color = (String) themeData.get("color");
            
            if (name != null && !name.trim().isEmpty()) {
                // Try to find existing theme by name
                Optional<Theme> existingTheme = existingThemes.stream()
                        .filter(t -> t.getName().equals(name))
                        .findFirst();
                        
                if (existingTheme.isPresent()) {
                    // Update existing theme
                    Theme theme = existingTheme.get();
                    theme.setDescription(description);
                    String newColor = color != null ? color : "#3498db";
                    if (!newColor.equals(theme.getColor())) {
                        // Color changed - update theme and propagate to related tables
                        theme.setColor(newColor);
                        themeRepository.save(theme);
                        
                        // Update backlog_epics that reference this theme
                        updateBacklogEpicsThemeColor(theme.getId(), newColor, product);
                        
                        // Update roadmap_items that reference this theme
                        updateRoadmapItemsThemeColor(theme.getId().toString(), newColor, product, theme.getName());
                    } else {
                        themeRepository.save(theme);
                    }
                } else {
                    // Create new theme
                    Theme theme = new Theme(product, name, description, color != null ? color : "#3498db");
                    themeRepository.save(theme);
                }
            }
        }
    }
    
    private void updateBacklogEpicsThemeColor(Long themeId, String newColor, Product product) {
        try {
            // Update backlog_epics that reference this theme ID
            List<BacklogEpic> epicsToUpdate = backlogEpicRepository.findByProductId(product.getId())
                    .stream()
                    .filter(epic -> epic.getThemeId() != null && epic.getThemeId().equals(themeId.toString()))
                    .collect(java.util.stream.Collectors.toList());
                    
            for (BacklogEpic epic : epicsToUpdate) {
                epic.setThemeColor(newColor);
                backlogEpicRepository.save(epic);
            }
        } catch (Exception e) {
            logger.warn("Failed to update backlog_epics theme color for theme ID: {}", themeId, e);
        }
    }
    
    private void updateRoadmapItemsThemeColor(String themeId, String newColor, Product product, String themeName) {
        try {
            // Note: For now, roadmap_items sync is handled by the frontend logic that fetches
            // current theme colors from the themes table via QuarterlyRoadmapV2Controller
            // Future enhancement: Add direct SQL update using @Query annotation
            logger.info("Theme color update propagated - roadmap items will sync via frontend theme lookup for theme: {}", themeName);
        } catch (Exception e) {
            logger.warn("Failed to update roadmap_items theme color for theme ID: {}", themeId, e);
        }
    }
    
    @Transactional
    private void saveInitiativesToNormalizedTable(Product product, String initiativesJson) throws Exception {
        if (initiativesJson == null || initiativesJson.trim().isEmpty() || "[]".equals(initiativesJson.trim())) {
            return;
        }
        
        // Clear existing initiatives for this product
        initiativeRepository.deleteByProductId(product.getId());
        
        // Parse and save new initiatives
        List<Map<String, Object>> initiatives = objectMapper.readValue(initiativesJson, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        
        for (Map<String, Object> initiativeData : initiatives) {
            String title = (String) initiativeData.get("title");
            String description = (String) initiativeData.get("description");
            String priority = (String) initiativeData.get("priority");
            String timeline = (String) initiativeData.get("timeline");
            String owner = (String) initiativeData.get("owner");
            
            if (title != null && !title.trim().isEmpty()) {
                Initiative initiative = new Initiative(product, title, description, 
                    priority != null ? priority : "Medium", timeline, owner);
                initiativeRepository.save(initiative);
            }
        }
    }
    
    private void saveAssumptionsToNormalizedTable(Product product, String assumptionsJson) throws Exception {
        if (assumptionsJson == null || assumptionsJson.trim().isEmpty() || "[]".equals(assumptionsJson.trim())) {
            return;
        }
        
        // Clear existing assumptions for this product
        assumptionRepository.deleteByProductId(product.getId());
        
        // Parse and save new assumptions
        List<Map<String, Object>> assumptions = objectMapper.readValue(assumptionsJson, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        
        for (Map<String, Object> assumptionData : assumptions) {
            String assumption = (String) assumptionData.get("assumption");
            String confidence = (String) assumptionData.get("confidence");
            String impact = (String) assumptionData.get("impact");
            
            if (assumption != null && !assumption.trim().isEmpty()) {
                Assumption newAssumption = new Assumption(product, assumption, 
                    confidence != null ? confidence : "Medium", 
                    impact != null ? impact : "Medium");
                assumptionRepository.save(newAssumption);
            }
        }
    }
}