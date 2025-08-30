import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useProduct } from '../hooks/useProduct';
import './CapacityPlanning.css';

interface Team {
  id: number;
  name: string;
  description: string;
  productId: number;
  isActive: boolean;
}

interface EpicEffort {
  id?: number;
  capacityPlanId?: number;
  epicId: string;
  epicName: string;
  teamId: number;
  teamName?: string;
  effortDays: number;
  notes?: string;
}

type EffortUnit = 'SPRINTS' | 'DAYS';

interface Epic {
  epicId: string;
  epicName: string;
  efforts: EpicEffort[];
}

interface CapacityPlan {
  id?: number;
  productId: number;
  year: number;
  quarter: number;
  effortUnit?: string;
  teams: Team[];
  epicEfforts: EpicEffort[];
}

const CapacityPlanning: React.FC = () => {
  const { productSlug } = useParams<{ productSlug: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { product, loading: productLoading, error: productError } = useProduct(productSlug);
  
  const [selectedYear, setSelectedYear] = useState<number>(new Date().getFullYear());
  const [selectedQuarter, setSelectedQuarter] = useState<number>(Math.ceil((new Date().getMonth() + 1) / 3));
  const [effortUnit, setEffortUnit] = useState<EffortUnit>('SPRINTS');
  const [capacityPlan, setCapacityPlan] = useState<CapacityPlan | null>(null);
  const [teams, setTeams] = useState<Team[]>([]);
  const [epics, setEpics] = useState<Epic[]>([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [isEditMode, setIsEditMode] = useState(false);
  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [showTeamModal, setShowTeamModal] = useState(false);
  const [newTeam, setNewTeam] = useState({ name: '', description: '' });

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  useEffect(() => {
    if (product) {
      loadCapacityPlan();
      loadTeams();
    }
  }, [product, selectedYear, selectedQuarter]);

  const loadTeams = async () => {
    if (!product) return;
    
    try {
      const response = await fetch(`http://localhost:8080/api/products/${product.productId}/capacity-planning/teams`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const teamsData = await response.json();
        setTeams(teamsData);
      } else {
        throw new Error('Failed to load teams');
      }
    } catch (err: any) {
      setError('Failed to load teams');
      console.error(err);
    }
  };

  const loadCapacityPlan = async () => {
    if (!product) return;
    
    try {
      setLoading(true);
      const response = await fetch(
        `http://localhost:8080/api/products/${product.productId}/capacity-planning/${selectedYear}/${selectedQuarter}`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );

      if (response.ok) {
        const data = await response.json();
        setCapacityPlan(data);
        
        // Set the effort unit from the response, default to SPRINTS if not present
        if (data.effortUnit) {
          setEffortUnit(data.effortUnit as EffortUnit);
        }
        
        // Group epic efforts by epic
        const epicGroups: { [key: string]: Epic } = {};
        
        data.epicEfforts?.forEach((effort: EpicEffort) => {
          if (!epicGroups[effort.epicId]) {
            epicGroups[effort.epicId] = {
              epicId: effort.epicId,
              epicName: effort.epicName,
              efforts: []
            };
          }
          epicGroups[effort.epicId].efforts.push(effort);
        });
        
        setEpics(Object.values(epicGroups));
      } else if (response.status === 404) {
        // No capacity plan exists for this quarter yet
        setCapacityPlan({
          productId: product.productId,
          year: selectedYear,
          quarter: selectedQuarter,
          teams: [],
          epicEfforts: []
        });
        setEpics([]);
      } else {
        throw new Error('Failed to load capacity plan');
      }
    } catch (err: any) {
      setError('Failed to load capacity plan');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const addTeam = async () => {
    if (!product || !newTeam.name.trim()) return;
    
    try {
      const response = await fetch(`http://localhost:8080/api/products/${product.productId}/capacity-planning/teams`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          name: newTeam.name.trim(),
          description: newTeam.description.trim(),
          isActive: true
        })
      });

      if (response.ok) {
        setNewTeam({ name: '', description: '' });
        setShowTeamModal(false);
        await loadTeams();
        await loadCapacityPlan(); // Reload to get updated epic efforts
      } else {
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to add team');
      }
    } catch (err: any) {
      setError(err.message || 'Failed to add team');
    }
  };

  const removeTeam = async (teamId: number) => {
    if (!product || !window.confirm('Are you sure you want to remove this team?')) return;
    
    try {
      const response = await fetch(`http://localhost:8080/api/products/${product.productId}/capacity-planning/teams/${teamId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        await loadTeams();
        await loadCapacityPlan(); // Reload to get updated epic efforts
      } else {
        throw new Error('Failed to remove team');
      }
    } catch (err: any) {
      setError('Failed to remove team');
      console.error(err);
    }
  };

  const updateEffort = (epicId: string, teamId: number, effortDays: number) => {
    setEpics(prevEpics => 
      prevEpics.map(epic => {
        if (epic.epicId === epicId) {
          return {
            ...epic,
            efforts: epic.efforts.map(effort => 
              effort.teamId === teamId 
                ? { ...effort, effortDays }
                : effort
            )
          };
        }
        return epic;
      })
    );
  };

  const updateNotes = (epicId: string, teamId: number, notes: string) => {
    setEpics(prevEpics => 
      prevEpics.map(epic => {
        if (epic.epicId === epicId) {
          return {
            ...epic,
            efforts: epic.efforts.map(effort => 
              effort.teamId === teamId 
                ? { ...effort, notes }
                : effort
            )
          };
        }
        return epic;
      })
    );
  };

  const saveCapacityPlan = async () => {
    if (!product || !capacityPlan) return;
    
    try {
      setSaving(true);
      
      // Flatten all epic efforts
      const allEfforts: EpicEffort[] = [];
      epics.forEach(epic => {
        epic.efforts.forEach(effort => {
          allEfforts.push({
            epicId: effort.epicId,
            epicName: effort.epicName,
            teamId: effort.teamId,
            effortDays: effort.effortDays,
            notes: effort.notes
          });
        });
      });

      const response = await fetch(
        `http://localhost:8080/api/products/${product.productId}/capacity-planning/${selectedYear}/${selectedQuarter}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify({
            year: selectedYear,
            quarter: selectedQuarter,
            effortUnit: effortUnit,
            epicEfforts: allEfforts
          })
        }
      );

      if (response.ok) {
        setIsEditMode(false);
        await loadCapacityPlan(); // Reload to get updated data
      } else {
        throw new Error('Failed to save capacity plan');
      }
    } catch (err: any) {
      setError('Failed to save capacity plan');
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  if (loading || productLoading) {
    return (
      <div className="capacity-planning-container">
        <div className="loading-state">
          <div className="spinner"></div>
          <p>Loading capacity planning...</p>
        </div>
      </div>
    );
  }

  if (error || productError) {
    return (
      <div className="capacity-planning-container">
        <div className="error-state">
          <h2>Error</h2>
          <p>{error || productError}</p>
          <button onClick={() => navigate(`/products/${productSlug}/modules`)} className="btn btn-primary">
            Back to Modules
          </button>
        </div>
      </div>
    );
  }

  const getTeamName = (teamId: number) => {
    const team = teams.find(t => t.id === teamId);
    return team?.name || 'Unknown Team';
  };

  const getTotalEffortForEpic = (epic: Epic) => {
    return epic.efforts.reduce((total, effort) => total + effort.effortDays, 0);
  };

  const getTotalEffortForTeam = (teamId: number) => {
    return epics.reduce((total, epic) => {
      const effort = epic.efforts.find(e => e.teamId === teamId);
      return total + (effort?.effortDays || 0);
    }, 0);
  };

  return (
    <div className="capacity-planning-container">
      <div className="page-header">
        <div className="header-top-row">
          <div className="header-left">
            <button 
              onClick={() => navigate(`/products/${productSlug}/modules`)} 
              className="back-button"
              aria-label="Back to modules"
            >
              <span className="material-icons">arrow_back</span>
            </button>
            <h1 className="page-title">Capacity Planning</h1>
          </div>
          
          <div className="quarter-selector">
            <select 
              value={selectedYear} 
              onChange={(e) => setSelectedYear(parseInt(e.target.value))}
              className="year-select"
            >
              {Array.from({length: 5}, (_, i) => new Date().getFullYear() - 2 + i).map(year => (
                <option key={year} value={year}>{year}</option>
              ))}
            </select>
            <select 
              value={selectedQuarter} 
              onChange={(e) => setSelectedQuarter(parseInt(e.target.value))}
              className="quarter-select"
            >
              <option value={1}>Q1 (Jan-Mar)</option>
              <option value={2}>Q2 (Apr-Jun)</option>
              <option value={3}>Q3 (Jul-Sep)</option>
              <option value={4}>Q4 (Oct-Dec)</option>
            </select>
          </div>

          {!isEditMode ? (
            <button
              onClick={() => setIsEditMode(true)}
              className="edit-mode-btn"
            >
              <span className="material-icons">edit</span>
              Edit
            </button>
          ) : (
            <div className="edit-actions">
              <button
                onClick={() => setIsEditMode(false)}
                className="cancel-edit-btn"
              >
                Cancel
              </button>
              <button
                onClick={saveCapacityPlan}
                className="save-btn"
                disabled={saving}
              >
                {saving ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          )}
        </div>
      </div>

      <div className="capacity-content">
        {error && (
          <div className="error-banner">
            <span className="material-icons">error</span>
            <span>{error}</span>
            <button onClick={() => setError('')} className="error-dismiss">
              <span className="material-icons">close</span>
            </button>
          </div>
        )}

        <div className="capacity-header">
          <div className="capacity-info">
            <span className="material-icons capacity-icon">groups</span>
            <div className="capacity-details">
              <h2>Q{selectedQuarter} {selectedYear} Capacity Plan</h2>
              <p>{epics.length} epics • {teams.length} teams</p>
            </div>
          </div>

          <button
            onClick={() => setShowSettingsModal(true)}
            className="settings-btn"
            title="Settings"
          >
            <span className="material-icons">settings</span>
          </button>
        </div>

        {epics.length === 0 ? (
          <div className="empty-state">
            <span className="material-icons">timeline</span>
            <h3>No epics found for Q{selectedQuarter} {selectedYear}</h3>
            <p>Add epics to your roadmap planner first, then come back to allocate capacity.</p>
          </div>
        ) : (
          <div className="capacity-table-container">
            <table className="capacity-table">
              <thead>
                <tr>
                  <th className="col-epic">Epic</th>
                  {teams.map(team => (
                    <th key={team.id} className="col-team">
                      <div className="team-header">
                        <span className="team-name">{team.name}</span>
                        {isEditMode && (
                          <button
                            onClick={() => removeTeam(team.id)}
                            className="remove-team-btn"
                            title="Remove team"
                          >
                            <span className="material-icons">close</span>
                          </button>
                        )}
                      </div>
                    </th>
                  ))}
                  <th className="col-total">Total</th>
                </tr>
              </thead>
              <tbody>
                {epics.map(epic => (
                  <tr key={epic.epicId} className="epic-row">
                    <td className="epic-cell">
                      <div className="epic-info">
                        <h4 className="epic-name">{epic.epicName}</h4>
                      </div>
                    </td>
                    {teams.map(team => {
                      const effort = epic.efforts.find(e => e.teamId === team.id);
                      return (
                        <td key={team.id} className="effort-cell">
                          {isEditMode ? (
                            <div className="effort-input-group">
                              <input
                                type="number"
                                min="0"
                                value={effort?.effortDays || 0}
                                onChange={(e) => updateEffort(epic.epicId, team.id, parseInt(e.target.value) || 0)}
                                className="effort-input"
                                placeholder="0"
                              />
                              <span className="effort-unit">{effortUnit.toLowerCase()}</span>
                            </div>
                          ) : (
                            <div className="effort-display">
                              <span className="effort-days">{effort?.effortDays || 0}</span>
                              <span className="effort-unit">{effortUnit.toLowerCase()}</span>
                            </div>
                          )}
                        </td>
                      );
                    })}
                    <td className="total-cell">
                      <span className="total-effort">{getTotalEffortForEpic(epic)} {effortUnit.toLowerCase()}</span>
                    </td>
                  </tr>
                ))}
                <tr className="totals-row">
                  <td className="totals-label">
                    <strong>Team Totals</strong>
                  </td>
                  {teams.map(team => (
                    <td key={team.id} className="team-total-cell">
                      <strong>{getTotalEffortForTeam(team.id)} {effortUnit.toLowerCase()}</strong>
                    </td>
                  ))}
                  <td className="grand-total-cell">
                    <strong>
                      {epics.reduce((total, epic) => total + getTotalEffortForEpic(epic), 0)} {effortUnit.toLowerCase()}
                    </strong>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Settings Modal */}
      {showSettingsModal && (
        <div className="modal-overlay" onClick={() => setShowSettingsModal(false)}>
          <div className="settings-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Capacity Planning Settings</h3>
              <button 
                onClick={() => setShowSettingsModal(false)}
                className="modal-close-btn"
              >
                <span className="material-icons">close</span>
              </button>
            </div>
            <div className="modal-body">
              {/* Effort Unit Section */}
              <div className="settings-section">
                <h4>Effort Unit</h4>
                <div className="unit-toggle">
                  <button
                    className={`unit-option ${effortUnit === 'SPRINTS' ? 'active' : ''}`}
                    onClick={() => setEffortUnit('SPRINTS')}
                  >
                    Sprints
                  </button>
                  <button
                    className={`unit-option ${effortUnit === 'DAYS' ? 'active' : ''}`}
                    onClick={() => setEffortUnit('DAYS')}
                  >
                    Days
                  </button>
                </div>
              </div>

              {/* Team Management Section */}
              <div className="settings-section">
                <div className="section-header">
                  <h4>Teams</h4>
                  <button 
                    onClick={() => setShowTeamModal(true)}
                    className="add-team-inline-btn"
                  >
                    <span className="material-icons">add</span>
                    Add Team
                  </button>
                </div>
                <div className="teams-list">
                  {teams.length === 0 ? (
                    <p className="no-teams">No teams configured</p>
                  ) : (
                    teams.map(team => (
                      <div key={team.id} className="team-item">
                        <div className="team-info">
                          <span className="team-name">{team.name}</span>
                          {team.description && (
                            <span className="team-description">{team.description}</span>
                          )}
                        </div>
                        <button
                          onClick={() => removeTeam(team.id)}
                          className="remove-team-inline-btn"
                          title="Remove team"
                        >
                          <span className="material-icons">delete</span>
                        </button>
                      </div>
                    ))
                  )}
                </div>
              </div>
            </div>
            <div className="modal-actions">
              <button 
                onClick={() => setShowSettingsModal(false)}
                className="btn-confirm"
              >
                Done
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Add Team Modal */}
      {showTeamModal && (
        <div className="modal-overlay" onClick={() => setShowTeamModal(false)}>
          <div className="team-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Add New Team</h3>
              <button 
                onClick={() => setShowTeamModal(false)}
                className="modal-close-btn"
              >
                <span className="material-icons">close</span>
              </button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label htmlFor="teamName">Team Name</label>
                <input
                  id="teamName"
                  type="text"
                  value={newTeam.name}
                  onChange={(e) => setNewTeam(prev => ({...prev, name: e.target.value}))}
                  placeholder="Enter team name"
                  className="form-input"
                />
              </div>
              <div className="form-group">
                <label htmlFor="teamDescription">Description (optional)</label>
                <textarea
                  id="teamDescription"
                  value={newTeam.description}
                  onChange={(e) => setNewTeam(prev => ({...prev, description: e.target.value}))}
                  placeholder="Enter team description"
                  className="form-textarea"
                  rows={3}
                />
              </div>
            </div>
            <div className="modal-actions">
              <button 
                onClick={() => {
                  setShowTeamModal(false);
                  setNewTeam({ name: '', description: '' });
                }}
                className="btn-cancel"
              >
                Cancel
              </button>
              <button 
                onClick={async () => {
                  await addTeam();
                  setShowTeamModal(false);
                }}
                className="btn-confirm"
                disabled={!newTeam.name.trim()}
              >
                Add Team
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CapacityPlanning;