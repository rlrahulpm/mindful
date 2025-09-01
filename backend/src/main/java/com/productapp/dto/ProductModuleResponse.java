package com.productapp.dto;

import java.time.LocalDateTime;

public class ProductModuleResponse {
    
    private Long id;
    private ProductResponse product;
    private ModuleResponse module;
    private Boolean isEnabled;
    private Integer completionPercentage;
    private LocalDateTime createdAt;
    
    public ProductModuleResponse() {}
    
    public ProductModuleResponse(Long id, ProductResponse product, ModuleResponse module, 
                               Boolean isEnabled, Integer completionPercentage, LocalDateTime createdAt) {
        this.id = id;
        this.product = product;
        this.module = module;
        this.isEnabled = isEnabled;
        this.completionPercentage = completionPercentage;
        this.createdAt = createdAt;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ProductResponse getProduct() {
        return product;
    }
    
    public void setProduct(ProductResponse product) {
        this.product = product;
    }
    
    public ModuleResponse getModule() {
        return module;
    }
    
    public void setModule(ModuleResponse module) {
        this.module = module;
    }
    
    public Boolean getIsEnabled() {
        return isEnabled;
    }
    
    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
    
    public Integer getCompletionPercentage() {
        return completionPercentage;
    }
    
    public void setCompletionPercentage(Integer completionPercentage) {
        this.completionPercentage = completionPercentage;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}