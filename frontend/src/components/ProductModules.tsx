import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ProductModule } from '../types/module';
import { Product } from '../types/product';
import { moduleService } from '../services/moduleService';
import { productService } from '../services/productService';
import { useAuth } from '../context/AuthContext';
import './ProductModules.css';

const ProductModules: React.FC = () => {
  const { productId } = useParams<{ productId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [product, setProduct] = useState<Product | null>(null);
  const [modules, setModules] = useState<ProductModule[]>([]);
  const [filteredModules, setFilteredModules] = useState<ProductModule[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (productId) {
      loadProductAndModules();
    }
  }, [productId]);

  useEffect(() => {
    if (user && modules.length > 0) {
      filterModulesByUserRole();
    }
  }, [user, modules]);

  const loadProductAndModules = async () => {
    try {
      setLoading(true);
      const productIdNum = parseInt(productId!, 10);
      
      // Load product details and modules in parallel
      const [productData, modulesData] = await Promise.all([
        productService.getProduct(productIdNum),
        moduleService.getProductModules(productIdNum)
      ]);
      
      setProduct(productData);
      setModules(modulesData);
    } catch (err: any) {
      setError('Failed to load product modules');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const filterModulesByUserRole = async () => {
    try {
      if (!user) {
        setFilteredModules([]);
        return;
      }

      // If user is superadmin, show all modules
      if (user.isSuperadmin) {
        setFilteredModules(modules);
        return;
      }

      // Get user's role and allowed modules from backend
      const response = await fetch(`http://localhost:8080/api/admin/users/${user.id}/role-modules`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const userRoleModules = await response.json();
        const allowedProductModuleIds = userRoleModules.map((pm: any) => pm.id);
        
        // Filter modules based on user's role permissions
        const accessible = modules.filter(productModule => 
          allowedProductModuleIds.includes(productModule.id)
        );
        setFilteredModules(accessible);
      } else {
        // If no role or API error, show no modules
        setFilteredModules([]);
      }
    } catch (err) {
      console.error('Error filtering modules by role:', err);
      setFilteredModules([]);
    }
  };


  if (loading) {
    return (
      <div className="modules-container">
        <div className="loading-state">
          <div className="spinner"></div>
          <p>Loading modules...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="modules-container">
        <div className="error-state">
          <h2>Error</h2>
          <p>{error}</p>
          <button onClick={() => navigate('/dashboard')} className="btn btn-primary">
            <span className="material-icons">arrow_back</span>
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="modules-container">
      <div className="page-header">
        <div className="header-top-row">
          <div className="header-left">
            <button 
              onClick={() => navigate('/dashboard')} 
              className="back-button"
              aria-label="Back to dashboard"
            >
              <span className="material-icons">arrow_back</span>
            </button>
            <h1 className="page-title">Product Modules</h1>
          </div>
        </div>
      </div>

      <div className="modules-grid">
        {filteredModules.map((productModule) => (
          <div 
            key={productModule.id} 
            className={`module-card ${productModule.isEnabled ? 'enabled' : 'disabled'}`}
          >
            <div className="module-icon">
              <span className="material-icons">
                {productModule.module.name === 'Product Basics' ? 'assignment' : 
                 productModule.module.name === 'Market & Competition Analysis' ? 'analytics' :
                 productModule.module.name === 'Product Hypothesis' ? 'lightbulb' : 'extension'}
              </span>
            </div>
            <div className="module-content">
              <h3 className="module-name">{productModule.module.name}</h3>
              <p className="module-description">{productModule.module.description}</p>
            </div>
            
            <div className="module-actions">
              <button 
                className="view-btn"
                onClick={() => {
                  if (productModule.module.name === 'Product Basics') {
                    navigate(`/products/${productId}/modules/basics`);
                  } else if (productModule.module.name === 'Market & Competition Analysis') {
                    navigate(`/products/${productId}/modules/market-competition`);
                  } else if (productModule.module.name === 'Product Hypothesis') {
                    navigate(`/products/${productId}/modules/hypothesis`);
                  } else if (productModule.module.name === 'Product Backlog') {
                    navigate(`/products/${productId}/modules/backlog`);
                  } else {
                    console.log('Viewing module:', productModule.module.name);
                  }
                }}
              >
                View
              </button>
            </div>
          </div>
        ))}
      </div>

      {filteredModules.length === 0 && !loading && (
        <div className="empty-state">
          <div className="empty-icon">
            <span className="material-icons">assignment</span>
          </div>
          <h3>No modules available</h3>
          <p>This product doesn't have any modules configured yet.</p>
        </div>
      )}
    </div>
  );
};

export default ProductModules;