package com.productapp.dto;

import com.productapp.entity.EpicEffort;
import java.time.LocalDateTime;

public class EpicEffortResponse {
    private Long id;
    private Long capacityPlanId;
    private String epicId;
    private String epicName;
    private Long teamId;
    private String teamName;
    private Integer effortDays;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public EpicEffortResponse() {}
    
    public EpicEffortResponse(EpicEffort epicEffort) {
        this.id = epicEffort.getId();
        this.capacityPlanId = epicEffort.getCapacityPlanId();
        this.epicId = epicEffort.getEpicId();
        this.epicName = epicEffort.getEpicName();
        this.teamId = epicEffort.getTeamId();
        this.effortDays = epicEffort.getEffortDays();
        this.notes = epicEffort.getNotes();
        this.createdAt = epicEffort.getCreatedAt();
        this.updatedAt = epicEffort.getUpdatedAt();
        
        if (epicEffort.getTeam() != null) {
            this.teamName = epicEffort.getTeam().getName();
        }
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getCapacityPlanId() {
        return capacityPlanId;
    }
    
    public void setCapacityPlanId(Long capacityPlanId) {
        this.capacityPlanId = capacityPlanId;
    }
    
    public String getEpicId() {
        return epicId;
    }
    
    public void setEpicId(String epicId) {
        this.epicId = epicId;
    }
    
    public String getEpicName() {
        return epicName;
    }
    
    public void setEpicName(String epicName) {
        this.epicName = epicName;
    }
    
    public Long getTeamId() {
        return teamId;
    }
    
    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
    
    public String getTeamName() {
        return teamName;
    }
    
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    
    public Integer getEffortDays() {
        return effortDays;
    }
    
    public void setEffortDays(Integer effortDays) {
        this.effortDays = effortDays;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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