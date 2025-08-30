package com.productapp.util;

import org.springframework.stereotype.Component;

@Component
public class SlugUtil {
    
    /**
     * Convert product name to URL-safe slug
     * Example: "Corrosion Monitoring" -> "corrosion-monitoring"
     */
    public static String toSlug(String productName) {
        if (productName == null) {
            return "";
        }
        return productName.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")  // Remove special characters
                .replaceAll("\\s+", "-")           // Replace spaces with hyphens
                .replaceAll("-+", "-")             // Replace multiple hyphens with single
                .replaceAll("^-|-$", "");          // Remove leading/trailing hyphens
    }
    
    /**
     * Convert slug back to product name for database lookup
     * Example: "corrosion-monitoring" -> "Corrosion Monitoring"
     * Note: This is for case-insensitive lookup
     */
    public static String fromSlug(String slug) {
        if (slug == null) {
            return "";
        }
        // Replace hyphens with spaces for lookup
        return slug.replaceAll("-", " ");
    }
}