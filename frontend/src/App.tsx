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
import ProductBacklog from './components/ProductBacklog';
import QuarterlyRoadmap from './components/QuarterlyRoadmap';
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
              path="/products/:productSlug/modules" 
              element={
                <PrivateRoute>
                  <ProductModules />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/products/:productSlug/modules/basics" 
              element={
                <PrivateRoute>
                  <ProductBasics />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/products/:productSlug/modules/market-competition" 
              element={
                <PrivateRoute>
                  <MarketCompetition />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/products/:productSlug/modules/hypothesis" 
              element={
                <PrivateRoute>
                  <ProductHypothesis />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/products/:productSlug/modules/backlog" 
              element={
                <PrivateRoute>
                  <ProductBacklog />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/products/:productSlug/modules/roadmap" 
              element={
                <PrivateRoute>
                  <QuarterlyRoadmap />
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
