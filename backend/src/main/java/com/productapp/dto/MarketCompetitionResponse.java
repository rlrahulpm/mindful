package com.productapp.dto;

import java.time.LocalDateTime;

public class MarketCompetitionResponse {
    
    private Long id;
    private Long productId;
    private String marketSize;
    private String marketGrowth;
    private String targetMarket;
    private String competitors;
    private String competitiveAdvantage;
    private String marketTrends;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public MarketCompetitionResponse() {}
    
    public MarketCompetitionResponse(Long id, Long productId, String marketSize, String marketGrowth, 
                                   String targetMarket, String competitors, String competitiveAdvantage, 
                                   String marketTrends, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.marketSize = marketSize;
        this.marketGrowth = marketGrowth;
        this.targetMarket = targetMarket;
        this.competitors = competitors;
        this.competitiveAdvantage = competitiveAdvantage;
        this.marketTrends = marketTrends;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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