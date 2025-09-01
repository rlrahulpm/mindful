package com.productapp.dto;

import java.util.List;

public class QuarterlyRoadmapRequest {
    private Long productId;
    private Integer year;
    private Integer quarter;
    private List<RoadmapItem> roadmapItems;
    
    public QuarterlyRoadmapRequest() {
    }
    
    public QuarterlyRoadmapRequest(Long productId, Integer year, Integer quarter, List<RoadmapItem> roadmapItems) {
        this.productId = productId;
        this.year = year;
        this.quarter = quarter;
        this.roadmapItems = roadmapItems;
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
    
    public List<RoadmapItem> getRoadmapItems() {
        return roadmapItems;
    }
    
    public void setRoadmapItems(List<RoadmapItem> roadmapItems) {
        this.roadmapItems = roadmapItems;
    }
    
    public static class RoadmapItem {
        private String epicId;
        private String epicName;
        private String epicDescription;
        private String priority;
        private String status;
        private String estimatedEffort;
        private String assignedTeam;
        private Integer reach;
        private Integer impact;
        private Integer confidence;
        private Integer effort;
        private Double riceScore;
        private Integer effortRating;
        private String startDate;
        private String endDate;
        private String initiativeName;
        private String themeName;
        private String themeColor;
        
        public RoadmapItem() {
        }
        
        public RoadmapItem(String epicId, String epicName, String epicDescription, String priority, String status, String estimatedEffort, String assignedTeam, Integer reach, Integer impact, Integer confidence, Integer effort, Double riceScore) {
            this.epicId = epicId;
            this.epicName = epicName;
            this.epicDescription = epicDescription;
            this.priority = priority;
            this.status = status;
            this.estimatedEffort = estimatedEffort;
            this.assignedTeam = assignedTeam;
            this.reach = reach;
            this.impact = impact;
            this.confidence = confidence;
            this.effort = effort;
            this.riceScore = riceScore;
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
        
        public Integer getEffort() {
            return effort;
        }
        
        public void setEffort(Integer effort) {
            this.effort = effort;
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
        
        public String getStartDate() {
            return startDate;
        }
        
        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }
        
        public String getEndDate() {
            return endDate;
        }
        
        public void setEndDate(String endDate) {
            this.endDate = endDate;
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
    }
}