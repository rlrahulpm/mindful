package com.productapp.dto;

import jakarta.validation.constraints.Size;

public class MarketCompetitionRequest {
    
    @Size(max = 10000, message = "Market size description cannot exceed 10000 characters")
    private String marketSize;
    
    @Size(max = 10000, message = "Market growth description cannot exceed 10000 characters")
    private String marketGrowth;
    
    @Size(max = 10000, message = "Target market description cannot exceed 10000 characters")
    private String targetMarket;
    
    @Size(max = 10000, message = "Competitors description cannot exceed 10000 characters")
    private String competitors;
    
    @Size(max = 10000, message = "Competitive advantage description cannot exceed 10000 characters")
    private String competitiveAdvantage;
    
    @Size(max = 10000, message = "Market trends description cannot exceed 10000 characters")
    private String marketTrends;
    
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
}