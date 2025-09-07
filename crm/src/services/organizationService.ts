import api from './api';
import { Organization, CreateOrganizationRequest, UpdateOrganizationRequest } from '../types';

export const organizationService = {
  async getAll(): Promise<Organization[]> {
    const response = await api.get('/crm/organizations');
    return response.data;
  },

  async getById(id: number): Promise<Organization> {
    const response = await api.get(`/crm/organizations/${id}`);
    return response.data;
  },

  async create(data: CreateOrganizationRequest): Promise<Organization> {
    const response = await api.post('/crm/organizations', data);
    return response.data;
  },

  async update(id: number, data: UpdateOrganizationRequest): Promise<Organization> {
    const response = await api.put(`/crm/organizations/${id}`, data);
    return response.data;
  },

  async delete(id: number): Promise<void> {
    await api.delete(`/crm/organizations/${id}`);
  }
};