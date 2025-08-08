import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import axios from 'axios';
import { Toaster } from 'react-hot-toast';

// Components
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import DeckList from './components/DeckList';
import DeckDetail from './components/DeckDetail';
import StudyMode from './components/StudyMode';
import Quiz from './components/Quiz';
import AdminPanel from './components/AdminPanel';
import Feedback from './components/Feedback';
import Navbar from './components/Navbar';

// Context
import { AuthProvider, useAuth } from './context/AuthContext';

// API Configuration
axios.defaults.baseURL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

// Add token to requests
axios.interceptors.request.use((config) => {
  const token = localStorage.getItem('flashcards_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle token expiration
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('flashcards_token');
      localStorage.removeItem('flashcards_user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="min-h-screen bg-gray-50">
          <Toaster position="top-right" />
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/" element={<PrivateRoute><><Navbar /><Dashboard /></></PrivateRoute>} />
            <Route path="/dashboard" element={<PrivateRoute><><Navbar /><Dashboard /></></PrivateRoute>} />
            <Route path="/decks" element={<PrivateRoute><><Navbar /><DeckList /></></PrivateRoute>} />
            <Route path="/decks/:id" element={<PrivateRoute><><Navbar /><DeckDetail /></></PrivateRoute>} />
            <Route path="/study/:deckId" element={<PrivateRoute><><Navbar /><StudyMode /></></PrivateRoute>} />
            <Route path="/quiz/:deckId" element={<PrivateRoute><><Navbar /><Quiz /></></PrivateRoute>} />
            <Route path="/admin" element={<PrivateRoute><><Navbar /><AdminPanel /></></PrivateRoute>} />
            <Route path="/feedback" element={<PrivateRoute><><Navbar /><Feedback /></></PrivateRoute>} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

function PrivateRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <div className="flex items-center justify-center min-h-screen">Loading...</div>;
  }

  return isAuthenticated ? children : <Navigate to="/login" />;
}

export default App; 