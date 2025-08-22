package com.productapp.dto;

import java.time.LocalDateTime;

public class UserResponse {
    
    private Long id;
    private String email;
    private Boolean isSuperadmin;
    private RoleResponse role;
    private LocalDateTime createdAt;
    
    public UserResponse() {}
    
    public UserResponse(Long id, String email, Boolean isSuperadmin, RoleResponse role, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.isSuperadmin = isSuperadmin;
        this.role = role;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Boolean getIsSuperadmin() {
        return isSuperadmin;
    }
    
    public void setIsSuperadmin(Boolean isSuperadmin) {
        this.isSuperadmin = isSuperadmin;
    }
    
    public RoleResponse getRole() {
        return role;
    }
    
    public void setRole(RoleResponse role) {
        this.role = role;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}