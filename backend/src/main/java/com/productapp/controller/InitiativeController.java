package com.productapp.controller;

import com.productapp.entity.Initiative;
import com.productapp.entity.Product;
import com.productapp.service.InitiativeService;
import com.productapp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products/{productId}/initiatives")
@CrossOrigin(origins = "http://localhost:3000")
public class InitiativeController {
    
    @Autowired
    private InitiativeService initiativeService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping
    public ResponseEntity<List<Initiative>> getInitiatives(@PathVariable Long productId) {
        List<Initiative> initiatives = initiativeService.getInitiativesByProductId(productId);
        return ResponseEntity.ok(initiatives);
    }
    
    @GetMapping("/{initiativeId}")
    public ResponseEntity<Initiative> getInitiative(@PathVariable Long productId, @PathVariable Long initiativeId) {
        Optional<Initiative> initiative = initiativeService.getInitiativeByIdAndProductId(initiativeId, productId);
        return initiative.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Initiative> createInitiative(@PathVariable Long productId, @RequestBody Map<String, Object> initiativeData) {
        try {
            Optional<Product> product = productRepository.findById(productId);
            if (product.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            String title = (String) initiativeData.get("title");
            String description = (String) initiativeData.get("description");
            String priority = (String) initiativeData.get("priority");
            String timeline = (String) initiativeData.get("timeline");
            String owner = (String) initiativeData.get("owner");
            
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Initiative initiative = initiativeService.createInitiative(product.get(), title, description, priority, timeline, owner);
            return ResponseEntity.ok(initiative);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{initiativeId}")
    public ResponseEntity<Initiative> updateInitiative(@PathVariable Long productId, @PathVariable Long initiativeId, 
                                                      @RequestBody Map<String, Object> initiativeData) {
        try {
            String title = (String) initiativeData.get("title");
            String description = (String) initiativeData.get("description");
            String priority = (String) initiativeData.get("priority");
            String timeline = (String) initiativeData.get("timeline");
            String owner = (String) initiativeData.get("owner");
            
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Initiative initiative = initiativeService.updateInitiative(initiativeId, productId, title, description, priority, timeline, owner);
            return ResponseEntity.ok(initiative);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{initiativeId}")
    public ResponseEntity<Void> deleteInitiative(@PathVariable Long productId, @PathVariable Long initiativeId) {
        initiativeService.deleteInitiative(initiativeId, productId);
        return ResponseEntity.ok().build();
    }
}