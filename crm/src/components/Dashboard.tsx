import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { organizationService } from '../services/organizationService';
import { userService } from '../services/userService';
import { Organization, User } from '../types';
import OrganizationManager from './OrganizationManager';
import UserManager from './UserManager';

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'organizations' | 'users'>('organizations');
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [selectedOrgId, setSelectedOrgId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!authService.isAuthenticated()) {
      navigate('/login');
      return;
    }
    loadOrganizations();
  }, [navigate]);

  const loadOrganizations = async () => {
    try {
      setLoading(true);
      const orgs = await organizationService.getAll();
      setOrganizations(Array.isArray(orgs) ? orgs : []);
      if (orgs.length > 0 && !selectedOrgId) {
        setSelectedOrgId(orgs[0].id);
      }
    } catch (err: any) {
      setError('Failed to load organizations');
      console.error(err);
      setOrganizations([]); // Ensure organizations is always an array
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  const handleOrganizationChange = () => {
    loadOrganizations();
  };

  const handleOrganizationSelect = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedOrgId(Number(e.target.value));
  };

  const currentUser = authService.getCurrentUser();

  return (
    <div>
      <div className="header">
        <div className="header-content">
          <div>
            <h1>Mindful CRM</h1>
            <span style={{ fontSize: '14px', opacity: 0.8 }}>
              Logged in as: {currentUser?.email}
            </span>
          </div>
          <button onClick={handleLogout} className="btn btn-logout">
            Logout
          </button>
        </div>
      </div>

      <div className="container">
        <div className="tabs">
          <button 
            className={`tab ${activeTab === 'organizations' ? 'active' : ''}`}
            onClick={() => setActiveTab('organizations')}
          >
            Organizations
          </button>
          <button 
            className={`tab ${activeTab === 'users' ? 'active' : ''}`}
            onClick={() => setActiveTab('users')}
          >
            Users
          </button>
        </div>

        {error && (
          <div className="alert alert-error">{error}</div>
        )}

        {loading ? (
          <div className="loading">
            <div className="spinner"></div>
            <p>Loading...</p>
          </div>
        ) : (
          <>
            {activeTab === 'organizations' ? (
              <OrganizationManager 
                organizations={organizations}
                onOrganizationChange={handleOrganizationChange}
              />
            ) : (
              <>
                <div className="org-selector">
                  <label className="org-selector-label">
                    Select Organization:
                  </label>
                  <select 
                    className="org-select"
                    value={selectedOrgId || ''}
                    onChange={handleOrganizationSelect}
                  >
                    <option value="">-- Select Organization --</option>
                    {(organizations || []).map(org => (
                      <option key={org.id} value={org.id}>
                        {org.name}
                      </option>
                    ))}
                  </select>
                </div>

                {selectedOrgId ? (
                  <UserManager 
                    organizationId={selectedOrgId}
                    organizationName={organizations.find(o => o.id === selectedOrgId)?.name || ''}
                  />
                ) : (
                  <div className="empty-state">
                    <p>Please select an organization to manage users.</p>
                  </div>
                )}
              </>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default Dashboard;