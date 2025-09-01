package com.productapp.dto;

import java.time.LocalDateTime;

public class ProductBasicsResponse {
    
    private Long id;
    private Long productId;
    private String vision;
    private String targetPersonas;
    private String goals;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public ProductBasicsResponse() {}
    
    public ProductBasicsResponse(Long id, Long productId, String vision, String targetPersonas, 
                               String goals, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.vision = vision;
        this.targetPersonas = targetPersonas;
        this.goals = goals;
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
    
    public String getVision() {
        return vision;
    }
    
    public void setVision(String vision) {
        this.vision = vision;
    }
    
    public String getTargetPersonas() {
        return targetPersonas;
    }
    
    public void setTargetPersonas(String targetPersonas) {
        this.targetPersonas = targetPersonas;
    }
    
    public String getGoals() {
        return goals;
    }
    
    public void setGoals(String goals) {
        this.goals = goals;
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