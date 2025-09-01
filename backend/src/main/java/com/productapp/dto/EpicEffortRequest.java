package com.productapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public class EpicEffortRequest {
    
    @NotBlank(message = "Epic ID is required")
    private String epicId;
    
    @NotBlank(message = "Epic name is required")
    private String epicName;
    
    @NotNull(message = "Team ID is required")
    private Long teamId;
    
    @NotNull(message = "Effort days is required")
    @Min(value = 0, message = "Effort days must be 0 or greater")
    private Integer effortDays;
    
    private String notes;
    
    public EpicEffortRequest() {}
    
    public EpicEffortRequest(String epicId, String epicName, Long teamId, Integer effortDays, String notes) {
        this.epicId = epicId;
        this.epicName = epicName;
        this.teamId = teamId;
        this.effortDays = effortDays;
        this.notes = notes;
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
}