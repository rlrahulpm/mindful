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
import com.productapp.model.Organization;
import com.productapp.exception.ResourceNotFoundException;
import com.productapp.exception.UnauthorizedException;
import com.productapp.repository.ModuleRepository;
import com.productapp.repository.OrganizationRepository;
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
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    // Check if user is superadmin
    private void checkSuperadmin(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        if (!user.getIsSuperadmin()) {
            throw new UnauthorizedException("Access denied. Superadmin privileges required.");
        }
    }
    
    // Check if user is global superadmin
    private void checkGlobalSuperadmin(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        if (!user.getIsGlobalSuperadmin()) {
            throw new UnauthorizedException("Access denied. Global superadmin privileges required.");
        }
    }
    
    // Organization management endpoints (Global Superadmin only)
    @GetMapping("/organizations")
    @Operation(summary = "Get all organizations", description = "Get all organizations (Global Superadmin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved organizations",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Global superadmin access required")
    })
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations(Authentication authentication) {
        checkGlobalSuperadmin(authentication);
        
        List<Organization> organizations = organizationRepository.findAll();
        List<OrganizationResponse> response = organizations.stream()
                .map(this::convertToOrganizationResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/organizations")
    @Operation(summary = "Create organization", description = "Create a new organization (Global Superadmin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid input or organization name already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Global superadmin access required")
    })
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody @Parameter(description = "Organization creation request") CreateOrganizationRequest request,
            Authentication authentication) {
        
        checkGlobalSuperadmin(authentication);
        
        if (organizationRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Organization with name '" + request.getName() + "' already exists");
        }
        
        Organization organization = new Organization(request.getName(), request.getDescription());
        organization = organizationRepository.save(organization);
        
        return ResponseEntity.ok(convertToOrganizationResponse(organization));
    }
    
    // Superadmin user management endpoints (Global Superadmin only)
    @PostMapping("/superadmins")
    @Operation(summary = "Create superadmin user", description = "Create a new superadmin user (Global Superadmin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Superadmin user created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid input, email already exists, or organization not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Global superadmin access required")
    })
    public ResponseEntity<UserResponse> createSuperadmin(
            @Valid @RequestBody @Parameter(description = "Superadmin user creation request") CreateSuperadminRequest request,
            Authentication authentication) {
        
        checkGlobalSuperadmin(authentication);
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email '" + request.getEmail() + "' already exists");
        }
        
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsSuperadmin(true);
        user.setIsGlobalSuperadmin(request.isGlobalSuperadmin());
        user.setOrganization(organization);
        
        user = userRepository.save(user);
        
        return ResponseEntity.ok(convertToUserResponse(user));
    }
    
    // Role management endpoints
    @GetMapping("/roles")
    @Operation(summary = "Get all roles", description = "Get all available roles (organization-scoped for org admins, all for global superadmins)")
    public ResponseEntity<List<RoleResponse>> getAllRoles(Authentication authentication) {
        checkSuperadmin(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        logger.info("User {} (ID: {}) requesting roles. IsGlobalSuperadmin: {}, Organization: {}",
                   currentUser.getEmail(), currentUser.getId(), currentUser.getIsGlobalSuperadmin(),
                   currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : "NULL");
        
        List<Role> roles;
        // Both global superadmins and organization superadmins should only see roles used in their organization
        if (currentUser.getOrganization() == null) {
            throw new IllegalStateException("User organization is null - cannot filter roles");
        }
        
        // Get roles that are actually assigned to users in the same organization
        List<User> organizationUsers = userRepository.findByOrganizationId(currentUser.getOrganization().getId());
        roles = organizationUsers.stream()
                .filter(user -> user.getRole() != null)
                .map(User::getRole)
                .distinct()
                .collect(Collectors.toList());
        
        if (currentUser.getIsGlobalSuperadmin() != null && currentUser.getIsGlobalSuperadmin()) {
            logger.info("Global superadmin - returning {} roles used in their organization {} (restricted access)", 
                       roles.size(), currentUser.getOrganization().getId());
        } else {
            logger.info("Organization superadmin - returning {} roles used in organization {}", 
                       roles.size(), currentUser.getOrganization().getId());
        }
        
        List<RoleResponse> response = roles.stream()
                .map(role -> convertToRoleResponse(role, currentUser))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/roles")
    @Operation(summary = "Create role", description = "Create a new role with selected modules")
    public ResponseEntity<RoleResponse> createRole(
            @Valid @RequestBody CreateRoleRequest request,
            Authentication authentication) {
        
        checkSuperadmin(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        logger.info("User {} (ID: {}) creating role '{}'. IsGlobalSuperadmin: {}, Organization: {}", 
                currentUser.getEmail(), currentUser.getId(), request.getName(),
                currentUser.getIsGlobalSuperadmin(), 
                currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : "NULL");
        
        if (roleRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Role with name '" + request.getName() + "' already exists");
        }
        
        Role role = new Role(request.getName(), request.getDescription());
        
        // Add product modules to role with organization validation
        if (request.getProductModuleIds() != null && !request.getProductModuleIds().isEmpty()) {
            logger.info("Processing {} product module IDs for role creation", request.getProductModuleIds().size());
            
            Set<ProductModule> productModules = request.getProductModuleIds().stream()
                    .map(productModuleId -> {
                        ProductModule pm = productModuleRepository.findById(productModuleId)
                                .orElseThrow(() -> new ResourceNotFoundException("ProductModule", "id", productModuleId));
                        
                        // Validate that the product module belongs to the user's organization
                        if (currentUser.getOrganization() == null) {
                            throw new IllegalStateException("User organization is null - cannot create role");
                        }
                        
                        if (pm.getProduct() == null || pm.getProduct().getOrganization() == null) {
                            throw new IllegalStateException("ProductModule " + productModuleId + " has no valid product or organization");
                        }
                        
                        if (!pm.getProduct().getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                            throw new IllegalArgumentException("ProductModule " + productModuleId + " does not belong to your organization");
                        }
                        
                        logger.info("Validated ProductModule ID: {} belongs to organization: {}", 
                                productModuleId, pm.getProduct().getOrganization().getId());
                        return pm;
                    })
                    .collect(Collectors.toSet());
            role.setProductModules(productModules);
            logger.info("Successfully validated and assigned {} product modules to role", productModules.size());
        }
        
        role = roleRepository.save(role);
        logger.info("Role '{}' created successfully with ID: {}", role.getName(), role.getId());
        
        return ResponseEntity.ok(convertToRoleResponse(role, currentUser));
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
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        return ResponseEntity.ok(convertToRoleResponse(role, currentUser));
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
    @Operation(summary = "Get users", description = "Get users (organization-scoped for org admins, all users for global superadmins)")
    public ResponseEntity<List<UserResponse>> getAllUsers(Authentication authentication) {
        checkSuperadmin(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        logger.info("User {} (ID: {}) requesting users. IsGlobalSuperadmin: {}, Organization: {}", 
                   currentUser.getEmail(), currentUser.getId(), currentUser.getIsGlobalSuperadmin(),
                   currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : "NULL");
        
        List<User> users;
        // Both global superadmins and organization superadmins can only see users in their organization
        if (currentUser.getOrganization() == null) {
            throw new IllegalStateException("User organization is null - cannot filter users");
        }
        users = userRepository.findByOrganizationId(currentUser.getOrganization().getId());
        
        if (currentUser.getIsGlobalSuperadmin() != null && currentUser.getIsGlobalSuperadmin()) {
            logger.info("Global superadmin - returning {} users from their organization {} (restricted access)", 
                       users.size(), currentUser.getOrganization().getId());
        } else {
            logger.info("Organization superadmin - returning {} users from organization {}", 
                       users.size(), currentUser.getOrganization().getId());
        }
        
        List<UserResponse> response = users.stream()
                .map(user -> {
                    logger.info("Processing user: email={}, id={}, roleId={}, roleName={}", 
                               user.getEmail(), user.getId(), 
                               user.getRole() != null ? user.getRole().getId() : "NULL",
                               user.getRole() != null ? user.getRole().getName() : "NULL");
                    return convertToUserResponse(user, currentUser);
                })
                .collect(Collectors.toList());
        
        logger.info("Final response contains {} users", response.size());
        response.forEach(userResp -> {
            logger.info("Response user: email={}, roleResponseName={}", 
                       userResp.getEmail(), 
                       userResp.getRole() != null ? userResp.getRole().getName() : "NULL");
        });
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/users")
    @Operation(summary = "Create user", description = "Create a new user with role")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {
        
        checkSuperadmin(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        logger.info("User {} (ID: {}) creating user '{}'. IsGlobalSuperadmin: {}, Organization: {}", 
                currentUser.getEmail(), currentUser.getId(), request.getEmail(),
                currentUser.getIsGlobalSuperadmin(), 
                currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : "NULL");
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email '" + request.getEmail() + "' already exists");
        }
        
        // Use the current user's organization instead of requiring organizationId in request
        if (currentUser.getOrganization() == null) {
            throw new IllegalStateException("User organization is null - cannot create user");
        }
        
        Organization organization = currentUser.getOrganization();
        logger.info("Assigning new user to organization: {} ({})", organization.getId(), organization.getName());
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsSuperadmin(false);
        user.setIsGlobalSuperadmin(false);
        user.setOrganization(organization);
        
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
            
            // Validate that the role's product modules belong to the same organization
            if (role.getProductModules() != null && !role.getProductModules().isEmpty()) {
                for (ProductModule pm : role.getProductModules()) {
                    if (pm.getProduct() == null || pm.getProduct().getOrganization() == null ||
                        !pm.getProduct().getOrganization().getId().equals(organization.getId())) {
                        throw new IllegalArgumentException("Role contains modules from different organization");
                    }
                }
            }
            
            user.setRole(role);
            logger.info("Assigned role '{}' (ID: {}) to new user", role.getName(), role.getId());
        }
        
        user = userRepository.save(user);
        logger.info("User '{}' created successfully with ID: {}", user.getEmail(), user.getId());
        
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
    @Operation(summary = "Get product modules", description = "Get available product-module combinations (organization-scoped for org admins, all for global superadmins)")
    public ResponseEntity<List<ProductModuleResponse>> getAllProductModules(Authentication authentication) {
        checkSuperadmin(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        
        List<ProductModule> productModules;
        // Both global superadmins and organization superadmins can only see product modules from their organization
        if (currentUser.getOrganization() == null) {
            throw new IllegalStateException("User organization is null - cannot filter product modules");
        }
        productModules = productModuleRepository.findByProductOrganizationId(currentUser.getOrganization().getId());
        
        if (currentUser.getIsGlobalSuperadmin() != null && currentUser.getIsGlobalSuperadmin()) {
            logger.info("Global superadmin - returning {} product modules from their organization {} (restricted access)", 
                       productModules.size(), currentUser.getOrganization().getId());
        } else {
            logger.info("Organization superadmin - returning {} product modules from organization {}", 
                       productModules.size(), currentUser.getOrganization().getId());
        }
        
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
                    .filter(pm -> pm.getProduct().getOrganization() != null && 
                                 pm.getProduct().getOrganization().getId().equals(user.getOrganization().getId()))
                    .map(this::convertToProductModuleResponse)
                    .collect(Collectors.toList());
        } else {
            response = List.of(); // Empty list if no role assigned
        }
        
        return ResponseEntity.ok(response);
    }
    
    // Helper methods
    private RoleResponse convertToRoleResponse(Role role, User currentUser) {
        List<ProductModuleResponse> productModules;
        
        if (currentUser.getIsGlobalSuperadmin() != null && currentUser.getIsGlobalSuperadmin()) {
            productModules = role.getProductModules().stream()
                    .map(this::convertToProductModuleResponse)
                    .collect(Collectors.toList());
        } else {
            productModules = role.getProductModules().stream()
                    .filter(pm -> pm.getProduct().getOrganization() != null && 
                                 pm.getProduct().getOrganization().getId().equals(currentUser.getOrganization().getId()))
                    .map(this::convertToProductModuleResponse)
                    .collect(Collectors.toList());
        }
        
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                productModules,
                role.getCreatedAt()
        );
    }
    
    private UserResponse convertToUserResponse(User user) {
        return convertToUserResponse(user, user);
    }
    
    private UserResponse convertToUserResponse(User user, User currentUser) {
        RoleResponse roleResponse = null;
        if (user.getRole() != null) {
            roleResponse = convertToRoleResponse(user.getRole(), currentUser);
        }
        
        OrganizationResponse organizationResponse = null;
        if (user.getOrganization() != null) {
            organizationResponse = convertToOrganizationResponse(user.getOrganization());
        }
        
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getIsSuperadmin(),
                user.getIsGlobalSuperadmin(),
                roleResponse,
                organizationResponse,
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
    
    private OrganizationResponse convertToOrganizationResponse(Organization organization) {
        int userCount = organization.getUsers() != null ? organization.getUsers().size() : 0;
        int productCount = organization.getProducts() != null ? organization.getProducts().size() : 0;
        
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getDescription(),
                userCount,
                productCount,
                organization.getCreatedAt(),
                organization.getUpdatedAt()
        );
    }
}