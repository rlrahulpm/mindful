package com.productapp.dto;

import java.time.LocalDateTime;
import java.util.List;

public class RoleResponse {
    
    private Long id;
    private String name;
    private String description;
    private List<ProductModuleResponse> productModules;
    private LocalDateTime createdAt;
    
    public RoleResponse() {}
    
    public RoleResponse(Long id, String name, String description, List<ProductModuleResponse> productModules, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.productModules = productModules;
        this.createdAt = createdAt;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<ProductModuleResponse> getProductModules() {
        return productModules;
    }
    
    public void setProductModules(List<ProductModuleResponse> productModules) {
        this.productModules = productModules;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}