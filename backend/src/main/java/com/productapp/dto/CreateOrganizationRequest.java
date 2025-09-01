package com.productapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new organization")
public class CreateOrganizationRequest {
    
    @Schema(description = "Organization name", example = "Acme Corporation", required = true)
    @NotBlank(message = "Organization name is required")
    @Size(max = 255, message = "Organization name must not exceed 255 characters")
    private String name;
    
    @Schema(description = "Organization description", example = "Leading provider of innovative solutions")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    public CreateOrganizationRequest() {}
    
    public CreateOrganizationRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}