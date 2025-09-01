package com.productapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.productapp.dto.AuthResponse;
import com.productapp.dto.LoginRequest;
import com.productapp.entity.User;
import com.productapp.repository.UserRepository;
import com.productapp.security.UserPrincipal;
import com.productapp.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // Note: User signup is disabled - users are created by admins only
    
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials",
                content = @Content)
    })
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElse(null);
        
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.warn("Login failed for email: {}", loginRequest.getEmail());
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid email or password");
        }
        
        String jwt = jwtUtil.generateJwtToken(user.getEmail(), user.getId(), 
            user.getOrganization().getId(), user.getIsSuperadmin(), user.getIsGlobalSuperadmin());
        
        return ResponseEntity.ok(new AuthResponse(jwt, user.getId(), user.getEmail(), user.getIsSuperadmin()));
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token", description = "Refresh the current user's JWT token")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token",
                content = @Content)
    })
    public ResponseEntity<?> refreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String newToken = jwtUtil.generateJwtToken(user.getEmail(), user.getId(), 
            user.getOrganization().getId(), user.getIsSuperadmin(), user.getIsGlobalSuperadmin());
        
        return ResponseEntity.ok(new AuthResponse(newToken, user.getId(), user.getEmail(), user.getIsSuperadmin()));
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logout the current user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful",
                content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    })
    public ResponseEntity<?> logoutUser(Authentication authentication) {
        return ResponseEntity.ok(new AuthResponse("User logged out successfully"));
    }
}