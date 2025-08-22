import api from './api';
import { ProductModule } from '../types/module';

export const moduleService = {
  getProductModules: async (productId: number): Promise<ProductModule[]> => {
    const response = await api.get(`/products/${productId}/modules`);
    return response.data;
  }
};