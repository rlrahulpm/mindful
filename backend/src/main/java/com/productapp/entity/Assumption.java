package com.productapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "assumptions")
@EntityListeners(AuditingEntityListener.class)
public class Assumption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;
    
    @Column(name = "assumption", nullable = false, columnDefinition = "TEXT")
    private String assumption;
    
    @Column(name = "confidence", nullable = false, length = 50)
    private String confidence; // Low, Medium, High
    
    @Column(name = "impact", nullable = false, length = 50)
    private String impact; // Low, Medium, High
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Assumption() {}
    
    public Assumption(Product product, String assumption, String confidence, String impact) {
        this.product = product;
        this.assumption = assumption;
        this.confidence = confidence;
        this.impact = impact;
    }
    
    // Getters and Setters
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
    
    public String getAssumption() {
        return assumption;
    }
    
    public void setAssumption(String assumption) {
        this.assumption = assumption;
    }
    
    public String getConfidence() {
        return confidence;
    }
    
    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
    
    public String getImpact() {
        return impact;
    }
    
    public void setImpact(String impact) {
        this.impact = impact;
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