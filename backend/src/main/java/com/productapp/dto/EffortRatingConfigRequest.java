package com.productapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class EffortRatingConfigRequest {
    
    @NotBlank(message = "Unit type is required")
    private String unitType; // "SPRINTS" or "DAYS"
    
    @NotNull(message = "Star 1 max is required")
    @Min(value = 1, message = "Star 1 max must be at least 1")
    private Integer star1Max;
    
    @NotNull(message = "Star 2 min is required")
    @Min(value = 1, message = "Star 2 min must be at least 1")
    private Integer star2Min;
    
    @NotNull(message = "Star 2 max is required")
    @Min(value = 1, message = "Star 2 max must be at least 1")
    private Integer star2Max;
    
    @NotNull(message = "Star 3 min is required")
    @Min(value = 1, message = "Star 3 min must be at least 1")
    private Integer star3Min;
    
    @NotNull(message = "Star 3 max is required")
    @Min(value = 1, message = "Star 3 max must be at least 1")
    private Integer star3Max;
    
    @NotNull(message = "Star 4 min is required")
    @Min(value = 1, message = "Star 4 min must be at least 1")
    private Integer star4Min;
    
    @NotNull(message = "Star 4 max is required")
    @Min(value = 1, message = "Star 4 max must be at least 1")
    private Integer star4Max;
    
    @NotNull(message = "Star 5 min is required")
    @Min(value = 1, message = "Star 5 min must be at least 1")
    private Integer star5Min;
    
    public EffortRatingConfigRequest() {}
    
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
}