package com.productapp.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "backlog_epics")
public class BacklogEpic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "epic_id", nullable = false)
    private String epicId;
    
    @Column(name = "epic_name", nullable = false)
    private String epicName;
    
    @Column(name = "epic_description", columnDefinition = "TEXT")
    private String epicDescription;
    
    @Column(name = "theme_id")
    private String themeId;
    
    @Column(name = "theme_name")
    private String themeName;
    
    @Column(name = "theme_color", length = 7)
    private String themeColor;
    
    @Column(name = "initiative_id")
    private String initiativeId;
    
    @Column(name = "initiative_name")
    private String initiativeName;
    
    @Column(name = "track", length = 100)
    private String track;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public BacklogEpic() {
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
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
    
    public String getThemeId() {
        return themeId;
    }
    
    public void setThemeId(String themeId) {
        this.themeId = themeId;
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
    
    public String getInitiativeId() {
        return initiativeId;
    }
    
    public void setInitiativeId(String initiativeId) {
        this.initiativeId = initiativeId;
    }
    
    public String getInitiativeName() {
        return initiativeName;
    }
    
    public void setInitiativeName(String initiativeName) {
        this.initiativeName = initiativeName;
    }
    
    public String getTrack() {
        return track;
    }
    
    public void setTrack(String track) {
        this.track = track;
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