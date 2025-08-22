package com.productapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.productapp.dto.*;
import com.productapp.entity.Module;
import com.productapp.entity.ProductModule;
import com.productapp.entity.Role;
import com.productapp.entity.User;
import com.productapp.exception.ResourceNotFoundException;
import com.productapp.exception.UnauthorizedException;
import com.productapp.repository.ModuleRepository;
import com.productapp.repository.ProductModuleRepository;
import com.productapp.repository.RoleRepository;
import com.productapp.repository.UserRepository;
import com.productapp.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin management APIs - Superadmin only")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private ModuleRepository moduleRepository;
    
    @Autowired
    private ProductModuleRepository productModuleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Check if user is superadmin
    private void checkSuperadmin(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        if (!user.getIsSuperadmin()) {
            throw new UnauthorizedException("Access denied. Superadmin privileges required.");
        }
    }
    
    // Role management endpoints
    @GetMapping("/roles")
    @Operation(summary = "Get all roles", description = "Get all available roles")
    public ResponseEntity<List<RoleResponse>> getAllRoles(Authentication authentication) {
        checkSuperadmin(authentication);
        
        List<Role> roles = roleRepository.findAllOrderByName();
        List<RoleResponse> response = roles.stream()
                .map(this::convertToRoleResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/roles")
    @Operation(summary = "Create role", description = "Create a new role with selected modules")
    public ResponseEntity<RoleResponse> createRole(
            @Valid @RequestBody CreateRoleRequest request,
            Authentication authentication) {
        
        checkSuperadmin(authentication);
        
        if (roleRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Role with name '" + request.getName() + "' already exists");
        }
        
        Role role = new Role(request.getName(), request.getDescription());
        
        // Add product modules to role
        if (request.getProductModuleIds() != null && !request.getProductModuleIds().isEmpty()) {
            Set<ProductModule> productModules = request.getProductModuleIds().stream()
                    .map(productModuleId -> productModuleRepository.findById(productModuleId)
                            .orElseThrow(() -> new ResourceNotFoundException("ProductModule", "id", productModuleId)))
                    .collect(Collectors.toSet());
            role.setProductModules(productModules);
        }
        
        role = roleRepository.save(role);
        
        return ResponseEntity.ok(convertToRoleResponse(role));
    }
    
    @PutMapping("/roles/{roleId}")
    @Operation(summary = "Update role", description = "Update role details and modules")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleRequest request,
            Authentication authentication) {
        
        checkSuperadmin(authentication);
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
        
        if (request.getName() != null) {
            role.setName(request.getName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        
        // Update product modules
        if (request.getProductModuleIds() != null) {
            Set<ProductModule> productModules = request.getProductModuleIds().stream()
                    .map(productModuleId -> productModuleRepository.findById(productModuleId)
                            .orElseThrow(() -> new ResourceNotFoundException("ProductModule", "id", productModuleId)))
                    .collect(Collectors.toSet());
            role.setProductModules(productModules);
        }
        
        role = roleRepository.save(role);
        
        return ResponseEntity.ok(convertToRoleResponse(role));
    }
    
    @DeleteMapping("/roles/{roleId}")
    @Operation(summary = "Delete role", description = "Delete a role")
    public ResponseEntity<Void> deleteRole(
            @PathVariable Long roleId,
            Authentication authentication) {
        
        checkSuperadmin(authentication);
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
        
        roleRepository.delete(role);
        
        return ResponseEntity.ok().build();
    }
    
    // User management endpoints
    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Get all users")
    public ResponseEntity<List<UserResponse>> getAllUsers(Authentication authentication) {
        checkSuperadmin(authentication);
        
        List<User> users = userRepository.findAll();
        List<UserResponse> response = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/users")
    @Operation(summary = "Create user", description = "Create a new user with role")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {
        
        checkSuperadmin(authentication);
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email '" + request.getEmail() + "' already exists");
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsSuperadmin(false);
        
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
            user.setRole(role);
        }
        
        user = userRepository.save(user);
        
        return ResponseEntity.ok(convertToUserResponse(user));
    }
    
    @PutMapping("/users/{userId}")
    @Operation(summary = "Update user", description = "Update user role")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        
        checkSuperadmin(authentication);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
            user.setRole(role);
        }
        
        user = userRepository.save(user);
        
        return ResponseEntity.ok(convertToUserResponse(user));
    }
    
    // Get all modules for role creation
    @GetMapping("/modules")
    @Operation(summary = "Get all modules", description = "Get all available modules")
    public ResponseEntity<List<ModuleResponse>> getAllModules(Authentication authentication) {
        checkSuperadmin(authentication);
        
        List<Module> modules = moduleRepository.findByIsActiveTrueOrderByDisplayOrder();
        List<ModuleResponse> response = modules.stream()
                .map(this::convertToModuleResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    // Get all product modules for role creation
    @GetMapping("/product-modules")
    @Operation(summary = "Get all product modules", description = "Get all available product-module combinations")
    public ResponseEntity<List<ProductModuleResponse>> getAllProductModules(Authentication authentication) {
        checkSuperadmin(authentication);
        
        List<ProductModule> productModules = productModuleRepository.findAll();
        List<ProductModuleResponse> response = productModules.stream()
                .map(this::convertToProductModuleResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    // Get user's role modules - for filtering frontend modules
    @GetMapping("/users/{userId}/role-modules")
    @Operation(summary = "Get user's role modules", description = "Get product-modules accessible to a user based on their role")
    public ResponseEntity<List<ProductModuleResponse>> getUserRoleModules(
            @PathVariable Long userId,
            Authentication authentication) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Users can only access their own role modules, unless they are superadmin
        if (!userPrincipal.getId().equals(userId)) {
            checkSuperadmin(authentication);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        List<ProductModuleResponse> response;
        if (user.getRole() != null) {
            response = user.getRole().getProductModules().stream()
                    .map(this::convertToProductModuleResponse)
                    .collect(Collectors.toList());
        } else {
            response = List.of(); // Empty list if no role assigned
        }
        
        return ResponseEntity.ok(response);
    }
    
    // Helper methods
    private RoleResponse convertToRoleResponse(Role role) {
        List<ProductModuleResponse> productModules = role.getProductModules().stream()
                .map(this::convertToProductModuleResponse)
                .collect(Collectors.toList());
        
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                productModules,
                role.getCreatedAt()
        );
    }
    
    private UserResponse convertToUserResponse(User user) {
        RoleResponse roleResponse = null;
        if (user.getRole() != null) {
            roleResponse = convertToRoleResponse(user.getRole());
        }
        
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getIsSuperadmin(),
                roleResponse,
                user.getCreatedAt()
        );
    }
    
    private ModuleResponse convertToModuleResponse(Module module) {
        return new ModuleResponse(
                module.getId(),
                module.getName(),
                module.getDescription(),
                module.getIcon(),
                module.getIsActive(),
                module.getDisplayOrder(),
                module.getCreatedAt()
        );
    }
    
    private ProductModuleResponse convertToProductModuleResponse(ProductModule productModule) {
        ModuleResponse moduleResponse = convertToModuleResponse(productModule.getModule());
        ProductResponse productResponse = convertToProductResponse(productModule.getProduct());
        return new ProductModuleResponse(
                productModule.getId(),
                productResponse,
                moduleResponse,
                productModule.getIsEnabled(),
                productModule.getCompletionPercentage(),
                productModule.getCreatedAt()
        );
    }
    
    private ProductResponse convertToProductResponse(com.productapp.entity.Product product) {
        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getCreatedAt()
        );
    }
}