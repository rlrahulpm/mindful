package com.productapp.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "roadmap_items")
public class RoadmapItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private QuarterlyRoadmap roadmap;
    
    @Column(name = "epic_id", nullable = false)
    private String epicId;
    
    @Column(name = "epic_name", nullable = false)
    private String epicName;
    
    @Column(name = "epic_description", columnDefinition = "TEXT")
    private String epicDescription;
    
    @Column(name = "priority")
    private String priority;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "estimated_effort")
    private String estimatedEffort;
    
    @Column(name = "assigned_team")
    private String assignedTeam;
    
    @Column(name = "reach")
    private Integer reach;
    
    @Column(name = "impact")
    private Integer impact;
    
    @Column(name = "confidence")
    private Integer confidence;
    
    @Column(name = "rice_score")
    private Double riceScore;
    
    @Column(name = "effort_rating")
    private Integer effortRating;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "initiative_name")
    private String initiativeName;
    
    @Column(name = "theme_name")
    private String themeName;
    
    @Column(name = "theme_color", length = 7)
    private String themeColor;
    
    @Column(name = "published")
    private Boolean published = false;
    
    @Column(name = "published_date")
    private LocalDate publishedDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public RoadmapItem() {}
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public QuarterlyRoadmap getRoadmap() {
        return roadmap;
    }
    
    public void setRoadmap(QuarterlyRoadmap roadmap) {
        this.roadmap = roadmap;
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
    
    public String getEpicDescription() {
        return epicDescription;
    }
    
    public void setEpicDescription(String epicDescription) {
        this.epicDescription = epicDescription;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getEstimatedEffort() {
        return estimatedEffort;
    }
    
    public void setEstimatedEffort(String estimatedEffort) {
        this.estimatedEffort = estimatedEffort;
    }
    
    public String getAssignedTeam() {
        return assignedTeam;
    }
    
    public void setAssignedTeam(String assignedTeam) {
        this.assignedTeam = assignedTeam;
    }
    
    public Integer getReach() {
        return reach;
    }
    
    public void setReach(Integer reach) {
        this.reach = reach;
    }
    
    public Integer getImpact() {
        return impact;
    }
    
    public void setImpact(Integer impact) {
        this.impact = impact;
    }
    
    public Integer getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }
    
    public Double getRiceScore() {
        return riceScore;
    }
    
    public void setRiceScore(Double riceScore) {
        this.riceScore = riceScore;
    }
    
    public Integer getEffortRating() {
        return effortRating;
    }
    
    public void setEffortRating(Integer effortRating) {
        this.effortRating = effortRating;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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
    
    public String getInitiativeName() {
        return initiativeName;
    }
    
    public void setInitiativeName(String initiativeName) {
        this.initiativeName = initiativeName;
    }
    
    public String getThemeName() {
        return themeName;
    }
    
    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }
    
    public String getThemeColor() {
        return themeColor;
    }
    
    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }
    
    public Boolean getPublished() {
        return published;
    }
    
    public void setPublished(Boolean published) {
        this.published = published;
    }
    
    public LocalDate getPublishedDate() {
        return publishedDate;
    }
    
    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }
}