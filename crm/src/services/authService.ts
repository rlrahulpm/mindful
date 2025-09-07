import api from './api';
import { LoginCredentials, AuthResponse } from '../types';

export const authService = {
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    const response = await api.post('/crm/login', credentials);
    const data = response.data;
    
    // Store token and user info
    localStorage.setItem('crm_token', data.token);
    localStorage.setItem('crm_user', JSON.stringify({
      id: data.userId,
      email: data.email,
      isGlobalSuperAdmin: data.isGlobalSuperAdmin
    }));
    
    return data;
  },

  logout(): void {
    localStorage.removeItem('crm_token');
    localStorage.removeItem('crm_user');
  },

  getToken(): string | null {
    return localStorage.getItem('crm_token');
  },

  getCurrentUser(): any {
    const userStr = localStorage.getItem('crm_user');
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated(): boolean {
    return !!this.getToken();
  }
};