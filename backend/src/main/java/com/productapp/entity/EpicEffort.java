package com.productapp.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "epic_efforts")
public class EpicEffort {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "capacity_plan_id", nullable = false)
    private Long capacityPlanId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capacity_plan_id", insertable = false, updatable = false)
    @JsonIgnore
    private CapacityPlan capacityPlan;
    
    @Column(name = "epic_id", nullable = false)
    private String epicId; // This references the epic from Product Backlog
    
    @Column(name = "epic_name", nullable = false)
    private String epicName;
    
    @Column(name = "team_id", nullable = false)
    private Long teamId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    @JsonIgnore
    private Team team;
    
    @Column(name = "effort_days", nullable = false)
    private Integer effortDays = 0;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public EpicEffort() {}
    
    public EpicEffort(Long capacityPlanId, String epicId, String epicName, Long teamId, Integer effortDays) {
        this.capacityPlanId = capacityPlanId;
        this.epicId = epicId;
        this.epicName = epicName;
        this.teamId = teamId;
        this.effortDays = effortDays;
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
    
    public CapacityPlan getCapacityPlan() {
        return capacityPlan;
    }
    
    public void setCapacityPlan(CapacityPlan capacityPlan) {
        this.capacityPlan = capacityPlan;
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
    
    public Team getTeam() {
        return team;
    }
    
    public void setTeam(Team team) {
        this.team = team;
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