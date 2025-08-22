import api from './api';
import { AuthResponse, LoginRequest, SignupRequest } from '../types/auth';

export const authService = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await api.post('/auth/login', credentials);
    return response.data;
  },

  async signup(userData: SignupRequest): Promise<any> {
    const response = await api.post('/auth/signup', userData);
    return response.data;
  },

  async logout(): Promise<void> {
    await api.post('/auth/logout');
  },

  setAuthToken(token: string) {
    localStorage.setItem('token', token);
  },

  removeAuthToken() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getStoredToken(): string | null {
    return localStorage.getItem('token');
  },

  getStoredUser(): any {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },

  setStoredUser(user: any) {
    localStorage.setItem('user', JSON.stringify(user));
  }
};