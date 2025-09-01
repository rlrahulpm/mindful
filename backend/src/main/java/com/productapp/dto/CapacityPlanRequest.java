package com.productapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;

public class CapacityPlanRequest {
    
    @NotNull(message = "Year is required")
    @Min(value = 2020, message = "Year must be 2020 or later")
    @Max(value = 2050, message = "Year must be 2050 or earlier")
    private Integer year;
    
    @NotNull(message = "Quarter is required")
    @Min(value = 1, message = "Quarter must be between 1 and 4")
    @Max(value = 4, message = "Quarter must be between 1 and 4")
    private Integer quarter;
    
    private String effortUnit = "SPRINTS";
    
    private List<EpicEffortRequest> epicEfforts;
    
    public CapacityPlanRequest() {}
    
    public CapacityPlanRequest(Integer year, Integer quarter, List<EpicEffortRequest> epicEfforts) {
        this.year = year;
        this.quarter = quarter;
        this.epicEfforts = epicEfforts;
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
    
    public String getEffortUnit() {
        return effortUnit;
    }
    
    public void setEffortUnit(String effortUnit) {
        this.effortUnit = effortUnit;
    }
    
    public List<EpicEffortRequest> getEpicEfforts() {
        return epicEfforts;
    }
    
    public void setEpicEfforts(List<EpicEffortRequest> epicEfforts) {
        this.epicEfforts = epicEfforts;
    }
}