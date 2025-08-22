import React, { createContext, useContext, useState, useEffect, ReactNode, useRef } from 'react';
import { User, AuthContextType } from '../types/auth';
import { authService } from '../services/authService';
import { shouldRefreshToken, isTokenExpired } from '../utils/jwtUtils';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const refreshTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const refreshTokenIfNeeded = async () => {
    const currentToken = authService.getStoredToken();
    if (!currentToken) return;

    if (isTokenExpired(currentToken)) {
      logout();
      return;
    }

    if (shouldRefreshToken(currentToken)) {
      try {
        const response = await authService.refreshToken();
        const userData = { id: response.userId, email: response.email, isSuperadmin: response.isSuperadmin };
        
        authService.setAuthToken(response.token);
        authService.setStoredUser(userData);
        
        setToken(response.token);
        setUser(userData);
        
        console.log('Token refreshed successfully');
      } catch (error) {
        console.error('Failed to refresh token:', error);
        logout();
      }
    }
  };

  const scheduleTokenRefresh = () => {
    if (refreshTimeoutRef.current) {
      clearTimeout(refreshTimeoutRef.current);
    }

    // Check every 2 minutes for token refresh
    refreshTimeoutRef.current = setTimeout(() => {
      refreshTokenIfNeeded();
      scheduleTokenRefresh();
    }, 2 * 60 * 1000);
  };

  useEffect(() => {
    const storedToken = authService.getStoredToken();
    const storedUser = authService.getStoredUser();
    
    if (storedToken && storedUser && !isTokenExpired(storedToken)) {
      setToken(storedToken);
      setUser(storedUser);
      scheduleTokenRefresh();
    } else if (storedToken && isTokenExpired(storedToken)) {
      authService.removeAuthToken();
    }
    setIsLoading(false);
  }, []);

  useEffect(() => {
    return () => {
      if (refreshTimeoutRef.current) {
        clearTimeout(refreshTimeoutRef.current);
      }
    };
  }, []);

  const login = async (email: string, password: string) => {
    setIsLoading(true);
    try {
      const response = await authService.login({ email, password });
      const userData = { id: response.userId, email: response.email, isSuperadmin: response.isSuperadmin };
      
      authService.setAuthToken(response.token);
      authService.setStoredUser(userData);
      
      setToken(response.token);
      setUser(userData);
      
      scheduleTokenRefresh();
    } catch (error) {
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const signup = async (email: string, password: string) => {
    setIsLoading(true);
    try {
      await authService.signup({ email, password });
    } catch (error) {
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    if (refreshTimeoutRef.current) {
      clearTimeout(refreshTimeoutRef.current);
      refreshTimeoutRef.current = null;
    }
    
    authService.removeAuthToken();
    setToken(null);
    setUser(null);
    authService.logout().catch(console.error);
  };

  const value: AuthContextType = {
    user,
    token,
    login,
    signup,
    logout,
    isLoading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};