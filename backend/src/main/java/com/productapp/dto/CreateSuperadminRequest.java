package com.productapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new superadmin user")
public class CreateSuperadminRequest {
    
    @Schema(description = "User email address", example = "admin@acme.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @Schema(description = "User password (minimum 6 characters)", example = "securepassword123", required = true)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
    
    @Schema(description = "ID of the organization the user belongs to", example = "1", required = true)
    @NotNull(message = "Organization ID is required")
    private Long organizationId;
    
    @Schema(description = "Whether this user should be a global superadmin (can manage all organizations)", example = "false")
    private boolean isGlobalSuperadmin = false;
    
    public CreateSuperadminRequest() {}
    
    public CreateSuperadminRequest(String email, String password, Long organizationId, boolean isGlobalSuperadmin) {
        this.email = email;
        this.password = password;
        this.organizationId = organizationId;
        this.isGlobalSuperadmin = isGlobalSuperadmin;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Long getOrganizationId() {
        return organizationId;
    }
    
    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
    
    public boolean isGlobalSuperadmin() {
        return isGlobalSuperadmin;
    }
    
    public void setGlobalSuperadmin(boolean globalSuperadmin) {
        isGlobalSuperadmin = globalSuperadmin;
    }
}