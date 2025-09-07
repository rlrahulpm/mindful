package com.productapp.controller;

import com.productapp.model.Organization;
import com.productapp.entity.User;
import com.productapp.dto.LoginRequest;
import com.productapp.dto.CRMUserRequest;
import com.productapp.dto.OrganizationRequest;
import com.productapp.exception.ResourceNotFoundException;
import com.productapp.exception.UnauthorizedException;
import com.productapp.repository.OrganizationRepository;
import com.productapp.repository.UserRepository;
import com.productapp.util.JwtUtil;
import com.productapp.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/crm")
@CrossOrigin(origins = "*")
public class CRMController {


    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Login endpoint for CRM (global super admins only)
    @PostMapping("/login")
    public ResponseEntity<?> authenticateCRMUser(@Valid @RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElse(null);
        
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Check if user is global super admin
        if (!user.getIsGlobalSuperadmin()) {
            throw new UnauthorizedException("Access denied. Only global super admins can access Mindful CRM.");
        }

        String jwt = jwtUtil.generateJwtToken(
            user.getEmail(), 
            user.getId(),
            user.getOrganization() != null ? user.getOrganization().getId() : null,
            user.getIsSuperadmin(),
            user.getIsGlobalSuperadmin()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("isGlobalSuperAdmin", user.getIsGlobalSuperadmin());
        
        return ResponseEntity.ok(response);
    }

    // Organization endpoints
    @GetMapping("/organizations")
    public ResponseEntity<List<Organization>> getAllOrganizations(Authentication authentication) {
        validateGlobalSuperAdmin(authentication);
        List<Organization> organizations = organizationRepository.findAll();
        return ResponseEntity.ok(organizations);
    }

    @GetMapping("/organizations/{id}")
    public ResponseEntity<Organization> getOrganization(@PathVariable Long id, Authentication authentication) {
        validateGlobalSuperAdmin(authentication);
        Organization organization = organizationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        return ResponseEntity.ok(organization);
    }

    @PostMapping("/organizations")
    public ResponseEntity<Organization> createOrganization(@Valid @RequestBody OrganizationRequest request, 
                                                          Authentication authentication) {
        validateGlobalSuperAdmin(authentication);
        
        Organization organization = new Organization();
        organization.setName(request.getName());
        Organization savedOrg = organizationRepository.save(organization);
        
        return ResponseEntity.ok(savedOrg);
    }

    @PutMapping("/organizations/{id}")
    public ResponseEntity<Organization> updateOrganization(@PathVariable Long id,
                                                          @Valid @RequestBody OrganizationRequest request,
                                                          Authentication authentication) {
        validateGlobalSuperAdmin(authentication);
        
        Organization organization = organizationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        
        organization.setName(request.getName());
        Organization updatedOrg = organizationRepository.save(organization);
        
        return ResponseEntity.ok(updatedOrg);
    }

    @DeleteMapping("/organizations/{id}")
    public ResponseEntity<?> deleteOrganization(@PathVariable Long id, Authentication authentication) {
        validateGlobalSuperAdmin(authentication);
        
        Organization organization = organizationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        
        // Delete all users in this organization
        List<User> orgUsers = userRepository.findByOrganizationId(id);
        userRepository.deleteAll(orgUsers);
        
        // Delete the organization
        organizationRepository.delete(organization);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Organization deleted successfully");
        return ResponseEntity.ok(response);
    }

    // User management endpoints
    @GetMapping("/organizations/{orgId}/users")
    public ResponseEntity<List<Map<String, Object>>> getUsersByOrganization(@PathVariable Long orgId, 
                                                                             Authentication authentication) {
        validateGlobalSuperAdmin(authentication);
        
        List<User> users = userRepository.findByOrganizationIdAndIsSuperadminTrue(orgId);
        
        List<Map<String, Object>> response = users.stream()
            .map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("email", user.getEmail());
                userMap.put("isSuperadmin", user.getIsSuperadmin());
                userMap.put("isGlobalSuperAdmin", user.getIsGlobalSuperadmin());
                userMap.put("organizationId", user.getOrganization() != null ? user.getOrganization().getId() : null);
                return userMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/superadmins")
    public ResponseEntity<Map<String, Object>> createSuperAdmin(@Valid @RequestBody CRMUserRequest request,
                                                                Authentication authentication) {
        validateGlobalSuperAdmin(authentication);
        
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        Organization organization = organizationRepository.findById(request.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsSuperadmin(true);
        user.setIsGlobalSuperadmin(false);
        user.setOrganization(organization);
        
        User savedUser = userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId());
        response.put("email", savedUser.getEmail());
        response.put("isSuperadmin", savedUser.getIsSuperadmin());
        response.put("isGlobalSuperAdmin", savedUser.getIsGlobalSuperadmin());
        response.put("organizationId", savedUser.getOrganization().getId());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/superadmins/{id}")
    public ResponseEntity<Map<String, Object>> updateSuperAdmin(@PathVariable Long id,
                                                                @Valid @RequestBody CRMUserRequest request,
                                                                Authentication authentication) {
        validateGlobalSuperAdmin(authentication);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        // Check if email is being changed and if new email already exists
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        user.setEmail(request.getEmail());
        
        // Only update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        User updatedUser = userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", updatedUser.getId());
        response.put("email", updatedUser.getEmail());
        response.put("isSuperadmin", updatedUser.getIsSuperadmin());
        response.put("isGlobalSuperAdmin", updatedUser.getIsGlobalSuperadmin());
        response.put("organizationId", updatedUser.getOrganization() != null ? updatedUser.getOrganization().getId() : null);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/superadmin")
    public ResponseEntity<Map<String, Object>> toggleUserSuperAdminStatus(@PathVariable Long id,
                                                                           @RequestBody Map<String, Boolean> request,
                                                                           Authentication authentication) {
        validateGlobalSuperAdmin(authentication);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        // Prevent modification of global superadmins
        if (user.getIsGlobalSuperadmin()) {
            throw new IllegalArgumentException("Cannot modify global superadmin status");
        }
        
        Boolean isSuperadmin = request.get("isSuperadmin");
        if (isSuperadmin != null) {
            user.setIsSuperadmin(isSuperadmin);
            userRepository.save(user);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("isSuperadmin", user.getIsSuperadmin());
        response.put("isGlobalSuperAdmin", user.getIsGlobalSuperadmin());
        response.put("organizationId", user.getOrganization() != null ? user.getOrganization().getId() : null);
        response.put("message", "User superadmin status updated successfully");
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication authentication) {
        validateGlobalSuperAdmin(authentication);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        // Prevent deletion of global superadmins
        if (user.getIsGlobalSuperadmin()) {
            throw new IllegalArgumentException("Cannot delete global superadmin users");
        }
        
        userRepository.delete(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> request,
                                                          Authentication authentication) {
        validateGlobalSuperAdmin(authentication);
        
        String email = (String) request.get("email");
        String password = (String) request.get("password");
        Long organizationId = Long.valueOf(request.get("organizationId").toString());
        
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        
        // Find organization
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        // Create new user
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setOrganization(organization);
        user.setIsSuperadmin(true); // New users created via CRM are superadmins
        user.setIsGlobalSuperadmin(false);
        
        User savedUser = userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId());
        response.put("email", savedUser.getEmail());
        response.put("isSuperadmin", savedUser.getIsSuperadmin());
        response.put("isGlobalSuperAdmin", savedUser.getIsGlobalSuperadmin());
        response.put("organizationId", savedUser.getOrganization().getId());
        response.put("message", "User created successfully");
        
        return ResponseEntity.ok(response);
    }

    private void validateGlobalSuperAdmin(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        if (!user.getIsGlobalSuperadmin()) {
            throw new UnauthorizedException("Access denied. Only global super admins can perform this operation.");
        }
    }
}