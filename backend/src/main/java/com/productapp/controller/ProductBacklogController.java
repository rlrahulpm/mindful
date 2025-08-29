package com.productapp.controller;

import com.productapp.dto.ProductBacklogRequest;
import com.productapp.dto.ProductBacklogResponse;
import com.productapp.entity.ProductBacklog;
import com.productapp.entity.Product;
import com.productapp.repository.ProductBacklogRepository;
import com.productapp.repository.ProductRepository;
import com.productapp.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/api/products/{productId}/backlog")
@Tag(name = "Product Backlog", description = "Product Backlog management endpoints")
public class ProductBacklogController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductBacklogController.class);
    
    @Autowired
    private ProductBacklogRepository productBacklogRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @GetMapping
    @Operation(summary = "Get product backlog", description = "Retrieve product backlog data for a specific product")
    public ResponseEntity<?> getProductBacklog(@PathVariable Long productId, HttpServletRequest request) {
        try {
            // Get user ID from JWT token
            String token = request.getHeader("Authorization").substring(7);
            Long userId = jwtUtil.getUserIdFromJwtToken(token);
            
            logger.info("Fetching product backlog for product ID: {} by user ID: {}", productId, userId);
            
            // Check if user owns the product or has access
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Product product = productOpt.get();
            if (!product.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body("Access denied");
            }
            
            Optional<ProductBacklog> backlogOpt = productBacklogRepository.findByProductId(productId);
            if (backlogOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ProductBacklog backlog = backlogOpt.get();
            ProductBacklogResponse response = new ProductBacklogResponse();
            response.setId(backlog.getId());
            response.setProductId(backlog.getProductId());
            response.setEpics(backlog.getEpics());
            response.setCreatedAt(backlog.getCreatedAt());
            response.setUpdatedAt(backlog.getUpdatedAt());
            
            logger.info("Product backlog retrieved successfully for product ID: {}", productId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching product backlog for product ID: {}", productId, e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
    
    @PostMapping
    @Operation(summary = "Save product backlog", description = "Save or update product backlog data")
    public ResponseEntity<?> saveProductBacklog(
            @PathVariable Long productId, 
            @RequestBody ProductBacklogRequest request,
            HttpServletRequest httpRequest) {
        try {
            // Get user ID from JWT token
            String token = httpRequest.getHeader("Authorization").substring(7);
            Long userId = jwtUtil.getUserIdFromJwtToken(token);
            
            logger.info("Saving product backlog for product ID: {} by user ID: {}", productId, userId);
            
            // Check if user owns the product
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Product product = productOpt.get();
            if (!product.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body("Access denied");
            }
            
            // Find existing or create new
            ProductBacklog backlog = productBacklogRepository.findByProductId(productId)
                    .orElse(new ProductBacklog());
            
            backlog.setProductId(productId);
            backlog.setEpics(request.getEpics());
            
            ProductBacklog savedBacklog = productBacklogRepository.save(backlog);
            
            ProductBacklogResponse response = new ProductBacklogResponse();
            response.setId(savedBacklog.getId());
            response.setProductId(savedBacklog.getProductId());
            response.setEpics(savedBacklog.getEpics());
            response.setCreatedAt(savedBacklog.getCreatedAt());
            response.setUpdatedAt(savedBacklog.getUpdatedAt());
            
            logger.info("Product backlog saved successfully for product ID: {}", productId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error saving product backlog for product ID: {}", productId, e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}