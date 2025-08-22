package com.productapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.productapp.dto.ProductRequest;
import com.productapp.dto.ProductResponse;
import com.productapp.entity.Product;
import com.productapp.entity.ProductModule;
import com.productapp.entity.Module;
import com.productapp.entity.User;
import com.productapp.entity.Role;
import com.productapp.exception.ResourceNotFoundException;
import com.productapp.exception.UnauthorizedException;
import com.productapp.repository.ProductRepository;
import com.productapp.repository.ProductModuleRepository;
import com.productapp.repository.ModuleRepository;
import com.productapp.repository.UserRepository;
import com.productapp.repository.RoleRepository;
import com.productapp.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductModuleRepository productModuleRepository;
    
    @Autowired
    private ModuleRepository moduleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @PostMapping
    @Operation(summary = "Create a new product", description = "Create a new product for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product created successfully",
                content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest productRequest, 
                                         Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        logger.info("Creating product '{}' for user ID: {}", productRequest.getProductName(), userPrincipal.getId());
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        Product product = new Product(productRequest.getProductName(), user);
        Product savedProduct = productRepository.save(product);
        logger.info("Product created successfully with ID: {}", savedProduct.getId());
        
        // Automatically create product-module associations with all active modules
        List<Module> activeModules = moduleRepository.findByIsActiveTrueOrderByDisplayOrder();
        logger.info("Found {} active modules to associate with product ID: {}", activeModules.size(), savedProduct.getId());
        
        for (Module module : activeModules) {
            ProductModule productModule = new ProductModule(savedProduct, module);
            productModuleRepository.save(productModule);
            logger.info("Created product-module association: Product ID {} with Module ID {}", savedProduct.getId(), module.getId());
        }
        
        return ResponseEntity.ok(new ProductResponse(savedProduct.getId(), 
                                                   savedProduct.getProductName(), 
                                                   savedProduct.getCreatedAt()));
    }
    
    @GetMapping
    @Operation(summary = "Get all products", description = "Get all products for the authenticated user (owned + role-accessible)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<List<ProductResponse>> getUserProducts(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        logger.info("Fetching products for user ID: {}", userPrincipal.getId());
        
        // Get user to check their role
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        Set<Product> accessibleProducts = new HashSet<>();
        
        // 1. Add products owned by the user
        List<Product> ownedProducts = productRepository.findByUserId(userPrincipal.getId());
        accessibleProducts.addAll(ownedProducts);
        logger.info("User owns {} products", ownedProducts.size());
        
        // 2. Add products accessible through role permissions
        if (user.getRole() != null) {
            logger.info("User has role: {}", user.getRole().getName());
            
            // Get all product-modules associated with the user's role
            Set<ProductModule> roleProductModules = user.getRole().getProductModules();
            
            // Extract unique products from the product-modules
            Set<Product> roleAccessibleProducts = roleProductModules.stream()
                    .map(ProductModule::getProduct)
                    .collect(Collectors.toSet());
            
            accessibleProducts.addAll(roleAccessibleProducts);
            logger.info("User has access to {} additional products through role", roleAccessibleProducts.size());
        }
        
        // Convert to response DTOs
        List<ProductResponse> productResponses = accessibleProducts.stream()
                .map(product -> new ProductResponse(product.getId(), 
                                                  product.getProductName(), 
                                                  product.getCreatedAt()))
                .sorted((a, b) -> a.getProductName().compareToIgnoreCase(b.getProductName()))
                .collect(Collectors.toList());
        
        logger.info("Returning total of {} accessible products for user ID: {}", productResponses.size(), userPrincipal.getId());
        return ResponseEntity.ok(productResponses);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Get a specific product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product retrieved successfully",
                content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - No access to this product", content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<?> getProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id, 
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        logger.info("Fetching product ID: {} for user ID: {}", id, userPrincipal.getId());
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        // Check if user has access to this product
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        boolean hasAccess = false;
        
        // Check if user owns the product
        if (product.getUser().getId().equals(userPrincipal.getId())) {
            hasAccess = true;
            logger.info("User owns product ID: {}", id);
        }
        // Check if user has role-based access to the product
        else if (user.getRole() != null) {
            hasAccess = user.getRole().getProductModules().stream()
                    .anyMatch(pm -> pm.getProduct().getId().equals(id));
            if (hasAccess) {
                logger.info("User has role-based access to product ID: {}", id);
            }
        }
        
        if (!hasAccess) {
            logger.warn("Unauthorized access attempt - User ID: {} tried to access product ID: {}", 
                       userPrincipal.getId(), id);
            throw new UnauthorizedException("You are not authorized to access this product");
        }
        
        logger.info("Product ID: {} fetched successfully for user ID: {}", id, userPrincipal.getId());
        return ResponseEntity.ok(new ProductResponse(product.getId(), 
                                                   product.getProductName(), 
                                                   product.getCreatedAt()));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully",
                content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - Product belongs to another user", content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<?> updateProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest productRequest,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        logger.info("Updating product ID: {} for user ID: {}", id, userPrincipal.getId());
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        if (!product.getUser().getId().equals(userPrincipal.getId())) {
            logger.warn("Unauthorized update attempt - User ID: {} tried to update product ID: {} owned by user ID: {}", 
                       userPrincipal.getId(), id, product.getUser().getId());
            throw new UnauthorizedException("You are not authorized to update this product");
        }
        
        product.setProductName(productRequest.getProductName());
        Product updatedProduct = productRepository.save(product);
        logger.info("Product ID: {} updated successfully for user ID: {}", id, userPrincipal.getId());
        
        return ResponseEntity.ok(new ProductResponse(updatedProduct.getId(), 
                                                   updatedProduct.getProductName(), 
                                                   updatedProduct.getCreatedAt()));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Delete an existing product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - Product belongs to another user", content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<?> deleteProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        logger.info("Deleting product ID: {} for user ID: {}", id, userPrincipal.getId());
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        if (!product.getUser().getId().equals(userPrincipal.getId())) {
            logger.warn("Unauthorized delete attempt - User ID: {} tried to delete product ID: {} owned by user ID: {}", 
                       userPrincipal.getId(), id, product.getUser().getId());
            throw new UnauthorizedException("You are not authorized to delete this product");
        }
        
        productRepository.delete(product);
        logger.info("Product ID: {} deleted successfully for user ID: {}", id, userPrincipal.getId());
        
        return ResponseEntity.ok("Product deleted successfully");
    }
}