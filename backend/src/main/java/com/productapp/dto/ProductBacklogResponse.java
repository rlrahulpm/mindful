package com.productapp.dto;

import java.time.LocalDateTime;

public class ProductBacklogResponse {
    private Long id;
    private Long productId;
    private String epics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public ProductBacklogResponse() {
    }
    
    public ProductBacklogResponse(Long id, Long productId, String epics, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.epics = epics;
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
    
    public String getEpics() {
        return epics;
    }
    
    public void setEpics(String epics) {
        this.epics = epics;
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