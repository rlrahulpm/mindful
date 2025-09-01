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
import com.productapp.exception.ResourceNotFoundException;
import com.productapp.exception.UnauthorizedException;
import com.productapp.repository.ProductRepository;
import com.productapp.repository.ProductModuleRepository;
import com.productapp.repository.ModuleRepository;
import com.productapp.repository.UserRepository;
import com.productapp.security.UserPrincipal;
import com.productapp.util.SlugUtil;
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
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        Product product = new Product(productRequest.getProductName(), user);
        product.setOrganization(user.getOrganization()); // Set organization from user
        Product savedProduct = productRepository.save(product);
        // Automatically create product-module associations with all active modules
        List<Module> activeModules = moduleRepository.findByIsActiveTrueOrderByDisplayOrder();
        
        for (Module module : activeModules) {
            ProductModule productModule = new ProductModule(savedProduct, module);
            productModuleRepository.save(productModule);
        }
        
        ProductResponse response = new ProductResponse(savedProduct.getId(), 
                                                       savedProduct.getProductName(), 
                                                       savedProduct.getCreatedAt());
        response.setSlug(SlugUtil.toSlug(savedProduct.getProductName()));
        return ResponseEntity.ok(response);
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
        
        // Get user to check their role
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        Set<Product> accessibleProducts = new HashSet<>();
        
        // 1. Add products owned by the user within their organization
        List<Product> ownedProducts = productRepository.findByUserIdAndOrganizationId(
            userPrincipal.getId(), user.getOrganization().getId());
        accessibleProducts.addAll(ownedProducts);
        
        // 2. Add products accessible through role permissions (within same organization)
        if (user.getRole() != null) {
            // Get all product-modules associated with the user's role
            Set<ProductModule> roleProductModules = user.getRole().getProductModules();
            
            // Extract unique products from the product-modules (filter by organization)
            Set<Product> roleAccessibleProducts = roleProductModules.stream()
                    .map(ProductModule::getProduct)
                    .filter(product -> product.getOrganization() != null && 
                           product.getOrganization().getId().equals(user.getOrganization().getId()))
                    .collect(Collectors.toSet());
            
            accessibleProducts.addAll(roleAccessibleProducts);
        }
        
        // Convert to response DTOs
        List<ProductResponse> productResponses = accessibleProducts.stream()
                .map(product -> {
                    ProductResponse resp = new ProductResponse(product.getId(), 
                                                               product.getProductName(), 
                                                               product.getCreatedAt());
                    resp.setSlug(SlugUtil.toSlug(product.getProductName()));
                    return resp;
                })
                .sorted((a, b) -> a.getProductName().compareToIgnoreCase(b.getProductName()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productResponses);
    }
    
    @GetMapping("/by-slug/{slug}")
    @Operation(summary = "Get product by slug", description = "Get a specific product by its URL slug")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product retrieved successfully",
                content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - No access to this product", content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    public ResponseEntity<?> getProductBySlug(
            @Parameter(description = "Product slug", required = true)
            @PathVariable String slug, 
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Get user for organization ID
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        // Find product by name (case-insensitive) and organization
        String productName = SlugUtil.fromSlug(slug);
        Product product = productRepository.findByProductNameAndOrganizationId(productName, user.getOrganization().getId())
                .orElseGet(() -> {
                    // Try case-insensitive search
                    List<Product> products = productRepository.findByOrganizationId(user.getOrganization().getId());
                    return products.stream()
                            .filter(p -> p.getProductName().equalsIgnoreCase(productName))
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
                });
        
        // Check if user has access to this product
        boolean hasAccess = false;
        
        // Check if user owns the product
        if (product.getUser().getId().equals(userPrincipal.getId())) {
            hasAccess = true;
        }
        // Check if user has role-based access to the product
        else if (user.getRole() != null) {
            hasAccess = user.getRole().getProductModules().stream()
                    .anyMatch(pm -> pm.getProduct().getId().equals(product.getId()));
        }
        
        if (!hasAccess) {
            logger.warn("Unauthorized access attempt - User ID: {} tried to access product: {}", 
                       userPrincipal.getId(), product.getProductName());
            throw new UnauthorizedException("You are not authorized to access this product");
        }
        
        ProductResponse response = new ProductResponse(product.getId(), 
                                                       product.getProductName(), 
                                                       product.getCreatedAt());
        response.setSlug(SlugUtil.toSlug(product.getProductName()));
        return ResponseEntity.ok(response);
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
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        // Check if user has access to this product
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
                    .anyMatch(pm -> pm.getProduct().getId().equals(id));
        }
        
        if (!hasAccess) {
            logger.warn("Unauthorized access attempt - User ID: {} tried to access product ID: {}", 
                       userPrincipal.getId(), id);
            throw new UnauthorizedException("You are not authorized to access this product");
        }
        
        ProductResponse response = new ProductResponse(product.getId(), 
                                                       product.getProductName(), 
                                                       product.getCreatedAt());
        response.setSlug(SlugUtil.toSlug(product.getProductName()));
        return ResponseEntity.ok(response);
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
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        if (!product.getUser().getId().equals(userPrincipal.getId())) {
            logger.warn("Unauthorized update attempt - User ID: {} tried to update product ID: {} owned by user ID: {}", 
                       userPrincipal.getId(), id, product.getUser().getId());
            throw new UnauthorizedException("You are not authorized to update this product");
        }
        
        product.setProductName(productRequest.getProductName());
        Product updatedProduct = productRepository.save(product);
        
        ProductResponse response = new ProductResponse(updatedProduct.getId(), 
                                                       updatedProduct.getProductName(), 
                                                       updatedProduct.getCreatedAt());
        response.setSlug(SlugUtil.toSlug(updatedProduct.getProductName()));
        return ResponseEntity.ok(response);
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
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        if (!product.getUser().getId().equals(userPrincipal.getId())) {
            logger.warn("Unauthorized delete attempt - User ID: {} tried to delete product ID: {} owned by user ID: {}", 
                       userPrincipal.getId(), id, product.getUser().getId());
            throw new UnauthorizedException("You are not authorized to delete this product");
        }
        
        productRepository.delete(product);
        
        return ResponseEntity.ok("Product deleted successfully");
    }
}