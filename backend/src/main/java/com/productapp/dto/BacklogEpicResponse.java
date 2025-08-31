package com.productapp.dto;

import java.time.LocalDateTime;

public class BacklogEpicResponse {
    private Long productId;
    private String epics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BacklogEpicResponse() {}

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getEpics() {
        return epics;
    }

    public void setEpics(String epics) {
        this.epics = epics;
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