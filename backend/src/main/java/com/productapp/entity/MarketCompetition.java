package com.productapp.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "market_competition")
@EntityListeners(AuditingEntityListener.class)
public class MarketCompetition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;
    
    @Column(name = "market_size", columnDefinition = "TEXT")
    private String marketSize;
    
    @Column(name = "market_growth", columnDefinition = "TEXT")
    private String marketGrowth;
    
    @Column(name = "target_market", columnDefinition = "TEXT")
    private String targetMarket;
    
    @Column(name = "competitors", columnDefinition = "TEXT")
    private String competitors;
    
    @Column(name = "competitive_advantage", columnDefinition = "TEXT")
    private String competitiveAdvantage;
    
    @Column(name = "market_trends", columnDefinition = "TEXT")
    private String marketTrends;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public MarketCompetition() {}
    
    public MarketCompetition(Product product) {
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
    
    public String getMarketSize() {
        return marketSize;
    }
    
    public void setMarketSize(String marketSize) {
        this.marketSize = marketSize;
    }
    
    public String getMarketGrowth() {
        return marketGrowth;
    }
    
    public void setMarketGrowth(String marketGrowth) {
        this.marketGrowth = marketGrowth;
    }
    
    public String getTargetMarket() {
        return targetMarket;
    }
    
    public void setTargetMarket(String targetMarket) {
        this.targetMarket = targetMarket;
    }
    
    public String getCompetitors() {
        return competitors;
    }
    
    public void setCompetitors(String competitors) {
        this.competitors = competitors;
    }
    
    public String getCompetitiveAdvantage() {
        return competitiveAdvantage;
    }
    
    public void setCompetitiveAdvantage(String competitiveAdvantage) {
        this.competitiveAdvantage = competitiveAdvantage;
    }
    
    public String getMarketTrends() {
        return marketTrends;
    }
    
    public void setMarketTrends(String marketTrends) {
        this.marketTrends = marketTrends;
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