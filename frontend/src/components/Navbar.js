import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Home, BookOpen, User, LogOut, MessageSquare, Settings, Globe } from 'lucide-react';
import LanguageSelector from './LanguageSelector';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [currentLanguage, setCurrentLanguage] = React.useState('en');

  const isActive = (path) => {
    return location.pathname === path;
  };

  const handleLanguageChange = (language) => {
    setCurrentLanguage(language);
    // In a real app, you'd update the i18n context here
    console.log('Language changed to:', language);
  };

  return (
    <nav className="bg-white shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center space-x-8">
            <div className="flex-shrink-0">
              <h1 className="text-xl font-bold text-indigo-600">FlashCards AI</h1>
            </div>
            
            <div className="hidden md:block">
              <div className="ml-10 flex items-baseline space-x-4">
                <button
                  onClick={() => navigate('/dashboard')}
                  className={`flex items-center px-3 py-2 rounded-md text-sm font-medium ${
                    isActive('/dashboard') 
                      ? 'bg-indigo-100 text-indigo-700' 
                      : 'text-gray-700 hover:text-gray-900 hover:bg-gray-50'
                  }`}
                >
                  <Home className="h-4 w-4 mr-2" />
                  Dashboard
                </button>
                
                <button
                  onClick={() => navigate('/decks')}
                  className={`flex items-center px-3 py-2 rounded-md text-sm font-medium ${
                    isActive('/decks') 
                      ? 'bg-indigo-100 text-indigo-700' 
                      : 'text-gray-700 hover:text-gray-900 hover:bg-gray-50'
                  }`}
                >
                  <BookOpen className="h-4 w-4 mr-2" />
                  My Decks
                </button>
                
                <button
                  onClick={() => navigate('/feedback')}
                  className={`flex items-center px-3 py-2 rounded-md text-sm font-medium ${
                    isActive('/feedback') 
                      ? 'bg-indigo-100 text-indigo-700' 
                      : 'text-gray-700 hover:text-gray-900 hover:bg-gray-50'
                  }`}
                >
                  <MessageSquare className="h-4 w-4 mr-2" />
                  Feedback
                </button>
                
                {user?.role === 'ADMIN' && (
                  <button
                    onClick={() => navigate('/admin')}
                    className={`flex items-center px-3 py-2 rounded-md text-sm font-medium ${
                      isActive('/admin') 
                        ? 'bg-indigo-100 text-indigo-700' 
                        : 'text-gray-700 hover:text-gray-900 hover:bg-gray-50'
                    }`}
                  >
                    <Settings className="h-4 w-4 mr-2" />
                    Admin
                  </button>
                )}
              </div>
            </div>
          </div>
          
          <div className="flex items-center space-x-4">
            <LanguageSelector 
              currentLanguage={currentLanguage} 
              onLanguageChange={handleLanguageChange} 
            />
            
            <div className="flex items-center space-x-2">
              <User className="h-4 w-4 text-gray-500" />
              <span className="text-sm text-gray-700">{user?.name || user?.username}</span>
            </div>
            
            <button
              onClick={logout}
              className="flex items-center px-3 py-2 rounded-md text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-50"
            >
              <LogOut className="h-4 w-4 mr-2" />
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar; 