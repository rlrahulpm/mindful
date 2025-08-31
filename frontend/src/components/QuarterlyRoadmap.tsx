import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
// import { useAuth } from '../context/AuthContext';
import { useProduct } from '../hooks/useProduct';
import './QuarterlyRoadmap.css';

interface RoadmapItem {
  epicId: string;
  epicName: string;
  epicDescription: string;
  priority: string;
  status: string;
  estimatedEffort: string;
  assignedTeam: string;
  reach: number;
  impact: number;
  confidence: number;
  riceScore: number;
  effortRating?: number; // Auto-filled from capacity planning
  startDate?: string;
  endDate?: string;
}

interface QuarterlyRoadmapData {
  id?: number;
  productId: number;
  year: number;
  quarter: number;
  roadmapItems: RoadmapItem[];
}

interface Epic {
  id: string;
  name: string;
  description: string;
  themeId: string;
  themeName: string;
  themeColor: string;
  initiativeId: string;
  initiativeName: string;
  track: string;
}

// Helper functions for quarter date calculations
const getQuarterStartDate = (year: number, quarter: number): string => {
  const month = (quarter - 1) * 3; // Q1=0, Q2=3, Q3=6, Q4=9
  const date = new Date(year, month, 1);
  return date.getFullYear() + '-' + 
         String(date.getMonth() + 1).padStart(2, '0') + '-' + 
         String(date.getDate()).padStart(2, '0');
};

const getQuarterEndDate = (year: number, quarter: number): string => {
  const month = quarter * 3; // Q1=3, Q2=6, Q3=9, Q4=12
  const date = new Date(year, month, 0); // Day 0 = last day of previous month
  return date.getFullYear() + '-' + 
         String(date.getMonth() + 1).padStart(2, '0') + '-' + 
         String(date.getDate()).padStart(2, '0');
};

const QuarterlyRoadmap: React.FC = () => {
  const { productSlug } = useParams<{ productSlug: string }>();
  const navigate = useNavigate();
  // const { user } = useAuth();
  const { product, loading: productLoading, error: productError } = useProduct(productSlug);
  
  const [roadmapData, setRoadmapData] = useState<QuarterlyRoadmapData | null>(null);
  const [availableEpics, setAvailableEpics] = useState<Epic[]>([]);
  const [selectedYear, setSelectedYear] = useState<number>(new Date().getFullYear());
  const [selectedQuarter, setSelectedQuarter] = useState<number>(Math.ceil((new Date().getMonth() + 1) / 3));
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [inlineError, setInlineError] = useState('');
  const [isEditMode, setIsEditMode] = useState(false);
  const [showEpicModal, setShowEpicModal] = useState(false);
  const [selectedEpics, setSelectedEpics] = useState<Set<string>>(new Set());
  const [epicSearchTerm, setEpicSearchTerm] = useState('');
  const [selectedThemeFilter, setSelectedThemeFilter] = useState('');
  const [selectedInitiativeFilter, setSelectedInitiativeFilter] = useState('');
  const [selectedTrackFilter, setSelectedTrackFilter] = useState('');
  const [assignedEpicIds, setAssignedEpicIds] = useState<Set<string>>(new Set());

  const loadRoadmapData = async () => {
    if (!product) return;
    
    try {
      console.log('Loading roadmap data for:', { productId: product.productId, selectedYear, selectedQuarter });
      setLoading(true);
      const response = await fetch(
        `http://localhost:8080/api/products/${product.productId}/roadmap/${selectedYear}/${selectedQuarter}`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );

      console.log('Roadmap response status:', response.status);

      if (response.ok) {
        const data = await response.json();
        console.log('Roadmap data received:', data);
        setRoadmapData(data);
        const epicIds = data.roadmapItems?.map((item: RoadmapItem) => item.epicId) || [];
        setSelectedEpics(new Set(epicIds));
      } else if (response.status === 404) {
        console.log('No roadmap exists for this quarter, creating empty one');
        // No roadmap exists for this quarter yet
        setRoadmapData({
          productId: product.productId,
          year: selectedYear,
          quarter: selectedQuarter,
          roadmapItems: []
        });
        setSelectedEpics(new Set());
      } else {
        throw new Error('Failed to load roadmap data');
      }
    } catch (err: any) {
      console.error('Error loading roadmap:', err);
      setError('Failed to load roadmap data');
    } finally {
      setLoading(false);
    }
  };

  const loadAvailableEpics = async () => {
    if (!product) return;
    
    try {
      const response = await fetch(`http://localhost:8080/api/products/${product.productId}/backlog`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const backlogData = await response.json();
        if (backlogData && backlogData.epics) {
          const epicsArray = JSON.parse(backlogData.epics);
          setAvailableEpics(epicsArray);
        }
      }
    } catch (err) {
      console.error('Error loading available epics:', err);
    }
  };

  const loadAssignedEpicIds = async () => {
    if (!product) return;
    
    try {
      // Fetch epic IDs that are already assigned to other quarters
      const response = await fetch(
        `http://localhost:8080/api/products/${product.productId}/roadmap/assigned-epics?excludeYear=${selectedYear}&excludeQuarter=${selectedQuarter}`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );

      if (response.ok) {
        const epicIds = await response.json();
        setAssignedEpicIds(new Set(epicIds));
        console.log('Assigned epic IDs from other quarters:', epicIds);
      }
    } catch (err) {
      console.error('Error loading assigned epic IDs:', err);
    }
  };
  
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  useEffect(() => {
    if (product) {
      setInlineError(''); // Clear any previous error messages
      loadRoadmapData();
      loadAvailableEpics();
      loadAssignedEpicIds();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [product, selectedYear, selectedQuarter]);

  const saveRoadmap = async () => {
    try {
      setLoading(true);
      
      let roadmapItems: RoadmapItem[];
      
      if (isEditMode) {
        // In edit mode, save the current roadmapData items
        roadmapItems = roadmapData?.roadmapItems || [];
      } else {
        // When adding/removing epics from the modal
        roadmapItems = Array.from(selectedEpics).map(epicId => {
          const epic = availableEpics.find(e => e.id === epicId);
          const existingItem = roadmapData?.roadmapItems.find(item => item.epicId === epicId);
          
          const reach = existingItem?.reach || 0;
          const impact = existingItem?.impact || 0;
          const confidence = existingItem?.confidence || 0;
          const riceScore = reach * impact * confidence;
          
          return {
            epicId,
            epicName: epic?.name || '',
            epicDescription: epic?.description || '',
            priority: existingItem?.priority || 'Medium',
            status: existingItem?.status || 'Proposed',
            estimatedEffort: existingItem?.estimatedEffort || '',
            assignedTeam: existingItem?.assignedTeam || '',
            reach,
            impact,
            confidence,
            riceScore,
            effortRating: existingItem?.effortRating || 0,
            startDate: existingItem?.startDate || getQuarterStartDate(selectedYear, selectedQuarter),
            endDate: existingItem?.endDate || getQuarterEndDate(selectedYear, selectedQuarter)
          };
        });
      }

      const requestData = {
        year: selectedYear,
        quarter: selectedQuarter,
        roadmapItems
      };

      const response = await fetch(`http://localhost:8080/api/products/${product?.productId}/roadmap`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(requestData)
      });

      if (response.ok) {
        setInlineError(''); // Clear any error messages on successful save
        await loadRoadmapData();
        if (isEditMode) {
          setIsEditMode(false);
        }
        setShowEpicModal(false);
      } else if (response.status === 409) {
        // Handle epic conflict error
        const errorText = await response.text();
        setInlineError(errorText);
      } else {
        throw new Error('Failed to save roadmap');
      }
    } catch (err) {
      setError('Failed to save roadmap');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const updateRoadmapItem = async (epicId: string, field: keyof RoadmapItem, value: string | number) => {
    if (!roadmapData) return;
    
    console.log('updateRoadmapItem called:', { epicId, field, value, isEditMode });
    
    // For effortRating, use the specific endpoint (only in view mode since it's auto-filled)
    if (field === 'effortRating' && !isEditMode) {
      try {
        console.log('Updating effort rating via specific endpoint:', { epicId, value });
        const response = await fetch(
          `http://localhost:8080/api/products/${product?.productId}/roadmap/${selectedYear}/${selectedQuarter}/epics/${epicId}/effort-rating`, 
          {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({
              effortRating: value
            })
          }
        );

        if (response.ok) {
          // Update local state on successful backend update
          const updatedItems = roadmapData.roadmapItems.map(item => {
            if (item.epicId === epicId) {
              return { ...item, effortRating: value as number };
            }
            return item;
          });
          
          setRoadmapData({
            ...roadmapData,
            roadmapItems: updatedItems
          });
          
          console.log('Effort rating updated successfully');
        } else {
          throw new Error('Failed to update effort rating');
        }
      } catch (err) {
        console.error('Error updating effort rating:', err);
      }
      return;
    }
    
    const updatedItems = roadmapData.roadmapItems.map(item => {
      if (item.epicId === epicId) {
        const updatedItem = { ...item, [field]: value };
        
        // Recalculate RICE score if any RICE component was updated
        if (['reach', 'impact', 'confidence'].includes(field)) {
          const reach = field === 'reach' ? value as number : updatedItem.reach || 0;
          const impact = field === 'impact' ? value as number : updatedItem.impact || 0;
          const confidence = field === 'confidence' ? value as number : updatedItem.confidence || 0;
          updatedItem.riceScore = reach * impact * confidence;
          console.log('RICE recalculated:', { reach, impact, confidence, riceScore: updatedItem.riceScore });
        }
        
        return updatedItem;
      }
      return item;
    });
    
    const updatedRoadmapData = {
      ...roadmapData,
      roadmapItems: updatedItems
    };
    
    setRoadmapData(updatedRoadmapData);
    
    // Only auto-save if NOT in edit mode
    if (!isEditMode) {
      console.log('Auto-saving to database (not in edit mode):', { year: selectedYear, quarter: selectedQuarter, roadmapItems: updatedItems });
      
      try {
        const response = await fetch(`http://localhost:8080/api/products/${product?.productId}/roadmap`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify({
            year: selectedYear,
            quarter: selectedQuarter,
            roadmapItems: updatedItems
          })
        });

        if (!response.ok) {
          if (response.status === 409) {
            // Handle epic conflict error in auto-save
            const errorText = await response.text();
            setInlineError(errorText);
            // Revert the local change since it conflicts
            await loadRoadmapData();
          } else {
            throw new Error('Failed to save roadmap item');
          }
        } else {
          setInlineError(''); // Clear any error messages on successful auto-save
          console.log('Roadmap saved successfully');
        }
      } catch (err) {
        console.error('Error saving roadmap item:', err);
        // Optionally show a toast notification or error message
      }
    } else {
      console.log('In edit mode - changes stored locally, not saved to database yet');
    }
  };

  const toggleEpicSelection = (epicId: string) => {
    const newSelection = new Set(selectedEpics);
    if (newSelection.has(epicId)) {
      newSelection.delete(epicId);
    } else {
      newSelection.add(epicId);
    }
    setSelectedEpics(newSelection);
  };

  // Filter epics based on search and filters, excluding those already assigned to other quarters
  const filteredAvailableEpics = availableEpics.filter(epic => {
    // First check if epic is already assigned to another quarter
    if (assignedEpicIds.has(epic.id)) {
      return false; // Don't show epics that are already in other quarters
    }
    
    const matchesSearch = epicSearchTerm === '' || 
      epic.name.toLowerCase().includes(epicSearchTerm.toLowerCase());
    const matchesTheme = selectedThemeFilter === '' || epic.themeName === selectedThemeFilter;
    const matchesInitiative = selectedInitiativeFilter === '' || epic.initiativeName === selectedInitiativeFilter;
    const matchesTrack = selectedTrackFilter === '' || epic.track === selectedTrackFilter;
    
    return matchesSearch && matchesTheme && matchesInitiative && matchesTrack;
  });

  // Get unique values for filter options
  const uniqueThemes = Array.from(new Set(availableEpics.map(epic => epic.themeName).filter(Boolean)));
  const uniqueInitiatives = Array.from(new Set(availableEpics.map(epic => epic.initiativeName).filter(Boolean)));
  const uniqueTracks = Array.from(new Set(availableEpics.map(epic => epic.track).filter(Boolean)));

  const clearEpicFilters = () => {
    setEpicSearchTerm('');
    setSelectedThemeFilter('');
    setSelectedInitiativeFilter('');
    setSelectedTrackFilter('');
  };

  const StarRating: React.FC<{
    value: number;
    onChange?: (value: number) => void;
    readOnly?: boolean;
  }> = ({ value, onChange, readOnly = false }) => {
    const [hoverValue, setHoverValue] = useState(0);

    const handleClick = (rating: number) => {
      if (!readOnly && onChange) {
        console.log('Star clicked, rating:', rating);
        onChange(rating);
      }
    };

    const handleMouseEnter = (rating: number) => {
      if (!readOnly) {
        setHoverValue(rating);
      }
    };

    const handleMouseLeave = () => {
      if (!readOnly) {
        setHoverValue(0);
      }
    };

    if (readOnly) {
      return (
        <div className="star-rating-display">
          {[1, 2, 3, 4, 5].map((star) => (
            <span
              key={star}
              className={`material-icons star-display ${star <= value ? '' : 'empty'}`}
            >
              star
            </span>
          ))}
        </div>
      );
    }

    return (
      <div className="star-rating" data-readonly={readOnly}>
        {[1, 2, 3, 4, 5].map((star) => (
          <span
            key={star}
            className={`material-icons star ${
              star <= (hoverValue || value) ? 'filled' : ''
            }`}
            onClick={() => handleClick(star)}
            onMouseEnter={() => handleMouseEnter(star)}
            onMouseLeave={handleMouseLeave}
          >
            star
          </span>
        ))}
      </div>
    );
  };

  if (loading || productLoading) {
    return (
      <div className="roadmap-container">
        <div className="loading-state">
          <div className="spinner"></div>
          <p>Loading roadmap...</p>
        </div>
      </div>
    );
  }

  if (error || productError) {
    return (
      <div className="roadmap-container">
        <div className="error-state">
          <h2>Error</h2>
          <p>{error || productError}</p>
          <div className="error-actions">
            <button onClick={() => setError('')} className="btn btn-secondary">
              <span className="material-icons">refresh</span>
              Try Again
            </button>
            <button onClick={() => navigate(`/products/${productSlug}/modules`)} className="btn btn-primary">
              <span className="material-icons">arrow_back</span>
              Back to Modules
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="roadmap-container">
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
            <h1 className="page-title">Roadmap Planner</h1>
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

          <div className="header-actions">
            {!isEditMode ? (
              <button
                onClick={() => setIsEditMode(true)}
                className="edit-mode-btn"
              >
                <span className="material-icons">edit</span>
                Edit Mode
              </button>
            ) : (
              <div className="edit-mode-controls">
                <button
                  onClick={() => {
                    loadAssignedEpicIds();
                    setShowEpicModal(true);
                  }}
                  className="add-epic-btn"
                >
                  <span className="material-icons">add</span>
                  Add/Remove Epics
                </button>
                <div className="edit-actions-row">
                  <button
                    onClick={() => {
                      setIsEditMode(false);
                      loadRoadmapData(); // Reload to discard unsaved changes
                    }}
                    className="cancel-edit-btn icon-only"
                    title="Cancel changes"
                    aria-label="Cancel changes"
                  >
                    <span className="material-icons">close</span>
                  </button>
                  <button
                    onClick={() => {
                      saveRoadmap();
                      setIsEditMode(false);
                    }}
                    className="save-btn icon-only"
                    title="Save changes"
                    aria-label="Save changes"
                  >
                    <span className="material-icons">save</span>
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="roadmap-content">
        {inlineError && !loading && (
          <div className="inline-error-banner">
            <div className="error-content">
              <span className="material-icons error-icon">warning</span>
              <span className="error-message">{inlineError}</span>
            </div>
            <button 
              onClick={() => setInlineError('')} 
              className="error-dismiss"
              aria-label="Dismiss error"
            >
              <span className="material-icons">close</span>
            </button>
          </div>
        )}
        
        <div className="quarter-info">
          <div className="quarter-header">
            <span className="material-icons quarter-icon">timeline</span>
            <div className="quarter-details">
              <h2>Q{selectedQuarter} {selectedYear} Roadmap</h2>
              <p>{roadmapData?.roadmapItems?.length || 0} items planned</p>
            </div>
          </div>
        </div>

        {isEditMode && (
          <div className="edit-mode-banner">
            <div className="edit-mode-info">
              <span className="material-icons">edit</span>
              <span>Edit Mode - You can modify scores, status, and priority</span>
            </div>
          </div>
        )}

        <div className="roadmap-items">
          {roadmapData?.roadmapItems?.length === 0 ? (
            <div className="empty-roadmap">
              <span className="material-icons">timeline</span>
              <h3>No items in roadmap</h3>
              <p>Enter edit mode to add epics from your backlog and start planning this quarter.</p>
            </div>
          ) : (
            <div className="roadmap-table-container">
              <table className="roadmap-table">
                <thead>
                  <tr>
                    <th className="col-epic">Epic</th>
                    <th className="col-initiative">Initiative</th>
                    <th className="col-theme">Theme</th>
                    <th className="col-track">Track</th>
                    <th className="col-reach">Reach</th>
                    <th className="col-impact">Impact</th>
                    <th className="col-confidence">Confidence</th>
                    <th className="col-effort-rating">Estimated Effort</th>
                    <th className="col-rice-score">RICE Score</th>
                    <th className="col-start-date">Start Date</th>
                    <th className="col-end-date">End Date</th>
                    <th className="col-status">Status</th>
                    <th className="col-priority">Priority</th>
                  </tr>
                </thead>
                <tbody>
                  {roadmapData?.roadmapItems?.map((item) => {
                    const epic = availableEpics.find(e => e.id === item.epicId);
                    return (
                    <tr key={item.epicId} className="roadmap-row">
                      <td className="col-epic">
                        <div className="epic-cell">
                          <h4 className="epic-title">{item.epicName}</h4>
                        </div>
                      </td>
                      <td className="col-initiative">
                        <span className="initiative-display">{epic?.initiativeName || '-'}</span>
                      </td>
                      <td className="col-theme">
                        <span className="theme-display">{epic?.themeName || '-'}</span>
                      </td>
                      <td className="col-track">
                        <span className="track-display">{epic?.track || '-'}</span>
                      </td>
                      <td className="col-reach">
                        <StarRating
                          value={item.reach || 0}
                          onChange={isEditMode ? (value) => updateRoadmapItem(item.epicId, 'reach', value) : undefined}
                          readOnly={!isEditMode}
                        />
                      </td>
                      <td className="col-impact">
                        <StarRating
                          value={item.impact || 0}
                          onChange={isEditMode ? (value) => updateRoadmapItem(item.epicId, 'impact', value) : undefined}
                          readOnly={!isEditMode}
                        />
                      </td>
                      <td className="col-confidence">
                        <StarRating
                          value={item.confidence || 0}
                          onChange={isEditMode ? (value) => updateRoadmapItem(item.epicId, 'confidence', value) : undefined}
                          readOnly={!isEditMode}
                        />
                      </td>
                      <td className="col-effort-rating">
                        <StarRating
                          value={item.effortRating || 0}
                          readOnly={true}
                        />
                      </td>
                      <td className="col-rice-score">
                        <span className="rice-score-display">
                          {item.reach && item.impact && item.confidence 
                            ? (item.reach * item.impact * item.confidence).toFixed(1)
                            : '-'
                          }
                        </span>
                      </td>
                      <td className="col-start-date">
                        {isEditMode ? (
                          <input
                            type="date"
                            value={item.startDate || getQuarterStartDate(selectedYear, selectedQuarter)}
                            onChange={(e) => updateRoadmapItem(item.epicId, 'startDate', e.target.value)}
                            className="date-input"
                          />
                        ) : (
                          <span className="date-display">
                            {item.startDate 
                              ? new Date(item.startDate).toLocaleDateString()
                              : new Date(getQuarterStartDate(selectedYear, selectedQuarter)).toLocaleDateString()
                            }
                          </span>
                        )}
                      </td>
                      <td className="col-end-date">
                        {isEditMode ? (
                          <input
                            type="date"
                            value={item.endDate || getQuarterEndDate(selectedYear, selectedQuarter)}
                            onChange={(e) => updateRoadmapItem(item.epicId, 'endDate', e.target.value)}
                            className="date-input"
                          />
                        ) : (
                          <span className="date-display">
                            {item.endDate 
                              ? new Date(item.endDate).toLocaleDateString()
                              : new Date(getQuarterEndDate(selectedYear, selectedQuarter)).toLocaleDateString()
                            }
                          </span>
                        )}
                      </td>
                      <td className="col-status">
                        {isEditMode ? (
                          <select
                            value={item.status}
                            onChange={(e) => updateRoadmapItem(item.epicId, 'status', e.target.value)}
                            className="status-select-table"
                          >
                            <option value="Proposed">Proposed</option>
                            <option value="Committed">Committed</option>
                            <option value="To-Do">To-Do</option>
                            <option value="In-Progress">In-Progress</option>
                            <option value="Done">Done</option>
                          </select>
                        ) : (
                          <span className={`status-badge ${item.status.toLowerCase().replace(' ', '-')}`}>
                            {item.status}
                          </span>
                        )}
                      </td>
                      <td className="col-priority">
                        {isEditMode ? (
                          <select
                            value={item.priority}
                            onChange={(e) => updateRoadmapItem(item.epicId, 'priority', e.target.value)}
                            className="priority-select-table"
                          >
                            <option value="Low">Low</option>
                            <option value="Medium">Medium</option>
                            <option value="High">High</option>
                            <option value="Critical">Critical</option>
                          </select>
                        ) : (
                          <span className={`priority-badge ${item.priority.toLowerCase()}`}>{item.priority}</span>
                        )}
                      </td>
                    </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Epic Selection Modal */}
      {showEpicModal && (
        <div className="modal-overlay">
          <div className="modal-content epic-selection-modal">
            <div className="modal-header">
              <h3>Select Epics for Q{selectedQuarter} {selectedYear}</h3>
              <button 
                className="modal-close-btn"
                onClick={() => setShowEpicModal(false)}
              >
                <span className="material-icons">close</span>
              </button>
            </div>
            
            <div className="modal-body">
              <div className="epic-filters">
                <div className="search-bar">
                  <div className="search-input-wrapper">
                    <span className="material-icons search-icon">search</span>
                    <input
                      type="text"
                      placeholder="Search epics..."
                      value={epicSearchTerm}
                      onChange={(e) => setEpicSearchTerm(e.target.value)}
                      className="search-input"
                    />
                  </div>
                </div>
                
                <div className="filter-controls">
                  <select
                    value={selectedThemeFilter}
                    onChange={(e) => setSelectedThemeFilter(e.target.value)}
                    className="filter-select"
                  >
                    <option value="">All Themes</option>
                    {uniqueThemes.map(theme => (
                      <option key={theme} value={theme}>{theme}</option>
                    ))}
                  </select>
                  
                  <select
                    value={selectedInitiativeFilter}
                    onChange={(e) => setSelectedInitiativeFilter(e.target.value)}
                    className="filter-select"
                  >
                    <option value="">All Initiatives</option>
                    {uniqueInitiatives.map(initiative => (
                      <option key={initiative} value={initiative}>{initiative}</option>
                    ))}
                  </select>
                  
                  <select
                    value={selectedTrackFilter}
                    onChange={(e) => setSelectedTrackFilter(e.target.value)}
                    className="filter-select"
                  >
                    <option value="">All Tracks</option>
                    {uniqueTracks.map(track => (
                      <option key={track} value={track}>{track}</option>
                    ))}
                  </select>
                  
                  {(epicSearchTerm || selectedThemeFilter || selectedInitiativeFilter || selectedTrackFilter) && (
                    <button
                      onClick={clearEpicFilters}
                      className="clear-epic-filters-btn"
                      title="Clear all filters"
                    >
                      <span className="material-icons">refresh</span>
                      Clear All
                    </button>
                  )}
                </div>
              </div>

              <div className="epic-selection-summary">
                <span className="epic-count">
                  {filteredAvailableEpics.length} available
                  {assignedEpicIds.size > 0 && (
                    <span className="epic-info"> ({assignedEpicIds.size} in other quarters)</span>
                  )}
                </span>
                <span className="selected-count">{selectedEpics.size} selected</span>
              </div>
              
              <div className="epic-selection-list">
                {filteredAvailableEpics.map(epic => (
                  <div 
                    key={epic.id} 
                    className={`epic-selection-item ${selectedEpics.has(epic.id) ? 'selected' : ''}`}
                    onClick={(e) => {
                      e.preventDefault();
                      e.stopPropagation();
                      toggleEpicSelection(epic.id);
                    }}
                  >
                    <div className="epic-checkbox-label">
                      <div className="epic-info">
                        <h4 className="epic-name">{epic.name}</h4>
                        <div className="epic-meta">
                          <span className="epic-initiative">{epic.initiativeName}</span>
                          <span className="epic-theme">{epic.themeName}</span>
                          <span className="epic-track">{epic.track}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
            
            <div className="modal-actions">
              <button 
                className="btn-cancel"
                onClick={() => setShowEpicModal(false)}
              >
                Cancel
              </button>
              <button 
                className="btn-confirm"
                onClick={saveRoadmap}
              >
                Add Selected Epics
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default QuarterlyRoadmap;