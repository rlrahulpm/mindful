package com.productapp.dto;

import java.time.LocalDateTime;

public class ModuleResponse {
    
    private Long moduleId;
    private String name;
    private String description;
    private String icon;
    private Boolean isActive;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    
    public ModuleResponse() {}
    
    public ModuleResponse(Long moduleId, String name, String description, String icon, 
                         Boolean isActive, Integer displayOrder, LocalDateTime createdAt) {
        this.moduleId = moduleId;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.isActive = isActive;
        this.displayOrder = displayOrder;
        this.createdAt = createdAt;
    }
    
    public Long getModuleId() {
        return moduleId;
    }
    
    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}