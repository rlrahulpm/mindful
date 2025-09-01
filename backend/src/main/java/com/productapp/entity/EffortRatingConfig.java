package com.productapp.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "effort_rating_configs")
public class EffortRatingConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "unit_type", nullable = false)
    private String unitType; // "SPRINTS" or "DAYS"
    
    @Column(name = "star_1_max", nullable = false)
    private Integer star1Max = 2; // <= 2 units
    
    @Column(name = "star_2_min", nullable = false)
    private Integer star2Min = 3;
    
    @Column(name = "star_2_max", nullable = false)
    private Integer star2Max = 4; // 3-4 units
    
    @Column(name = "star_3_min", nullable = false)
    private Integer star3Min = 5;
    
    @Column(name = "star_3_max", nullable = false)
    private Integer star3Max = 6; // 5-6 units
    
    @Column(name = "star_4_min", nullable = false)
    private Integer star4Min = 7;
    
    @Column(name = "star_4_max", nullable = false)
    private Integer star4Max = 8; // 7-8 units
    
    @Column(name = "star_5_min", nullable = false)
    private Integer star5Min = 9; // >= 9 units
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public EffortRatingConfig() {}
    
    public static EffortRatingConfig createDefaultForSprints(Long productId) {
        EffortRatingConfig config = new EffortRatingConfig();
        config.setProductId(productId);
        config.setUnitType("SPRINTS");
        config.setStar1Max(2);
        config.setStar2Min(3);
        config.setStar2Max(4);
        config.setStar3Min(5);
        config.setStar3Max(6);
        config.setStar4Min(7);
        config.setStar4Max(8);
        config.setStar5Min(9);
        return config;
    }
    
    public static EffortRatingConfig createDefaultForDays(Long productId) {
        EffortRatingConfig config = new EffortRatingConfig();
        config.setProductId(productId);
        config.setUnitType("DAYS");
        config.setStar1Max(10);
        config.setStar2Min(11);
        config.setStar2Max(20);
        config.setStar3Min(21);
        config.setStar3Max(30);
        config.setStar4Min(31);
        config.setStar4Max(40);
        config.setStar5Min(41);
        return config;
    }
    
    public Integer calculateStarRating(Integer totalEffort) {
        if (totalEffort == null || totalEffort == 0) {
            return 1;
        }
        
        if (totalEffort <= star1Max) {
            return 1;
        } else if (totalEffort >= star2Min && totalEffort <= star2Max) {
            return 2;
        } else if (totalEffort >= star3Min && totalEffort <= star3Max) {
            return 3;
        } else if (totalEffort >= star4Min && totalEffort <= star4Max) {
            return 4;
        } else if (totalEffort >= star5Min) {
            return 5;
        }
        
        // Handle gaps in configuration
        if (totalEffort < star2Min) {
            return 1;
        } else if (totalEffort < star3Min) {
            return 2;
        } else if (totalEffort < star4Min) {
            return 3;
        } else if (totalEffort < star5Min) {
            return 4;
        }
        
        return 5;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getUnitType() {
        return unitType;
    }
    
    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }
    
    public Integer getStar1Max() {
        return star1Max;
    }
    
    public void setStar1Max(Integer star1Max) {
        this.star1Max = star1Max;
    }
    
    public Integer getStar2Min() {
        return star2Min;
    }
    
    public void setStar2Min(Integer star2Min) {
        this.star2Min = star2Min;
    }
    
    public Integer getStar2Max() {
        return star2Max;
    }
    
    public void setStar2Max(Integer star2Max) {
        this.star2Max = star2Max;
    }
    
    public Integer getStar3Min() {
        return star3Min;
    }
    
    public void setStar3Min(Integer star3Min) {
        this.star3Min = star3Min;
    }
    
    public Integer getStar3Max() {
        return star3Max;
    }
    
    public void setStar3Max(Integer star3Max) {
        this.star3Max = star3Max;
    }
    
    public Integer getStar4Min() {
        return star4Min;
    }
    
    public void setStar4Min(Integer star4Min) {
        this.star4Min = star4Min;
    }
    
    public Integer getStar4Max() {
        return star4Max;
    }
    
    public void setStar4Max(Integer star4Max) {
        this.star4Max = star4Max;
    }
    
    public Integer getStar5Min() {
        return star5Min;
    }
    
    public void setStar5Min(Integer star5Min) {
        this.star5Min = star5Min;
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