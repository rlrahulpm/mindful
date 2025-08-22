package com.productapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Product creation/update request")
public class ProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Schema(description = "Name of the product", example = "iPhone 15 Pro", required = true)
    private String productName;
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
}