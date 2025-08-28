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
import com.productapp.entity.Product;
import com.productapp.entity.ProductHypothesis;
import com.productapp.entity.User;
import com.productapp.exception.ResourceNotFoundException;
import com.productapp.exception.UnauthorizedException;
import com.productapp.repository.ProductHypothesisRepository;
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
        logger.info("Fetching product hypothesis for product ID: {} by user ID: {}", productId, userPrincipal.getId());
        
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
        
        ProductHypothesisResponse response = new ProductHypothesisResponse(
                productHypothesis.getId(),
                productHypothesis.getProduct().getId(),
                productHypothesis.getHypothesisStatement(),
                productHypothesis.getSuccessMetrics(),
                productHypothesis.getAssumptions(),
                productHypothesis.getInitiatives(),
                productHypothesis.getThemes(),
                productHypothesis.getCreatedAt(),
                productHypothesis.getUpdatedAt()
        );
        
        logger.info("Product hypothesis retrieved successfully for product ID: {}", productId);
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
        logger.info("Saving product hypothesis for product ID: {} by user ID: {}", productId, userPrincipal.getId());
        
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
        productHypothesis.setAssumptions(request.getAssumptions());
        productHypothesis.setInitiatives(request.getInitiatives());
        productHypothesis.setThemes(request.getThemes());
        
        ProductHypothesis savedProductHypothesis = productHypothesisRepository.save(productHypothesis);
        logger.info("Product hypothesis saved successfully for product ID: {}", productId);
        
        ProductHypothesisResponse response = new ProductHypothesisResponse(
                savedProductHypothesis.getId(),
                savedProductHypothesis.getProduct().getId(),
                savedProductHypothesis.getHypothesisStatement(),
                savedProductHypothesis.getSuccessMetrics(),
                savedProductHypothesis.getAssumptions(),
                savedProductHypothesis.getInitiatives(),
                savedProductHypothesis.getThemes(),
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
}