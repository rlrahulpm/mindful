import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import SuperadminRoute from './components/SuperadminRoute';
import Navbar from './components/Navbar';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import ProductModules from './components/ProductModules';
import ProductBasics from './components/ProductBasics';
import MarketCompetition from './components/MarketCompetition';
import ProductHypothesis from './components/ProductHypothesis';
import AdminDashboard from './components/AdminDashboard';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Navbar />
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/login" element={<Login />} />
            <Route 
              path="/dashboard" 
              element={
                <PrivateRoute>
                  <Dashboard />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/products/:productId/modules" 
              element={
                <PrivateRoute>
                  <ProductModules />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/products/:productId/modules/basics" 
              element={
                <PrivateRoute>
                  <ProductBasics />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/products/:productId/modules/market-competition" 
              element={
                <PrivateRoute>
                  <MarketCompetition />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/products/:productId/modules/hypothesis" 
              element={
                <PrivateRoute>
                  <ProductHypothesis />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/admin" 
              element={
                <SuperadminRoute>
                  <AdminDashboard />
                </SuperadminRoute>
              } 
            />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
