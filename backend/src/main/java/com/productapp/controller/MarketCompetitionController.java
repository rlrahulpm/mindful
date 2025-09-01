package com.productapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.productapp.dto.MarketCompetitionRequest;
import com.productapp.dto.MarketCompetitionResponse;
import com.productapp.entity.Product;
import com.productapp.entity.MarketCompetition;
import com.productapp.entity.User;
import com.productapp.exception.ResourceNotFoundException;
import com.productapp.exception.UnauthorizedException;
import com.productapp.repository.MarketCompetitionRepository;
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
@RequestMapping("/api/products/{productId}/market-competition")
@Tag(name = "Market Competition", description = "Market and Competition Analysis module management APIs")
@SecurityRequirement(name = "bearerAuth")
public class MarketCompetitionController {
    
    private static final Logger logger = LoggerFactory.getLogger(MarketCompetitionController.class);
    
    @Autowired
    private MarketCompetitionRepository marketCompetitionRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    @Operation(summary = "Get market competition analysis", description = "Get market and competition analysis data for a specific product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Market competition data retrieved successfully",
                content = @Content(schema = @Schema(implementation = MarketCompetitionResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - No access to this product", content = @Content),
        @ApiResponse(responseCode = "404", description = "Product or market competition data not found", content = @Content)
    })
    public ResponseEntity<?> getMarketCompetition(
            @PathVariable Long productId,
            Authentication authentication) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Verify user has access to the product
        if (!hasProductAccess(productId, userPrincipal.getId())) {
            logger.warn("Unauthorized access attempt - User ID: {} tried to access product ID: {}", 
                       userPrincipal.getId(), productId);
            throw new UnauthorizedException("You are not authorized to access this product");
        }
        
        MarketCompetition marketCompetition = marketCompetitionRepository.findByProductId(productId)
                .orElse(null);
        
        if (marketCompetition == null) {
            // Return empty response if no data exists yet
            return ResponseEntity.ok(new MarketCompetitionResponse(null, productId, null, null, null, null, null, null, null, null));
        }
        
        MarketCompetitionResponse response = new MarketCompetitionResponse(
                marketCompetition.getId(),
                marketCompetition.getProduct().getId(),
                marketCompetition.getMarketSize(),
                marketCompetition.getMarketGrowth(),
                marketCompetition.getTargetMarket(),
                marketCompetition.getCompetitors(),
                marketCompetition.getCompetitiveAdvantage(),
                marketCompetition.getMarketTrends(),
                marketCompetition.getCreatedAt(),
                marketCompetition.getUpdatedAt()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(summary = "Create or update market competition analysis", description = "Create or update market and competition analysis data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Market competition data saved successfully",
                content = @Content(schema = @Schema(implementation = MarketCompetitionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - No access to this product", content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<?> saveMarketCompetition(
            @PathVariable Long productId,
            @Valid @RequestBody MarketCompetitionRequest request,
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
        
        MarketCompetition marketCompetition = marketCompetitionRepository.findByProductId(productId)
                .orElse(new MarketCompetition(product));
        
        // Update the fields
        marketCompetition.setMarketSize(request.getMarketSize());
        marketCompetition.setMarketGrowth(request.getMarketGrowth());
        marketCompetition.setTargetMarket(request.getTargetMarket());
        marketCompetition.setCompetitors(request.getCompetitors());
        marketCompetition.setCompetitiveAdvantage(request.getCompetitiveAdvantage());
        marketCompetition.setMarketTrends(request.getMarketTrends());
        
        MarketCompetition savedMarketCompetition = marketCompetitionRepository.save(marketCompetition);
        
        MarketCompetitionResponse response = new MarketCompetitionResponse(
                savedMarketCompetition.getId(),
                savedMarketCompetition.getProduct().getId(),
                savedMarketCompetition.getMarketSize(),
                savedMarketCompetition.getMarketGrowth(),
                savedMarketCompetition.getTargetMarket(),
                savedMarketCompetition.getCompetitors(),
                savedMarketCompetition.getCompetitiveAdvantage(),
                savedMarketCompetition.getMarketTrends(),
                savedMarketCompetition.getCreatedAt(),
                savedMarketCompetition.getUpdatedAt()
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