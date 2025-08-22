package com.productapp.security;

public class UserPrincipal {
    private Long id;
    private String email;
    private Long organizationId;
    private Boolean isSuperadmin;
    private Boolean isGlobalSuperadmin;
    
    public UserPrincipal(Long id, String email, Long organizationId, Boolean isSuperadmin, Boolean isGlobalSuperadmin) {
        this.id = id;
        this.email = email;
        this.organizationId = organizationId;
        this.isSuperadmin = isSuperadmin;
        this.isGlobalSuperadmin = isGlobalSuperadmin;
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
    
    public Long getOrganizationId() {
        return organizationId;
    }
    
    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
    
    public Boolean getIsSuperadmin() {
        return isSuperadmin;
    }
    
    public void setIsSuperadmin(Boolean isSuperadmin) {
        this.isSuperadmin = isSuperadmin;
    }
    
    public Boolean getIsGlobalSuperadmin() {
        return isGlobalSuperadmin;
    }
    
    public void setIsGlobalSuperadmin(Boolean isGlobalSuperadmin) {
        this.isGlobalSuperadmin = isGlobalSuperadmin;
    }
}