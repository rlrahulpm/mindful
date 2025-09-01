package com.productapp.dto;

import jakarta.validation.constraints.Size;

public class ProductBasicsRequest {
    
    @Size(max = 5000, message = "Vision cannot exceed 5000 characters")
    private String vision;
    
    @Size(max = 5000, message = "Target personas cannot exceed 5000 characters")
    private String targetPersonas;
    
    @Size(max = 5000, message = "Goals cannot exceed 5000 characters")
    private String goals;
    
    public ProductBasicsRequest() {}
    
    public ProductBasicsRequest(String vision, String targetPersonas, String goals) {
        this.vision = vision;
        this.targetPersonas = targetPersonas;
        this.goals = goals;
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
}