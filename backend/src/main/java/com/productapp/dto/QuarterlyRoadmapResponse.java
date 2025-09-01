package com.productapp.dto;

import java.time.LocalDateTime;
import java.util.List;

public class QuarterlyRoadmapResponse {
    private Long id;
    private Long productId;
    private Integer year;
    private Integer quarter;
    private List<QuarterlyRoadmapRequest.RoadmapItem> roadmapItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public QuarterlyRoadmapResponse() {
    }
    
    public QuarterlyRoadmapResponse(Long id, Long productId, Integer year, Integer quarter, List<QuarterlyRoadmapRequest.RoadmapItem> roadmapItems, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.year = year;
        this.quarter = quarter;
        this.roadmapItems = roadmapItems;
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
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public Integer getQuarter() {
        return quarter;
    }
    
    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }
    
    public List<QuarterlyRoadmapRequest.RoadmapItem> getRoadmapItems() {
        return roadmapItems;
    }
    
    public void setRoadmapItems(List<QuarterlyRoadmapRequest.RoadmapItem> roadmapItems) {
        this.roadmapItems = roadmapItems;
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