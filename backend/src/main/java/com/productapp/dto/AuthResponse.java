package com.productapp.dto;

public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private Boolean isSuperadmin;
    private String message;
    
    public AuthResponse() {}
    
    public AuthResponse(String token, Long userId, String email, Boolean isSuperadmin) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.isSuperadmin = isSuperadmin;
    }
    
    public AuthResponse(String message) {
        this.message = message;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Boolean getIsSuperadmin() {
        return isSuperadmin;
    }
    
    public void setIsSuperadmin(Boolean isSuperadmin) {
        this.isSuperadmin = isSuperadmin;
    }
}