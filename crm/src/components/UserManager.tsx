import React, { useState, useEffect } from 'react';
import { userService } from '../services/userService';
import { User } from '../types';

interface Props {
  organizationId: number;
  organizationName: string;
}

const UserManager: React.FC<Props> = ({ organizationId, organizationName }) => {
  const [users, setUsers] = useState<User[]>([]);
  const [loadingUsers, setLoadingUsers] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [addLoading, setAddLoading] = useState(false);
  const [formData, setFormData] = useState({ email: '', password: '' });

  useEffect(() => {
    loadUsers();
  }, [organizationId]);

  const loadUsers = async () => {
    try {
      setLoadingUsers(true);
      const allUsers = await userService.getUsersByOrganization(organizationId);
      setUsers(allUsers);
    } catch (err: any) {
      console.error('Failed to load users:', err);
    } finally {
      setLoadingUsers(false);
    }
  };


  const handleDelete = async (id: number, isGlobalSuperAdmin: boolean) => {
    if (isGlobalSuperAdmin) {
      alert('Cannot delete global superadmin users');
      return;
    }

    if (!window.confirm('Are you sure you want to delete this user?')) {
      return;
    }

    try {
      await userService.deleteUser(id);
      loadUsers();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to delete user');
    }
  };

  const handleAddUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.email || !formData.password) {
      alert('Please fill in all fields');
      return;
    }

    try {
      setAddLoading(true);
      await userService.createUser({
        email: formData.email,
        password: formData.password,
        organizationId: organizationId
      });
      setFormData({ email: '', password: '' });
      setShowAddModal(false);
      loadUsers();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to create user');
    } finally {
      setAddLoading(false);
    }
  };

  return (
    <div className="card">
      <div className="card-header">
        <h2 className="card-title">Users - {organizationName}</h2>
        <button 
          onClick={() => setShowAddModal(true)}
          className="btn btn-primary"
        >
          Add User
        </button>
      </div>

      {loadingUsers ? (
        <div className="loading">
          <div className="spinner"></div>
          <p>Loading users...</p>
        </div>
      ) : users.length === 0 ? (
        <div className="empty-state">
          <p>No users found for this organization.</p>
        </div>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Email</th>
              <th>Global Admin</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map(user => (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>{user.email}</td>
                <td>
                  {user.isGlobalSuperAdmin && (
                    <span className="badge badge-warning">Global Admin</span>
                  )}
                </td>
                <td>
                  <div className="actions">
                    <button 
                      onClick={() => handleDelete(user.id, user.isGlobalSuperAdmin)}
                      className="btn btn-danger btn-sm"
                      disabled={user.isGlobalSuperAdmin}
                      title={user.isGlobalSuperAdmin ? 'Cannot delete global superadmin' : ''}
                    >
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {showAddModal && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h3 className="modal-title">Add New User</h3>
              <button 
                onClick={() => setShowAddModal(false)}
                className="close-btn"
              >
                ×
              </button>
            </div>
            <form onSubmit={handleAddUser}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">Email</label>
                  <input
                    type="email"
                    className="form-control"
                    value={formData.email}
                    onChange={(e) => setFormData({...formData, email: e.target.value})}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Password</label>
                  <input
                    type="password"
                    className="form-control"
                    value={formData.password}
                    onChange={(e) => setFormData({...formData, password: e.target.value})}
                    required
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button 
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="btn btn-secondary"
                  disabled={addLoading}
                >
                  Cancel
                </button>
                <button 
                  type="submit"
                  className="btn btn-primary"
                  disabled={addLoading}
                >
                  {addLoading ? 'Creating...' : 'Create User'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManager;