import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { productService } from '../services/productService';
import { Product } from '../types/product';

const Dashboard: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [newProductName, setNewProductName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isAddingProduct, setIsAddingProduct] = useState(false);
  const { user, logout } = useAuth();

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const productsData = await productService.getProducts();
      setProducts(productsData);
    } catch (err: any) {
      setError('Failed to load products');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newProductName.trim()) return;

    try {
      setIsAddingProduct(true);
      const newProduct = await productService.createProduct({ productName: newProductName });
      setProducts([...products, newProduct]);
      setNewProductName('');
    } catch (err: any) {
      setError('Failed to add product');
      console.error(err);
    } finally {
      setIsAddingProduct(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div style={{ maxWidth: '800px', margin: '20px auto', padding: '20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <h1>Product Management Dashboard</h1>
        <div>
          <span style={{ marginRight: '15px' }}>Welcome, {user?.email}</span>
          <button 
            onClick={logout}
            style={{ padding: '8px 15px', backgroundColor: '#dc3545', color: 'white', border: 'none', borderRadius: '4px' }}
          >
            Logout
          </button>
        </div>
      </div>

      <div style={{ marginBottom: '30px', padding: '20px', border: '1px solid #ccc', borderRadius: '8px' }}>
        <h3>Add New Product</h3>
        <form onSubmit={handleAddProduct} style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <input
            type="text"
            value={newProductName}
            onChange={(e) => setNewProductName(e.target.value)}
            placeholder="Enter product name"
            required
            style={{ flex: 1, padding: '10px', border: '1px solid #ccc', borderRadius: '4px' }}
          />
          <button 
            type="submit" 
            disabled={isAddingProduct}
            style={{ 
              padding: '10px 20px', 
              backgroundColor: '#007bff', 
              color: 'white', 
              border: 'none', 
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            {isAddingProduct ? 'Adding...' : 'Add Product'}
          </button>
        </form>
      </div>

      <div>
        <h3>Your Products</h3>
        {error && <div style={{ color: 'red', marginBottom: '15px' }}>{error}</div>}
        
        {loading ? (
          <div>Loading products...</div>
        ) : products.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
            No products found. Add your first product above!
          </div>
        ) : (
          <div style={{ display: 'grid', gap: '15px' }}>
            {products.map((product) => (
              <div 
                key={product.productId} 
                style={{ 
                  padding: '15px', 
                  border: '1px solid #ddd', 
                  borderRadius: '8px', 
                  backgroundColor: '#f9f9f9' 
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <h4 style={{ margin: '0 0 5px 0', color: '#333' }}>{product.productName}</h4>
                    <small style={{ color: '#666' }}>
                      Created: {formatDate(product.createdAt)}
                    </small>
                  </div>
                  <div style={{ color: '#007bff', fontWeight: 'bold' }}>
                    ID: {product.productId}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;