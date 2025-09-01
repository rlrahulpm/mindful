import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminService } from '../services/adminService';
import { Role, User, ProductModuleResponse, CreateRoleRequest, CreateUserRequest } from '../types/admin';
import './AdminDashboard.css';

const AdminDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'roles' | 'users'>('roles');
  const [roles, setRoles] = useState<Role[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [modules, setModules] = useState<ProductModuleResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [activeProductTab, setActiveProductTab] = useState<number | null>(null);

  // Modal states
  const [showRoleModal, setShowRoleModal] = useState(false);
  const [showUserModal, setShowUserModal] = useState(false);
  const [editingRole, setEditingRole] = useState<Role | null>(null);

  // Form states
  const [roleForm, setRoleForm] = useState<CreateRoleRequest>({
    name: '',
    description: '',
    productModuleIds: []
  });
  const [userForm, setUserForm] = useState<CreateUserRequest>({
    email: '',
    password: '',
    roleId: undefined
  });


  // Helper functions for the tabbed interface
  const getUniqueProducts = useCallback(() => {
    // Extract unique products from the product-modules data
    const uniqueProductsMap = new Map();
    modules.forEach(productModule => {
      if (productModule.product) {
        uniqueProductsMap.set(productModule.product.productId, {
          productId: productModule.product.productId,
          productName: productModule.product.productName,
          createdAt: productModule.product.createdAt
        });
      }
    });
    return Array.from(uniqueProductsMap.values());
  }, [modules]);

  const getModulesForProduct = useCallback((productId: number) => {
    const filteredModules = modules.filter(module => module.product?.productId === productId);
    return filteredModules;
  }, [modules]);

  const groupModulesByProduct = useCallback((productModules: ProductModuleResponse[]) => {
    const grouped = new Map<string, ProductModuleResponse[]>();
    
    productModules.forEach(pm => {
      const productName = pm.product?.productName || 'Unknown Product';
      if (!grouped.has(productName)) {
        grouped.set(productName, []);
      }
      grouped.get(productName)!.push(pm);
    });
    
    // Sort products alphabetically and modules by display order
    const sortedGroups = Array.from(grouped.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([productName, modules]) => ({
        productName,
        modules: modules.sort((a, b) => 
          (a.module.displayOrder || 0) - (b.module.displayOrder || 0)
        )
      }));
    
    return sortedGroups;
  }, []);

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    // Set first product as active when modules load
    if (modules.length > 0 && activeProductTab === null) {
      const products = getUniqueProducts();
      if (products.length > 0) {
        setActiveProductTab(products[0].productId);
      }
    }
  }, [modules, activeProductTab, getUniqueProducts]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [rolesData, usersData, modulesData] = await Promise.all([
        adminService.getRoles(),
        adminService.getUsers(),
        adminService.getProductModules()
      ]);
      
      setRoles(rolesData);
      setUsers(usersData);
      setModules(modulesData);
      
    } catch (err: any) {
      setError('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateRole = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingRole) {
        const updatedRole = await adminService.updateRole(editingRole.id, roleForm);
        setRoles(roles.map(r => r.id === updatedRole.id ? updatedRole : r));
        setSuccessMessage('Role updated successfully!');
      } else {
        const newRole = await adminService.createRole(roleForm);
        setRoles([...roles, newRole]);
        setSuccessMessage('Role created successfully!');
      }
      setShowRoleModal(false);
      setEditingRole(null);
      setRoleForm({ name: '', description: '', productModuleIds: [] });
      
      // Clear success message after 3 seconds
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err: any) {
      setError('Failed to save role');
    }
  };

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const newUser = await adminService.createUser(userForm);
      setUsers([...users, newUser]);
      setShowUserModal(false);
      setUserForm({ email: '', password: '', roleId: undefined });
      setSuccessMessage('User created successfully!');
      
      // Clear success message after 3 seconds
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err: any) {
      setError('Failed to create user');
    }
  };

  const handleDeleteRole = async (roleId: number) => {
    if (window.confirm('Are you sure you want to delete this role?')) {
      try {
        await adminService.deleteRole(roleId);
        setRoles(roles.filter(r => r.id !== roleId));
        setSuccessMessage('Role deleted successfully!');
        setTimeout(() => setSuccessMessage(''), 3000);
      } catch (err: any) {
        setError('Failed to delete role');
      }
    }
  };

  const openEditRole = (role: Role) => {
    setEditingRole(role);
    setRoleForm({
      name: role.name,
      description: role.description,
      productModuleIds: role.productModules.map(pm => pm.id)
    });
    setShowRoleModal(true);
    // Reset product tab to first available product
    const products = getUniqueProducts();
    if (products.length > 0) {
      setActiveProductTab(products[0].productId);
    }
  };

  const handleProductModuleToggle = (productModuleId: number) => {
    const currentIds = roleForm.productModuleIds || [];
    if (currentIds.includes(productModuleId)) {
      setRoleForm({
        ...roleForm,
        productModuleIds: currentIds.filter(id => id !== productModuleId)
      });
    } else {
      setRoleForm({
        ...roleForm,
        productModuleIds: [...currentIds, productModuleId]
      });
    }
  };

  if (loading) {
    return (
      <div className="admin-container">
        <div className="loading-state">
          <div className="spinner"></div>
          <p>Loading admin data...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="modules-container">
      <button 
        onClick={() => navigate('/dashboard')} 
        className="admin-back-button"
        aria-label="Back to dashboard"
      >
        <span className="material-icons">arrow_back</span>
      </button>
      <div className="modules-header">
        <div className="product-info">
          <h1 className="product-title">Admin Dashboard</h1>
          <p className="product-subtitle">Manage roles and users across your organization</p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {successMessage && <div className="alert alert-success">{successMessage}</div>}

      <div className="admin-tabs">
        <button 
          className={`tab-btn ${activeTab === 'roles' ? 'active' : ''}`}
          onClick={() => setActiveTab('roles')}
        >
          <span className="material-icons">badge</span>
          Roles
          <span className="tab-count">{roles.length}</span>
        </button>
        <button 
          className={`tab-btn ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          <span className="material-icons">people</span>
          Users
          <span className="tab-count">{users.length}</span>
        </button>
      </div>

      {activeTab === 'roles' && (
        <div className="tab-content">
          <div className="section-header">
            <h2 className="section-title">Role Management</h2>
            <button 
              onClick={() => {
                setShowRoleModal(true);
                // Initialize product tab for new role creation
                const products = getUniqueProducts();
                if (products.length > 0) {
                  setActiveProductTab(products[0].productId);
                }
              }}
              className="btn-primary-action"
            >
              <span className="material-icons">add</span>
              Create Role
            </button>
          </div>

          <div className="roles-grid">
            {roles.map((role) => (
              <div key={role.id} className="role-card">
                <div className="card-header">
                  <div className="card-icon">
                    <span className="material-icons">badge</span>
                  </div>
                  <div className="card-info">
                    <h3 className="card-title">{role.name}</h3>
                    <p className="card-description">{role.description}</p>
                  </div>
                </div>
                
                <div className="card-content">
                  <div className="modules-section">
                    <h4 className="modules-title">
                      <span className="material-icons">dashboard</span>
                      Accessible Modules
                    </h4>
                    {groupModulesByProduct(role.productModules).map((group) => (
                      <div key={group.productName} className="product-module-group">
                        <h5 className="product-group-name">{group.productName}</h5>
                        <div className="module-tags">
                          {group.modules.map((productModule) => (
                            <span key={productModule.id} className="module-tag">
                              <span className="material-icons">extension</span>
                              {productModule.module.name}
                            </span>
                          ))}
                        </div>
                      </div>
                    ))}
                    {role.productModules.length === 0 && (
                      <p className="no-modules">No modules assigned</p>
                    )}
                  </div>
                </div>
                
                <div className="card-actions">
                  <button 
                    onClick={() => openEditRole(role)}
                    className="action-btn edit-btn"
                  >
                    <span className="material-icons">edit</span>
                    Edit
                  </button>
                  <button 
                    onClick={() => handleDeleteRole(role.id)}
                    className="action-btn delete-btn"
                  >
                    <span className="material-icons">delete</span>
                    Delete
                  </button>
                </div>
              </div>
            ))}
            
            {roles.length === 0 && (
              <div className="empty-state">
                <span className="material-icons">badge</span>
                <h3>No roles created yet</h3>
                <p>Create your first role to manage user permissions</p>
              </div>
            )}
          </div>
        </div>
      )}

      {activeTab === 'users' && (
        <div className="tab-content">
          <div className="section-header">
            <h2 className="section-title">User Management</h2>
            <button 
              onClick={() => setShowUserModal(true)}
              className="btn-primary-action"
            >
              <span className="material-icons">person_add</span>
              Create User
            </button>
          </div>

          <div className="users-grid">
            {users.map((user) => (
              <div key={user.id} className="user-card">
                <div className="user-avatar">
                  <span className="material-icons">account_circle</span>
                </div>
                <div className="user-info">
                  <h3 className="user-email">{user.email}</h3>
                  <div className="user-meta">
                    <span className="user-role">
                      <span className="material-icons">badge</span>
                      {user.role?.name || 'No Role'}
                    </span>
                    {user.isSuperadmin && (
                      <span className="badge badge-admin">
                        <span className="material-icons">admin_panel_settings</span>
                        Admin
                      </span>
                    )}
                  </div>
                  <p className="user-created">
                    <span className="material-icons">calendar_today</span>
                    Joined {new Date(user.createdAt).toLocaleDateString()}
                  </p>
                </div>
              </div>
            ))}
            
            {users.length === 0 && (
              <div className="empty-state">
                <span className="material-icons">people</span>
                <h3>No users created yet</h3>
                <p>Create your first user to grant system access</p>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Role Modal */}
      {showRoleModal && (
        <div className="modal-overlay" onClick={() => {
          setShowRoleModal(false);
          setEditingRole(null);
          setRoleForm({ name: '', description: '', productModuleIds: [] });
        }}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">
                <span className="material-icons">
                  {editingRole ? 'edit' : 'add_circle'}
                </span>
                {editingRole ? 'Edit Role' : 'Create New Role'}
              </h3>
              <button 
                onClick={() => {
                  setShowRoleModal(false);
                  setEditingRole(null);
                  setRoleForm({ name: '', description: '', productModuleIds: [] });
                }}
                className="modal-close-btn"
              >
                <span className="material-icons">close</span>
              </button>
            </div>
            <form onSubmit={handleCreateRole} className="modal-form">
              <div className="form-group">
                <label htmlFor="roleName" className="form-label">
                  <span className="material-icons">label</span>
                  Role Name
                </label>
                <input
                  type="text"
                  id="roleName"
                  value={roleForm.name}
                  onChange={(e) => setRoleForm({ ...roleForm, name: e.target.value })}
                  placeholder="Enter role name"
                  required
                  className="form-control"
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="roleDescription" className="form-label">
                  <span className="material-icons">description</span>
                  Description
                </label>
                <textarea
                  id="roleDescription"
                  value={roleForm.description}
                  onChange={(e) => setRoleForm({ ...roleForm, description: e.target.value })}
                  placeholder="Enter role description"
                  className="form-control"
                  rows={3}
                />
              </div>

              <div className="form-group">
                <label className="form-label">
                  <span className="material-icons">dashboard</span>
                  Grant Access to Modules
                </label>
                <p className="form-help-text">Select which modules this role can access</p>
                
                {getUniqueProducts().length > 0 && (
                  <div className="product-module-tabs">
                    {/* Product Tabs */}
                    <div className="product-tabs">
                      {getUniqueProducts().map((product) => (
                        <button
                          key={product.productId}
                          type="button"
                          className={`product-tab ${activeProductTab === product.productId ? 'active' : ''}`}
                          onClick={() => setActiveProductTab(product.productId)}
                        >
                          {product.productName}
                        </button>
                      ))}
                    </div>
                    
                    {/* Modules for Active Product */}
                    {activeProductTab && (
                      <div className="modules-selection">
                        {getModulesForProduct(activeProductTab).length > 0 ? (
                          getModulesForProduct(activeProductTab).map((productModule) => (
                            <label key={productModule.id} className="checkbox-label">
                              <input
                                type="checkbox"
                                checked={(roleForm.productModuleIds || []).includes(productModule.id)}
                                onChange={() => handleProductModuleToggle(productModule.id)}
                              />
                              <span className="checkbox-text">
                                <span className="module-info">
                                  <span className="material-icons">extension</span>
                                  {productModule.module.name}
                                </span>
                                <span className="module-description">{productModule.module.description}</span>
                              </span>
                            </label>
                          ))
                        ) : (
                          <div className="no-modules-message">
                            <span className="material-icons">info</span>
                            <p>No modules available for this product</p>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                )}
              </div>

              <div className="modal-actions">
                <button 
                  type="button"
                  onClick={() => {
                    setShowRoleModal(false);
                    setEditingRole(null);
                    setRoleForm({ name: '', description: '', productModuleIds: [] });
                  }}
                  className="btn-secondary"
                >
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  <span className="material-icons">
                    {editingRole ? 'save' : 'add'}
                  </span>
                  {editingRole ? 'Update Role' : 'Create Role'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* User Modal */}
      {showUserModal && (
        <div className="modal-overlay" onClick={() => {
          setShowUserModal(false);
          setUserForm({ email: '', password: '', roleId: undefined });
        }}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">
                <span className="material-icons">person_add</span>
                Create New User
              </h3>
              <button 
                onClick={() => {
                  setShowUserModal(false);
                  setUserForm({ email: '', password: '', roleId: undefined });
                }}
                className="modal-close-btn"
              >
                <span className="material-icons">close</span>
              </button>
            </div>
            <form onSubmit={handleCreateUser} className="modal-form">
              <div className="form-group">
                <label htmlFor="userEmail" className="form-label">
                  <span className="material-icons">email</span>
                  Email
                </label>
                <input
                  type="email"
                  id="userEmail"
                  value={userForm.email}
                  onChange={(e) => setUserForm({ ...userForm, email: e.target.value })}
                  placeholder="Enter email address"
                  required
                  className="form-control"
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="userPassword" className="form-label">
                  <span className="material-icons">lock</span>
                  Password
                </label>
                <input
                  type="password"
                  id="userPassword"
                  value={userForm.password}
                  onChange={(e) => setUserForm({ ...userForm, password: e.target.value })}
                  placeholder="Enter password (min 6 characters)"
                  required
                  minLength={6}
                  className="form-control"
                />
              </div>

              <div className="form-group">
                <label htmlFor="userRole" className="form-label">
                  <span className="material-icons">badge</span>
                  Role
                </label>
                <select
                  id="userRole"
                  value={userForm.roleId || ''}
                  onChange={(e) => setUserForm({ 
                    ...userForm, 
                    roleId: e.target.value ? Number(e.target.value) : undefined 
                  })}
                  className="form-control"
                >
                  <option value="">Select a role (optional)</option>
                  {roles.map((role) => (
                    <option key={role.id} value={role.id}>
                      {role.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="modal-actions">
                <button 
                  type="button"
                  onClick={() => {
                    setShowUserModal(false);
                    setUserForm({ email: '', password: '', roleId: undefined });
                  }}
                  className="btn-secondary"
                >
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  <span className="material-icons">person_add</span>
                  Create User
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;