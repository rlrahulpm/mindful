import React, { useState } from 'react';
import { organizationService } from '../services/organizationService';
import { Organization, CreateOrganizationRequest, UpdateOrganizationRequest } from '../types';

interface Props {
  organizations: Organization[];
  onOrganizationChange: () => void;
}

const OrganizationManager: React.FC<Props> = ({ organizations = [], onOrganizationChange }) => {
  const [showModal, setShowModal] = useState(false);
  const [editingOrg, setEditingOrg] = useState<Organization | null>(null);
  const [formData, setFormData] = useState<CreateOrganizationRequest>({
    name: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const openModal = (org?: Organization) => {
    if (org) {
      setEditingOrg(org);
      setFormData({ name: org.name });
    } else {
      setEditingOrg(null);
      setFormData({ name: '' });
    }
    setShowModal(true);
    setError('');
  };

  const closeModal = () => {
    setShowModal(false);
    setEditingOrg(null);
    setFormData({ name: '' });
    setError('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (editingOrg) {
        await organizationService.update(editingOrg.id, formData);
      } else {
        await organizationService.create(formData);
      }
      onOrganizationChange();
      closeModal();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Operation failed');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this organization? This will also delete all associated users.')) {
      return;
    }

    try {
      await organizationService.delete(id);
      onOrganizationChange();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to delete organization');
    }
  };

  return (
    <div className="card">
      <div className="card-header">
        <h2 className="card-title">Organizations</h2>
        <button onClick={() => openModal()} className="btn btn-primary">
          Add Organization
        </button>
      </div>

      {!organizations || organizations.length === 0 ? (
        <div className="empty-state">
          <p>No organizations found.</p>
          <button onClick={() => openModal()} className="btn btn-primary">
            Create First Organization
          </button>
        </div>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Created At</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {(organizations || []).map(org => (
              <tr key={org.id}>
                <td>{org.id}</td>
                <td>{org.name}</td>
                <td>{new Date(org.createdAt).toLocaleDateString()}</td>
                <td>
                  <div className="actions">
                    <button 
                      onClick={() => openModal(org)}
                      className="btn btn-secondary btn-sm"
                    >
                      Edit
                    </button>
                    <button 
                      onClick={() => handleDelete(org.id)}
                      className="btn btn-danger btn-sm"
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

      {showModal && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">
                {editingOrg ? 'Edit Organization' : 'Add Organization'}
              </h3>
              <button onClick={closeModal} className="close-btn">×</button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                {error && (
                  <div className="alert alert-error">{error}</div>
                )}

                <div className="form-group">
                  <label className="form-label">Organization Name</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.name}
                    onChange={(e) => setFormData({ name: e.target.value })}
                    required
                    placeholder="Enter organization name"
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button 
                  type="button"
                  onClick={closeModal}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button 
                  type="submit"
                  className="btn btn-primary"
                  disabled={loading}
                >
                  {loading ? 'Saving...' : (editingOrg ? 'Update' : 'Create')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default OrganizationManager;