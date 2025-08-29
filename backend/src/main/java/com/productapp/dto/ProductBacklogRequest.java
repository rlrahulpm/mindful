package com.productapp.dto;

public class ProductBacklogRequest {
    private String epics;
    
    public ProductBacklogRequest() {
    }
    
    public ProductBacklogRequest(String epics) {
        this.epics = epics;
    }
    
    public String getEpics() {
        return epics;
    }
    
    public void setEpics(String epics) {
        this.epics = epics;
    }
}