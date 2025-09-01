package com.productapp.dto;

import java.time.LocalDateTime;

public class UserResponse {
    
    private Long id;
    private String email;
    private Boolean isSuperadmin;
    private Boolean isGlobalSuperadmin;
    private RoleResponse role;
    private OrganizationResponse organization;
    private LocalDateTime createdAt;
    
    public UserResponse() {}
    
    public UserResponse(Long id, String email, Boolean isSuperadmin, Boolean isGlobalSuperadmin, 
                       RoleResponse role, OrganizationResponse organization, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.isSuperadmin = isSuperadmin;
        this.isGlobalSuperadmin = isGlobalSuperadmin;
        this.role = role;
        this.organization = organization;
        this.createdAt = createdAt;
    }
    
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
    
    public Boolean getIsGlobalSuperadmin() {
        return isGlobalSuperadmin;
    }
    
    public void setIsGlobalSuperadmin(Boolean isGlobalSuperadmin) {
        this.isGlobalSuperadmin = isGlobalSuperadmin;
    }
    
    public OrganizationResponse getOrganization() {
        return organization;
    }
    
    public void setOrganization(OrganizationResponse organization) {
        this.organization = organization;
    }
}