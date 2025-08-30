package com.productapp.dto;

import java.time.LocalDateTime;

public class ProductResponse {
    private Long productId;
    private String productName;
    private LocalDateTime createdAt;
    private String slug;
    
    public ProductResponse() {}
    
    public ProductResponse(Long productId, String productName, LocalDateTime createdAt) {
        this.productId = productId;
        this.productName = productName;
        this.createdAt = createdAt;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
}