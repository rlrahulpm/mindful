package com.productapp.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_hypothesis")
@EntityListeners(AuditingEntityListener.class)
public class ProductHypothesis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;
    
    @Column(name = "hypothesis_statement", columnDefinition = "TEXT")
    private String hypothesisStatement;
    
    @Column(name = "success_metrics", columnDefinition = "TEXT")
    private String successMetrics;
    
    @Column(name = "assumptions", columnDefinition = "TEXT")
    private String assumptions;
    
    @Column(name = "initiatives", columnDefinition = "TEXT")
    private String initiatives;
    
    @Column(name = "themes", columnDefinition = "TEXT")
    private String themes;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public ProductHypothesis() {}
    
    public ProductHypothesis(Product product) {
        this.product = product;
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