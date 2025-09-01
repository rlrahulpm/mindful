package com.productapp.dto;

import jakarta.validation.constraints.Size;

public class ProductHypothesisRequest {
    
    @Size(max = 10000, message = "Hypothesis statement cannot exceed 10000 characters")
    private String hypothesisStatement;
    
    
    @Size(max = 10000, message = "Success metrics cannot exceed 10000 characters")
    private String successMetrics;
    
    @Size(max = 10000, message = "Assumptions cannot exceed 10000 characters")
    private String assumptions;
    
    
    @Size(max = 10000, message = "Initiatives cannot exceed 10000 characters")
    private String initiatives;
    
    @Size(max = 10000, message = "Themes cannot exceed 10000 characters")
    private String themes;
    
    public String getHypothesisStatement() {
        return hypothesisStatement;
    }
    
    public void setHypothesisStatement(String hypothesisStatement) {
        this.hypothesisStatement = hypothesisStatement;
    }
    
    
    public String getSuccessMetrics() {
        return successMetrics;
    }
    
    public void setSuccessMetrics(String successMetrics) {
        this.successMetrics = successMetrics;
    }
    
    public String getAssumptions() {
        return assumptions;
    }
    
    public void setAssumptions(String assumptions) {
        this.assumptions = assumptions;
    }
    
    
    public String getInitiatives() {
        return initiatives;
    }
    
    public void setInitiatives(String initiatives) {
        this.initiatives = initiatives;
    }
    
    public String getThemes() {
        return themes;
    }
    
    public void setThemes(String themes) {
        this.themes = themes;
    }
}