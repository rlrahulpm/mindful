package com.productapp.controller;

import com.productapp.entity.Theme;
import com.productapp.entity.Product;
import com.productapp.service.ThemeService;
import com.productapp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products/{productId}/themes")
@CrossOrigin(origins = "http://localhost:3000")
public class ThemeController {
    
    @Autowired
    private ThemeService themeService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping
    public ResponseEntity<List<Theme>> getThemes(@PathVariable Long productId) {
        List<Theme> themes = themeService.getThemesByProductId(productId);
        return ResponseEntity.ok(themes);
    }
    
    @GetMapping("/{themeId}")
    public ResponseEntity<Theme> getTheme(@PathVariable Long productId, @PathVariable Long themeId) {
        Optional<Theme> theme = themeService.getThemeByIdAndProductId(themeId, productId);
        return theme.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Theme> createTheme(@PathVariable Long productId, @RequestBody Map<String, Object> themeData) {
        try {
            Optional<Product> product = productRepository.findById(productId);
            if (product.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            String name = (String) themeData.get("name");
            String description = (String) themeData.get("description");
            String color = (String) themeData.get("color");
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (color == null || color.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Theme theme = themeService.createTheme(product.get(), name, description, color);
            return ResponseEntity.ok(theme);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{themeId}")
    public ResponseEntity<Theme> updateTheme(@PathVariable Long productId, @PathVariable Long themeId, 
                                           @RequestBody Map<String, Object> themeData) {
        try {
            String name = (String) themeData.get("name");
            String description = (String) themeData.get("description");
            String color = (String) themeData.get("color");
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (color == null || color.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Theme theme = themeService.updateTheme(themeId, productId, name, description, color);
            return ResponseEntity.ok(theme);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{themeId}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long productId, @PathVariable Long themeId) {
        themeService.deleteTheme(themeId, productId);
        return ResponseEntity.ok().build();
    }
}