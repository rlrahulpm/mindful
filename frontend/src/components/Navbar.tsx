import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <nav style={{ 
      padding: '10px 20px', 
      backgroundColor: '#007bff', 
      color: 'white',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center'
    }}>
      <Link to="/" style={{ color: 'white', textDecoration: 'none', fontSize: '24px', fontWeight: 'bold' }}>
        Product Manager
      </Link>
      
      <div>
        {user ? (
          <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
            <span>Welcome, {user.email}</span>
            <button 
              onClick={logout}
              style={{ 
                padding: '8px 15px', 
                backgroundColor: 'transparent', 
                color: 'white', 
                border: '1px solid white', 
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              Logout
            </button>
          </div>
        ) : (
          <div style={{ display: 'flex', gap: '10px' }}>
            <Link 
              to="/login" 
              style={{ 
                color: 'white', 
                textDecoration: 'none', 
                padding: '8px 15px',
                border: '1px solid white',
                borderRadius: '4px'
              }}
            >
              Login
            </Link>
            <Link 
              to="/signup" 
              style={{ 
                color: '#007bff', 
                backgroundColor: 'white',
                textDecoration: 'none', 
                padding: '8px 15px',
                borderRadius: '4px'
              }}
            >
              Sign Up
            </Link>
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;