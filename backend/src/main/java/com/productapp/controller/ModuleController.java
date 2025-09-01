package com.productapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.productapp.dto.ModuleResponse;
import com.productapp.dto.ProductModuleResponse;
import com.productapp.dto.ProductResponse;
import com.productapp.entity.Module;
import com.productapp.entity.Product;
import com.productapp.entity.ProductModule;
import com.productapp.entity.User;
import com.productapp.exception.ResourceNotFoundException;
import com.productapp.exception.UnauthorizedException;
import com.productapp.repository.ModuleRepository;
import com.productapp.repository.ProductRepository;
import com.productapp.repository.ProductModuleRepository;
import com.productapp.repository.UserRepository;
import com.productapp.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products/{productId}/modules")
@Tag(name = "Modules", description = "Product module management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ModuleController {
    
    private static final Logger logger = LoggerFactory.getLogger(ModuleController.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ModuleRepository moduleRepository;
    
    @Autowired
    private ProductModuleRepository productModuleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    @Operation(summary = "Get product modules", description = "Get all enabled modules for a specific product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Modules retrieved successfully",
                content = @Content(schema = @Schema(implementation = ProductModuleResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - Product belongs to another user", content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<List<ProductModuleResponse>> getProductModules(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId,
            Authentication authentication) {
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // Verify product exists and user has access
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
            
            // Get user to check their role
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
            
            boolean hasAccess = false;
            
            // Check if user owns the product
            if (product.getUser().getId().equals(userPrincipal.getId())) {
                hasAccess = true;
            }
            // Check if user has role-based access to the product
            else if (user.getRole() != null) {
                hasAccess = user.getRole().getProductModules().stream()
                        .anyMatch(pm -> pm.getProduct().getId().equals(productId));
                if (hasAccess) {
                }
            }
            
            if (!hasAccess) {
                logger.warn("Unauthorized access attempt - User ID: {} tried to access product ID: {}", 
                           userPrincipal.getId(), productId);
                throw new UnauthorizedException("You are not authorized to access this product");
            }
            
            // Get product modules - filter based on user access
            List<ProductModule> productModules;
            
            // If user owns the product, they get all modules
            if (product.getUser().getId().equals(userPrincipal.getId())) {
                List<ProductModule> allProductModules = productModuleRepository.findAll();
                productModules = allProductModules.stream()
                        .filter(pm -> pm.getProduct().getId().equals(productId) && pm.getIsEnabled())
                        .collect(Collectors.toList());
            }
            // If user has role-based access, they only get modules they have permission for
            else {
                productModules = user.getRole().getProductModules().stream()
                        .filter(pm -> pm.getProduct().getId().equals(productId) && pm.getIsEnabled())
                        .collect(Collectors.toList());
            }
            
            
            List<ProductModuleResponse> response = productModules.stream()
                    .map(this::convertToProductModuleResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching modules for product ID: {}", productId, e);
            throw e;
        }
    }
    
    
    private ProductModuleResponse convertToProductModuleResponse(ProductModule productModule) {
        ModuleResponse moduleResponse = new ModuleResponse(
                productModule.getModule().getId(),
                productModule.getModule().getName(),
                productModule.getModule().getDescription(),
                productModule.getModule().getIcon(),
                productModule.getModule().getIsActive(),
                productModule.getModule().getDisplayOrder(),
                productModule.getModule().getCreatedAt()
        );
        
        ProductResponse productResponse = convertToProductResponse(productModule.getProduct());
        
        return new ProductModuleResponse(
                productModule.getId(),
                productResponse,
                moduleResponse,
                productModule.getIsEnabled(),
                productModule.getCompletionPercentage(),
                productModule.getCreatedAt()
        );
    }
    
    private ProductResponse convertToProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getCreatedAt()
        );
    }
}