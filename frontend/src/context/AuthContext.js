import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';
import toast from 'react-hot-toast';

// Set axios defaults
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    checkAuth();
  }, []);

  // Set auth token in axios headers
  const setAuthToken = (token) => {
    if (token) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
      delete api.defaults.headers.common['Authorization'];
    }
  };

  // Check if token is expired
  const isTokenExpired = (token) => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 < Date.now();
    } catch (err) {
      return true;
    }
  };

  const checkAuth = async () => {
    const token = localStorage.getItem('flashcards_token');
    const savedUser = localStorage.getItem('flashcards_user');

    if (!token || !savedUser) {
      setLoading(false);
      return;
    }

    // Check if token is expired
    if (isTokenExpired(token)) {
      localStorage.removeItem('flashcards_token');
      localStorage.removeItem('flashcards_user');
      setUser(null);
      setIsAuthenticated(false);
      setLoading(false);
      return;
    }

    try {
      // Set auth token for subsequent requests
      setAuthToken(token);
      
      // Verify token with backend
      const response = await api.get('/auth/me');
      setUser(response.data);
      setIsAuthenticated(true);
    } catch (error) {
      console.error('Auth check failed:', error);
      // Token is invalid, clear storage
      localStorage.removeItem('flashcards_token');
      localStorage.removeItem('flashcards_user');
      setUser(null);
      setIsAuthenticated(false);
    } finally {
      setLoading(false);
    }
  };

  const login = async (email, password) => {
    try {
      const response = await api.post('/auth/login', { email, password });
      const { token, user: userData } = response.data;

      // Store token and user data
      localStorage.setItem('flashcards_token', token);
      localStorage.setItem('flashcards_user', JSON.stringify(userData));
      
      // Set auth header for subsequent requests
      setAuthToken(token);
      
      // Update state
      setUser(userData);
      setIsAuthenticated(true);
      
      toast.success('Login successful!');
      return true;
    } catch (error) {
      const message = error.response?.data?.message || 'Login failed';
      toast.error(message);
      return false;
    }
  };

  const register = async (userData) => {
    try {
      const response = await api.post('/auth/register', userData);
      const { token, user: newUser } = response.data;

      // Store token and user data
      localStorage.setItem('flashcards_token', token);
      localStorage.setItem('flashcards_user', JSON.stringify(newUser));
      
      // Set auth header for subsequent requests
      setAuthToken(token);
      
      // Update state
      setUser(newUser);
      setIsAuthenticated(true);
      
      toast.success('Registration successful!');
      return true;
    } catch (error) {
      const message = error.response?.data?.message || 'Registration failed';
      toast.error(message);
      return false;
    }
  };

  const logout = () => {
    try {
      // Clear storage
      localStorage.removeItem('flashcards_token');
      localStorage.removeItem('flashcards_user');
      
      // Clear axios auth header
      setAuthToken(null);
      
      // Reset state
      setUser(null);
      setIsAuthenticated(false);
      
      toast.success('Logged out successfully');
    } catch (error) {
      console.error('Error during logout:', error);
      toast.error('Error during logout');
    }
  };

  const updateProfile = async (profileData) => {
    try {
      const response = await api.put('/auth/profile', profileData);
      const updatedUser = response.data;
      
      // Update both state and local storage
      setUser(updatedUser);
      localStorage.setItem('flashcards_user', JSON.stringify(updatedUser));
      
      toast.success('Profile updated successfully!');
      return true;
    } catch (error) {
      const message = error.response?.data?.message || 'Profile update failed';
      toast.error(message);
      return false;
    }
  };
  
  // Add axios response interceptor to handle 401 errors
  useEffect(() => {
    const interceptor = api.interceptors.response.use(
      response => response,
      async error => {
        if (error.response?.status === 401) {
          // If we get a 401 and have a token, the token is invalid/expired
          if (localStorage.getItem('flashcards_token')) {
            // Clear auth state
            localStorage.removeItem('flashcards_token');
            localStorage.removeItem('flashcards_user');
            setAuthToken(null);
            setUser(null);
            setIsAuthenticated(false);
            
            // Show error message if not on login/register page
            if (!window.location.pathname.includes('/login') && 
                !window.location.pathname.includes('/register')) {
              toast.error('Session expired. Please log in again.');
            }
          }
        }
        return Promise.reject(error);
      }
    );

    // Cleanup interceptor on unmount
    return () => {
      api.interceptors.response.eject(interceptor);
    };
  }, []);

  const value = {
    user,
    loading,
    isAuthenticated,
    login,
    register,
    logout,
    updateProfile,
    checkAuth
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}; 