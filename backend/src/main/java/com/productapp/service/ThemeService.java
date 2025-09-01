package com.productapp.service;

import com.productapp.entity.Theme;
import com.productapp.entity.Product;
import com.productapp.repository.ThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ThemeService {
    
    @Autowired
    private ThemeRepository themeRepository;
    
    public List<Theme> getThemesByProduct(Product product) {
        return themeRepository.findByProduct(product);
    }
    
    public List<Theme> getThemesByProductId(Long productId) {
        return themeRepository.findByProductId(productId);
    }
    
    public Optional<Theme> getThemeById(Long id) {
        return themeRepository.findById(id);
    }
    
    public Optional<Theme> getThemeByIdAndProductId(Long id, Long productId) {
        return themeRepository.findByIdAndProductId(id, productId);
    }
    
    public Theme saveTheme(Theme theme) {
        return themeRepository.save(theme);
    }
    
    public Theme createTheme(Product product, String name, String description, String color) {
        if (themeRepository.existsByProductIdAndName(product.getId(), name)) {
            throw new IllegalArgumentException("Theme with name '" + name + "' already exists for this product");
        }
        
        Theme theme = new Theme(product, name, description, color);
        return themeRepository.save(theme);
    }
    
    public Theme updateTheme(Long id, Long productId, String name, String description, String color) {
        Optional<Theme> existingTheme = themeRepository.findByIdAndProductId(id, productId);
        if (existingTheme.isEmpty()) {
            throw new IllegalArgumentException("Theme not found");
        }
        
        Theme theme = existingTheme.get();
        
        if (!theme.getName().equals(name) && 
            themeRepository.existsByProductIdAndName(productId, name)) {
            throw new IllegalArgumentException("Theme with name '" + name + "' already exists for this product");
        }
        
        theme.setName(name);
        theme.setDescription(description);
        theme.setColor(color);
        
        return themeRepository.save(theme);
    }
    
    public void deleteTheme(Long id, Long productId) {
        Optional<Theme> theme = themeRepository.findByIdAndProductId(id, productId);
        if (theme.isPresent()) {
            themeRepository.delete(theme.get());
        }
    }
    
    public void deleteThemesByProductId(Long productId) {
        themeRepository.deleteByProductId(productId);
    }
}