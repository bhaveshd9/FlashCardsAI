import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import { BookOpen, Plus, User, BarChart3, CheckCircle, PlusCircle, Award, Settings, MessageSquare } from 'lucide-react';
import axios from 'axios';
import { getRecentActivities } from '../services/activityService';
import LanguageSelector from './LanguageSelector';

const Dashboard = () => {
  const { t } = useTranslation();
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [stats, setStats] = useState({
    totalDecks: 0,
    studyStreak: 0,
    cardsStudiedToday: 0,
    recentActivities: []
  });
  const [loading, setLoading] = useState(true);

  // Fetch data on initial load and when location changes to dashboard
  useEffect(() => {
    // Only fetch if we're on the dashboard route
    if (location.pathname === '/dashboard' || location.pathname === '/') {
      fetchDashboardStats();
    }
  }, [location.pathname]);

  // Refetch data when component becomes visible (tab switch)
  useEffect(() => {
    const handleFocus = () => {
      if (document.visibilityState === 'visible' && 
          (location.pathname === '/dashboard' || location.pathname === '/')) {
        fetchDashboardStats();
      }
    };
    
    document.addEventListener('visibilitychange', handleFocus);
    return () => document.removeEventListener('visibilitychange', handleFocus);
  }, [location.pathname]);

  const fetchDashboardStats = async () => {
    try {
      setLoading(true);
      
      // Fetch decks in parallel with activities
      const [decksResponse, activitiesResponse] = await Promise.all([
        axios.get('/decks/my', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('flashcards_token')}`
          }
        }),
        getRecentActivities(5) // Get 5 most recent activities
      ]);
      
      // Calculate study streak (mock implementation - replace with actual logic)
      const studyStreak = Math.floor(Math.random() * 10); // Mock data
      
      // Calculate cards studied today (mock implementation - replace with actual logic)
      const cardsStudiedToday = Math.floor(Math.random() * 20); // Mock data
      
      setStats({
        totalDecks: decksResponse.data.length,
        studyStreak,
        cardsStudiedToday,
        recentActivities: activitiesResponse || []
      });
      
    } catch (error) {
      console.error('Error fetching dashboard stats:', error);
      console.error('Error response:', error.response?.data);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  // Quick action items
  const quickActions = [
    {
      id: 'create-deck',
      title: 'Create New Deck',
      description: 'Start building your knowledge',
      icon: Plus,
      iconColor: 'text-green-600',
      bgColor: 'bg-green-50 hover:bg-green-100',
      textColor: 'text-green-600',
      onClick: () => navigate('/decks')
    },
    {
      id: 'study',
      title: 'Study Now',
      description: 'Continue your learning',
      icon: BookOpen,
      iconColor: 'text-blue-600',
      bgColor: 'bg-blue-50 hover:bg-blue-100',
      textColor: 'text-blue-600',
      onClick: () => navigate('/study')
    },
    {
      id: 'stats',
      title: 'View Stats',
      description: 'Track your progress',
      icon: BarChart3,
      iconColor: 'text-purple-600',
      bgColor: 'bg-purple-50 hover:bg-purple-100',
      textColor: 'text-purple-600',
      onClick: () => navigate('/stats')
    }
  ];

  // Admin actions
  const adminActions = user?.role === 'ADMIN' ? [
    {
      id: 'admin',
      title: 'Admin Panel',
      description: 'Manage system settings',
      icon: Settings,
      iconColor: 'text-red-600',
      bgColor: 'bg-red-50 hover:bg-red-100',
      textColor: 'text-red-600',
      onClick: () => navigate('/admin')
    }
  ] : [];

  // Combine all actions
  const allActions = [...quickActions, ...adminActions];

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {/* Header */}
          <div className="flex justify-between items-center mb-8">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">{t('dashboard.title')}</h1>
              <p className="text-gray-600">{t('common.welcome', { name: user?.name || t('common.user') })}</p>
            </div>
            <div className="flex items-center space-x-4">
              <LanguageSelector />
              <button
                onClick={logout}
                className="flex items-center space-x-2 px-4 py-2 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 transition-colors"
              >
                <span>{t('common.signOut')}</span>
              </button>
            </div>
          </div>

          {/* Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            {/* Total Decks Card */}
            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-500">
                    {t('dashboard.totalDecks')}
                  </p>
                  <p className="text-3xl font-bold text-gray-900 mt-1">
                    {stats.totalDecks}
                  </p>
                </div>
                <div className="p-3 rounded-full bg-indigo-50">
                  <BookOpen className="h-6 w-6 text-indigo-600" />
                </div>
              </div>
            </div>

            {/* Study Streak Card */}
            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-500">
                    {t('dashboard.studyStreak')}
                  </p>
                  <p className="text-3xl font-bold text-gray-900 mt-1">
                    {stats.studyStreak} {t('common.days')}
                  </p>
                </div>
                <div className="p-3 rounded-full bg-yellow-50">
                  <Award className="h-6 w-6 text-yellow-600" />
                </div>
              </div>
            </div>

            {/* Cards Studied Today Card */}
            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-500">
                    {t('dashboard.cardsStudiedToday')}
                  </p>
                  <p className="text-3xl font-bold text-gray-900 mt-1">
                    {stats.cardsStudiedToday}
                  </p>
                </div>
                <div className="p-3 rounded-full bg-green-50">
                  <CheckCircle className="h-6 w-6 text-green-600" />
                </div>
              </div>
            </div>
            {/* Cards Studied */}
            <div className="bg-white overflow-hidden shadow rounded-lg hover:shadow-md transition-shadow">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0 bg-green-100 p-3 rounded-lg">
                    <CheckCircle className="h-6 w-6 text-green-600" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500 truncate">Cards Studied</p>
                    <p className="text-2xl font-semibold text-gray-900">{stats.cardsStudiedToday} today</p>
                  </div>
                </div>
                <div className="mt-4">
                  <div className="flex items-center text-sm text-green-600">
                    <span>📊 {stats.cardsStudiedToday > 0 ? 'Great job!' : 'Ready to study?'}</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Quick Actions Summary */}
            <div className="bg-white overflow-hidden shadow rounded-lg hover:shadow-md transition-shadow">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0 bg-blue-100 p-3 rounded-lg">
                    <PlusCircle className="h-6 w-6 text-blue-600" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500 truncate">Quick Actions</p>
                    <p className="text-2xl font-semibold text-gray-900">{allActions.length} available</p>
                  </div>
                </div>
                <div className="mt-4">
                  <div className="flex space-x-2">
                    {allActions.slice(0, 3).map((action) => (
                      <div key={action.id} className="h-2 w-2 rounded-full bg-blue-200"></div>
                    ))}
                    {allActions.length > 3 && (
                      <div className="h-2 w-2 rounded-full bg-blue-200 opacity-50"></div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="mb-8">
            <h2 className="text-lg font-medium text-gray-900 mb-4 flex items-center">
              <span className="bg-indigo-100 p-1.5 rounded-lg mr-2">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-indigo-600" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 0v2H7a1 1 0 100 2h2v2a1 1 0 102 0v-2h2a1 1 0 100-2h-2V7z" clipRule="evenodd" />
                </svg>
              </span>
              Quick Actions
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {allActions.map((action) => {
                const Icon = action.icon;
                return (
                  <div 
                    key={action.id}
                    onClick={action.onClick}
                    className={`${action.bgColor} p-4 rounded-lg cursor-pointer transition-colors border border-transparent hover:border-${action.textColor.split('-')[1]}-300`}
                  >
                    <div className="flex items-start">
                      <div className={`flex-shrink-0 p-2 rounded-lg ${action.iconColor.replace('text-', 'bg-')} bg-opacity-20`}>
                        <Icon className="h-5 w-5" />
                      </div>
                      <div className="ml-4">
                        <h3 className={`text-sm font-medium ${action.textColor}`}>{action.title}</h3>
                        <p className="mt-1 text-sm text-gray-600">{action.description}</p>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Recent Activity Section */}
          <div className="mt-8">
            <h2 className="text-lg font-medium text-gray-900 mb-4 flex items-center">
              <span className="bg-indigo-100 p-1.5 rounded-lg mr-2">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-indigo-600" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 2a1 1 0 011 1v1.323l3.954 1.582 1.599-.8a1 1 0 01.894 1.79l-1.233.616 1.738 5.42a1.5 1.5 0 01-2.15 1.767l-.175-.087a3 3 0 10-2.48 1.304 1.5 1.5 0 01-2.9 0 3 3 0 10-2.48-1.304l-.175.087a1.5 1.5 0 01-2.15-1.767l1.737-5.42-1.233-.617a1 1 0 01.894-1.789l1.599.8L9 4.323V3a1 1 0 011-1zm-5 8.274l-.818 2.552a.5.5 0 00.717.59l2.138-1.069a4 4 0 013.165 0l2.138 1.069a.5.5 0 00.717-.59L15 10.274 12.223 9.12a4 4 0 01-4.446 0L5 10.274z" clipRule="evenodd" />
                </svg>
              </span>
              Recent Activity
            </h2>
            <div className="bg-white shadow-sm rounded-lg overflow-hidden">
              {stats.recentActivities.length > 0 ? (
                <ul className="divide-y divide-gray-100">
                  {stats.recentActivities.map((activity, index) => (
                    <li key={index} className="px-4 py-3 hover:bg-gray-50 transition-colors">
                      <div className="flex">
                        <div className="flex-shrink-0 mr-3">
                          <div className="h-10 w-10 rounded-full bg-indigo-50 flex items-center justify-center">
                            {getActivityIcon(activity.type, 'h-5 w-5 text-indigo-600')}
                          </div>
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium text-gray-900 truncate">
                            {activity.description}
                          </p>
                          <div className="mt-1 flex justify-between items-center">
                            <p className="text-xs text-gray-500">
                              {formatActivityDate(activity.timestamp)}
                            </p>
                            {activity.action && (
                              <button 
                                onClick={() => activity.action.onClick()} 
                                className="text-xs font-medium text-indigo-600 hover:text-indigo-500 hover:underline"
                              >
                                {activity.action.label}
                              </button>
                            )}
                          </div>
                        </div>
                      </div>
                    </li>
                  ))}
                </ul>
              ) : (
                <div className="px-6 py-8 text-center">
                  <svg
                    className="mx-auto h-12 w-12 text-gray-400"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    aria-hidden="true"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={1}
                      d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                    />
                  </svg>
                  <h3 className="mt-2 text-sm font-medium text-gray-900">No activity yet</h3>
                  <p className="mt-1 text-sm text-gray-500">Your recent activities will appear here.</p>
                  <div className="mt-6">
                    <button
                      type="button"
                      onClick={() => navigate('/decks/new')}
                      className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                    >
                      <Plus className="-ml-1 mr-2 h-4 w-4" />
                      Create your first deck
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

// Helper function to get icon based on activity type
const getActivityIcon = (activityType, className = 'h-5 w-5') => {
  const baseClass = className || 'h-5 w-5';
  const iconProps = { className: baseClass };
  
  switch(activityType) {
    case 'DECK_CREATED':
      return <Plus {...iconProps} />;
    case 'DECK_STUDIED':
      return <BookOpen {...iconProps} />;
    case 'FLASHCARD_ADDED':
      return <PlusCircle {...iconProps} />;
    case 'ACHIEVEMENT_UNLOCKED':
      return <Award {...iconProps} />;
    case 'STUDY_SESSION_COMPLETED':
      return <CheckCircle {...iconProps} />;
    case 'DECK_SHARED':
      return <User {...iconProps} />;
    default:
      return <MessageSquare {...iconProps} />;
  }
};

// Format activity date to be more readable
const formatActivityDate = (dateString) => {
  if (!dateString) return 'Just now';
  
  const date = new Date(dateString);
  const now = new Date();
  const diffInSeconds = Math.floor((now - date) / 1000);
  const diffInMinutes = Math.floor(diffInSeconds / 60);
  const diffInHours = Math.floor(diffInSeconds / 3600);
  const diffInDays = Math.floor(diffInSeconds / 86400);
  
  if (diffInSeconds < 60) return 'Just now';
  if (diffInSeconds < 3600) return `${diffInMinutes}m ago`;
  if (diffInSeconds < 86400) return `${diffInHours}h ago`;
  if (diffInDays === 1) return 'Yesterday';
  if (diffInDays < 7) return `${diffInDays}d ago`;
  
  // For dates older than a week, show the actual date
  const isCurrentYear = date.getFullYear() === now.getFullYear();
  
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: isCurrentYear ? undefined : 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: true
  });
};

export default Dashboard;