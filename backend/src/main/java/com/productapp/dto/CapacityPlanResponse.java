package com.productapp.dto;

import com.productapp.entity.CapacityPlan;
import com.productapp.entity.EpicEffort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CapacityPlanResponse {
    private Long id;
    private Long productId;
    private Integer year;
    private Integer quarter;
    private String effortUnit;
    private List<TeamResponse> teams;
    private List<EpicEffortResponse> epicEfforts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public CapacityPlanResponse() {}
    
    public CapacityPlanResponse(CapacityPlan capacityPlan) {
        this.id = capacityPlan.getId();
        this.productId = capacityPlan.getProductId();
        this.year = capacityPlan.getYear();
        this.quarter = capacityPlan.getQuarter();
        this.effortUnit = capacityPlan.getEffortUnit();
        this.createdAt = capacityPlan.getCreatedAt();
        this.updatedAt = capacityPlan.getUpdatedAt();
    }
    
    public CapacityPlanResponse(CapacityPlan capacityPlan, List<EpicEffort> epicEfforts) {
        this(capacityPlan);
        this.epicEfforts = epicEfforts.stream()
                .map(EpicEffortResponse::new)
                .collect(Collectors.toList());
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
    
    public String getEffortUnit() {
        return effortUnit;
    }
    
    public void setEffortUnit(String effortUnit) {
        this.effortUnit = effortUnit;
    }
    
    public List<TeamResponse> getTeams() {
        return teams;
    }
    
    public void setTeams(List<TeamResponse> teams) {
        this.teams = teams;
    }
    
    public List<EpicEffortResponse> getEpicEfforts() {
        return epicEfforts;
    }
    
    public void setEpicEfforts(List<EpicEffortResponse> epicEfforts) {
        this.epicEfforts = epicEfforts;
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