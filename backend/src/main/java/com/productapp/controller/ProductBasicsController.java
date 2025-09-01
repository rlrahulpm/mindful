package com.productapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.productapp.dto.ProductBasicsRequest;
import com.productapp.dto.ProductBasicsResponse;
import com.productapp.entity.Product;
import com.productapp.entity.ProductBasics;
import com.productapp.entity.User;
import com.productapp.exception.ResourceNotFoundException;
import com.productapp.exception.UnauthorizedException;
import com.productapp.repository.ProductBasicsRepository;
import com.productapp.repository.ProductRepository;
import com.productapp.repository.UserRepository;
import com.productapp.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/{productId}/basics")
@Tag(name = "Product Basics", description = "Product Basics module management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProductBasicsController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductBasicsController.class);
    
    @Autowired
    private ProductBasicsRepository productBasicsRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    @Operation(summary = "Get product basics", description = "Get product basics data for a specific product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product basics retrieved successfully",
                content = @Content(schema = @Schema(implementation = ProductBasicsResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - No access to this product", content = @Content),
        @ApiResponse(responseCode = "404", description = "Product or product basics not found", content = @Content)
    })
    public ResponseEntity<?> getProductBasics(
            @PathVariable Long productId,
            Authentication authentication) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Verify user has access to the product
        if (!hasProductAccess(productId, userPrincipal.getId())) {
            logger.warn("Unauthorized access attempt - User ID: {} tried to access product ID: {}", 
                       userPrincipal.getId(), productId);
            throw new UnauthorizedException("You are not authorized to access this product");
        }
        
        ProductBasics productBasics = productBasicsRepository.findByProductId(productId)
                .orElse(null);
        
        if (productBasics == null) {
            // Return empty response if no data exists yet
            return ResponseEntity.ok(new ProductBasicsResponse(null, productId, null, null, null, null, null));
        }
        
        ProductBasicsResponse response = new ProductBasicsResponse(
                productBasics.getId(),
                productBasics.getProduct().getId(),
                productBasics.getVision(),
                productBasics.getTargetPersonas(),
                productBasics.getGoals(),
                productBasics.getCreatedAt(),
                productBasics.getUpdatedAt()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(summary = "Create or update product basics", description = "Create or update product basics data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product basics saved successfully",
                content = @Content(schema = @Schema(implementation = ProductBasicsResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - No access to this product", content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<?> saveProductBasics(
            @PathVariable Long productId,
            @Valid @RequestBody ProductBasicsRequest request,
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
        
        ProductBasics productBasics = productBasicsRepository.findByProductId(productId)
                .orElse(new ProductBasics(product));
        
        // Update the fields
        productBasics.setVision(request.getVision());
        productBasics.setTargetPersonas(request.getTargetPersonas());
        productBasics.setGoals(request.getGoals());
        
        ProductBasics savedProductBasics = productBasicsRepository.save(productBasics);
        
        ProductBasicsResponse response = new ProductBasicsResponse(
                savedProductBasics.getId(),
                savedProductBasics.getProduct().getId(),
                savedProductBasics.getVision(),
                savedProductBasics.getTargetPersonas(),
                savedProductBasics.getGoals(),
                savedProductBasics.getCreatedAt(),
                savedProductBasics.getUpdatedAt()
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
}