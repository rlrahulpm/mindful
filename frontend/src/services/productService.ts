import api from './api';
import { Product, ProductRequest } from '../types/product';

export const productService = {
  async getProducts(): Promise<Product[]> {
    const response = await api.get('/products');
    return response.data;
  },

  async createProduct(productData: ProductRequest): Promise<Product> {
    const response = await api.post('/products', productData);
    return response.data;
  },

  async getProduct(id: number): Promise<Product> {
    const response = await api.get(`/products/${id}`);
    return response.data;
  }
};