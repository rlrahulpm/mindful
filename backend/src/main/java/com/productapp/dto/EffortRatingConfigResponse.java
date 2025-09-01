package com.productapp.dto;

import com.productapp.entity.EffortRatingConfig;
import java.time.LocalDateTime;

public class EffortRatingConfigResponse {
    private Long id;
    private Long productId;
    private String unitType;
    private Integer star1Max;
    private Integer star2Min;
    private Integer star2Max;
    private Integer star3Min;
    private Integer star3Max;
    private Integer star4Min;
    private Integer star4Max;
    private Integer star5Min;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public EffortRatingConfigResponse() {}
    
    public EffortRatingConfigResponse(EffortRatingConfig config) {
        this.id = config.getId();
        this.productId = config.getProductId();
        this.unitType = config.getUnitType();
        this.star1Max = config.getStar1Max();
        this.star2Min = config.getStar2Min();
        this.star2Max = config.getStar2Max();
        this.star3Min = config.getStar3Min();
        this.star3Max = config.getStar3Max();
        this.star4Min = config.getStar4Min();
        this.star4Max = config.getStar4Max();
        this.star5Min = config.getStar5Min();
        this.createdAt = config.getCreatedAt();
        this.updatedAt = config.getUpdatedAt();
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
    
    public String getUnitType() {
        return unitType;
    }
    
    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }
    
    public Integer getStar1Max() {
        return star1Max;
    }
    
    public void setStar1Max(Integer star1Max) {
        this.star1Max = star1Max;
    }
    
    public Integer getStar2Min() {
        return star2Min;
    }
    
    public void setStar2Min(Integer star2Min) {
        this.star2Min = star2Min;
    }
    
    public Integer getStar2Max() {
        return star2Max;
    }
    
    public void setStar2Max(Integer star2Max) {
        this.star2Max = star2Max;
    }
    
    public Integer getStar3Min() {
        return star3Min;
    }
    
    public void setStar3Min(Integer star3Min) {
        this.star3Min = star3Min;
    }
    
    public Integer getStar3Max() {
        return star3Max;
    }
    
    public void setStar3Max(Integer star3Max) {
        this.star3Max = star3Max;
    }
    
    public Integer getStar4Min() {
        return star4Min;
    }
    
    public void setStar4Min(Integer star4Min) {
        this.star4Min = star4Min;
    }
    
    public Integer getStar4Max() {
        return star4Max;
    }
    
    public void setStar4Max(Integer star4Max) {
        this.star4Max = star4Max;
    }
    
    public Integer getStar5Min() {
        return star5Min;
    }
    
    public void setStar5Min(Integer star5Min) {
        this.star5Min = star5Min;
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