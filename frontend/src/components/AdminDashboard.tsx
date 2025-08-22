import React, { useState, useEffect, useCallback } from 'react';
import { adminService } from '../services/adminService';
import { Role, User, ProductModuleResponse, CreateRoleRequest, CreateUserRequest } from '../types/admin';
import './AdminDashboard.css';

const AdminDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'roles' | 'users'>('roles');
  const [roles, setRoles] = useState<Role[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [modules, setModules] = useState<ProductModuleResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
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
    console.log(`getModulesForProduct(${productId}): Found ${filteredModules.length} modules`);
    console.log('All modules available:', modules.length);
    if (modules.length > 0) {
      console.log('Sample module:', modules[0]);
    }
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
    console.log('AdminDashboard component mounted');
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
      console.log('AdminDashboard: Starting to load data...');
      setLoading(true);
      const [rolesData, usersData, modulesData] = await Promise.all([
        adminService.getRoles(),
        adminService.getUsers(),
        adminService.getProductModules()
      ]);
      console.log('AdminDashboard: Data loaded successfully');
      console.log('Roles:', rolesData);
      console.log('Users:', usersData);
      console.log('Product-Modules:', modulesData);
      
      setRoles(rolesData);
      setUsers(usersData);
      setModules(modulesData);
      
    } catch (err: any) {
      setError('Failed to load data');
      console.error('Admin data loading error:', err);
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
      } else {
        const newRole = await adminService.createRole(roleForm);
        setRoles([...roles, newRole]);
      }
      setShowRoleModal(false);
      setEditingRole(null);
      setRoleForm({ name: '', description: '', productModuleIds: [] });
    } catch (err: any) {
      setError('Failed to save role');
      console.error(err);
    }
  };

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const newUser = await adminService.createUser(userForm);
      setUsers([...users, newUser]);
      setShowUserModal(false);
      setUserForm({ email: '', password: '', roleId: undefined });
    } catch (err: any) {
      setError('Failed to create user');
      console.error(err);
    }
  };

  const handleDeleteRole = async (roleId: number) => {
    if (window.confirm('Are you sure you want to delete this role?')) {
      try {
        await adminService.deleteRole(roleId);
        setRoles(roles.filter(r => r.id !== roleId));
      } catch (err: any) {
        setError('Failed to delete role');
        console.error(err);
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
    <div className="admin-container">
      <div className="admin-header">
        <h1 className="admin-title">Admin Dashboard</h1>
        <p className="admin-subtitle">Manage roles and users</p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="admin-tabs">
        <button 
          className={`tab-btn ${activeTab === 'roles' ? 'active' : ''}`}
          onClick={() => setActiveTab('roles')}
        >
          Roles ({roles.length})
        </button>
        <button 
          className={`tab-btn ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          Users ({users.length})
        </button>
      </div>

      {activeTab === 'roles' && (
        <div className="tab-content">
          <div className="section-header">
            <h2>Role Management</h2>
            <button 
              onClick={() => {
                setShowRoleModal(true);
                // Initialize product tab for new role creation
                const products = getUniqueProducts();
                if (products.length > 0) {
                  setActiveProductTab(products[0].productId);
                }
              }}
              className="btn btn-primary"
            >
              Create Role
            </button>
          </div>

          <div className="roles-grid">
            {roles.map((role) => (
              <div key={role.id} className="role-card">
                <div className="role-header">
                  <h3 className="role-name">{role.name}</h3>
                  <div className="role-actions">
                    <button 
                      onClick={() => openEditRole(role)}
                      className="btn btn-secondary btn-sm"
                    >
                      Edit
                    </button>
                    <button 
                      onClick={() => handleDeleteRole(role.id)}
                      className="btn btn-danger btn-sm"
                    >
                      Delete
                    </button>
                  </div>
                </div>
                <p className="role-description">{role.description}</p>
                <div className="role-modules">
                  <strong>Accessible Modules:</strong>
                  {groupModulesByProduct(role.productModules).map((group) => (
                    <div key={group.productName} className="product-module-group">
                      <h4 className="product-group-name">{group.productName}</h4>
                      <div className="module-tags">
                        {group.modules.map((productModule) => (
                          <span key={productModule.id} className="module-tag">
                            {productModule.module.icon} {productModule.module.name}
                          </span>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {activeTab === 'users' && (
        <div className="tab-content">
          <div className="section-header">
            <h2>User Management</h2>
            <button 
              onClick={() => setShowUserModal(true)}
              className="btn btn-primary"
            >
              Create User
            </button>
          </div>

          <div className="users-table">
            <table>
              <thead>
                <tr>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Superadmin</th>
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td>{user.email}</td>
                    <td>{user.role?.name || 'No Role'}</td>
                    <td>
                      <span className={`badge ${user.isSuperadmin ? 'badge-success' : 'badge-secondary'}`}>
                        {user.isSuperadmin ? 'Yes' : 'No'}
                      </span>
                    </td>
                    <td>{new Date(user.createdAt).toLocaleDateString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
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
                ×
              </button>
            </div>
            <form onSubmit={handleCreateRole} className="modal-form">
              <div className="form-group">
                <label htmlFor="roleName" className="form-label">Role Name</label>
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
                <label htmlFor="roleDescription" className="form-label">Description</label>
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
                <label className="form-label">Grant Access to Modules</label>
                <p className="form-help-text">Select which modules this role can access by choosing products and their modules</p>
                
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
                                  {productModule.module.icon} {productModule.module.name}
                                </span>
                                <span className="module-description">{productModule.module.description}</span>
                              </span>
                            </label>
                          ))
                        ) : (
                          <div className="no-modules-message">
                            <p>No modules are available for this product yet.</p>
                            <p>Modules can be added to products by administrators.</p>
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
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
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
              <h3 className="modal-title">Create New User</h3>
              <button 
                onClick={() => {
                  setShowUserModal(false);
                  setUserForm({ email: '', password: '', roleId: undefined });
                }}
                className="modal-close-btn"
              >
                ×
              </button>
            </div>
            <form onSubmit={handleCreateUser} className="modal-form">
              <div className="form-group">
                <label htmlFor="userEmail" className="form-label">Email</label>
                <input
                  type="email"
                  id="userEmail"
                  value={userForm.email}
                  onChange={(e) => setUserForm({ ...userForm, email: e.target.value })}
                  placeholder="Enter email"
                  required
                  className="form-control"
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="userPassword" className="form-label">Password</label>
                <input
                  type="password"
                  id="userPassword"
                  value={userForm.password}
                  onChange={(e) => setUserForm({ ...userForm, password: e.target.value })}
                  placeholder="Enter password"
                  required
                  minLength={6}
                  className="form-control"
                />
              </div>

              <div className="form-group">
                <label htmlFor="userRole" className="form-label">Role</label>
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
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
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