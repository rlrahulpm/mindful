import api from './api';
import { User, CreateUserRequest, UpdateUserRequest } from '../types';

export const userService = {
  async getUsersByOrganization(organizationId: number): Promise<User[]> {
    const response = await api.get(`/crm/organizations/${organizationId}/users`);
    return response.data;
  },


  async deleteUser(id: number): Promise<void> {
    await api.delete(`/crm/users/${id}`);
  },

  async createUser(userData: { email: string; password: string; organizationId: number }): Promise<User> {
    const response = await api.post('/crm/users', userData);
    return response.data;
  }
};