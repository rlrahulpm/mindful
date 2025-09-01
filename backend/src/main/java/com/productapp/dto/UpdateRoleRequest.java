package com.productapp.dto;

import java.util.List;

public class UpdateRoleRequest {
    
    private String name;
    private String description;
    private List<Long> productModuleIds;
    
    public UpdateRoleRequest() {}
    
    public UpdateRoleRequest(String name, String description, List<Long> productModuleIds) {
        this.name = name;
        this.description = description;
        this.productModuleIds = productModuleIds;
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
    
    public List<Long> getProductModuleIds() {
        return productModuleIds;
    }
    
    public void setProductModuleIds(List<Long> productModuleIds) {
        this.productModuleIds = productModuleIds;
    }
}