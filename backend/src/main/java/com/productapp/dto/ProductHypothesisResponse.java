package com.productapp.dto;

import java.time.LocalDateTime;

public class ProductHypothesisResponse {
    
    private Long id;
    private Long productId;
    private String hypothesisStatement;
    private String successMetrics;
    private String assumptions;
    private String initiatives;
    private String themes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public ProductHypothesisResponse() {}
    
    public ProductHypothesisResponse(Long id, Long productId, String hypothesisStatement, 
                                   String successMetrics,
                                   String assumptions, String initiatives, String themes,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.hypothesisStatement = hypothesisStatement;
        this.successMetrics = successMetrics;
        this.assumptions = assumptions;
        this.initiatives = initiatives;
        this.themes = themes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getHypothesisStatement() {
        return hypothesisStatement;
    }
    
    public void setHypothesisStatement(String hypothesisStatement) {
        this.hypothesisStatement = hypothesisStatement;
    }
    
    public String getSuccessMetrics() {
        return successMetrics;
    }
    
    public void setSuccessMetrics(String successMetrics) {
        this.successMetrics = successMetrics;
    }
    
    public String getAssumptions() {
        return assumptions;
    }
    
    public void setAssumptions(String assumptions) {
        this.assumptions = assumptions;
    }
    
    public String getInitiatives() {
        return initiatives;
    }
    
    public void setInitiatives(String initiatives) {
        this.initiatives = initiatives;
    }
    
    public String getThemes() {
        return themes;
    }
    
    public void setThemes(String themes) {
        this.themes = themes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}