import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const getRecentActivities = async (limit = 5) => {
  try {
    const token = localStorage.getItem('flashcards_token');
    const response = await axios.get(`${API_URL}/activities/recent?limit=${limit}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching recent activities:', error);
    throw error;
  }
};

const logActivity = async (activityType, description, relatedId = null) => {
  try {
    const token = localStorage.getItem('flashcards_token');
    await axios.post(
      `${API_URL}/activities/log`,
      { activityType, description, relatedId },
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );
  } catch (error) {
    console.error('Error logging activity:', error);
    // Don't throw error to prevent breaking main functionality
  }
};

export { getRecentActivities, logActivity };
