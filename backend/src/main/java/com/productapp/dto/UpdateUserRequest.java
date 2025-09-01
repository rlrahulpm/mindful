package com.productapp.dto;

public class UpdateUserRequest {
    
    private Long roleId;
    
    public UpdateUserRequest() {}
    
    public UpdateUserRequest(Long roleId) {
        this.roleId = roleId;
    }
    
    public Long getRoleId() {
        return roleId;
    }
    
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}