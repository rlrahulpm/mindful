package com.productapp.controller;

import com.productapp.entity.Assumption;
import com.productapp.entity.Product;
import com.productapp.service.AssumptionService;
import com.productapp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products/{productId}/assumptions")
@CrossOrigin(origins = "http://localhost:3000")
public class AssumptionController {
    
    @Autowired
    private AssumptionService assumptionService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping
    public ResponseEntity<List<Assumption>> getAssumptions(@PathVariable Long productId) {
        List<Assumption> assumptions = assumptionService.getAssumptionsByProductId(productId);
        return ResponseEntity.ok(assumptions);
    }
    
    @GetMapping("/{assumptionId}")
    public ResponseEntity<Assumption> getAssumption(@PathVariable Long productId, @PathVariable Long assumptionId) {
        Optional<Assumption> assumption = assumptionService.getAssumptionByIdAndProductId(assumptionId, productId);
        return assumption.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Assumption> createAssumption(@PathVariable Long productId, @RequestBody Map<String, Object> assumptionData) {
        try {
            Optional<Product> product = productRepository.findById(productId);
            if (product.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            String assumption = (String) assumptionData.get("assumption");
            String confidence = (String) assumptionData.get("confidence");
            String impact = (String) assumptionData.get("impact");
            
            if (assumption == null || assumption.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (confidence == null || confidence.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (impact == null || impact.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Assumption newAssumption = assumptionService.createAssumption(product.get(), assumption, confidence, impact);
            return ResponseEntity.ok(newAssumption);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{assumptionId}")
    public ResponseEntity<Assumption> updateAssumption(@PathVariable Long productId, @PathVariable Long assumptionId, 
                                                      @RequestBody Map<String, Object> assumptionData) {
        try {
            String assumption = (String) assumptionData.get("assumption");
            String confidence = (String) assumptionData.get("confidence");
            String impact = (String) assumptionData.get("impact");
            
            if (assumption == null || assumption.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (confidence == null || confidence.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (impact == null || impact.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Assumption updatedAssumption = assumptionService.updateAssumption(assumptionId, productId, assumption, confidence, impact);
            return ResponseEntity.ok(updatedAssumption);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{assumptionId}")
    public ResponseEntity<Void> deleteAssumption(@PathVariable Long productId, @PathVariable Long assumptionId) {
        assumptionService.deleteAssumption(assumptionId, productId);
        return ResponseEntity.ok().build();
    }
}