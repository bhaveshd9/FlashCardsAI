import axios from 'axios';

const getRecentActivities = async (limit = 5) => {
  try {
    // Use the global axios instance which already has the interceptor configured
    const response = await axios.get(`/activities/recent?limit=${limit}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching recent activities:', error);
    // Return empty array instead of throwing to prevent dashboard from breaking
    return [];
  }
};

const logActivity = async (activityType, description, relatedId = null) => {
  try {
    // Use the global axios instance which already has the interceptor configured
    await axios.post('/activities/log', {
      activityType,
      description,
      relatedId
    });
  } catch (error) {
    console.error('Error logging activity:', error);
    // Don't throw error to prevent breaking main functionality
  }
};

export { getRecentActivities, logActivity };
