import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { MessageSquare, Send, CheckCircle } from 'lucide-react';
import axios from 'axios';
import toast from 'react-hot-toast';

const Feedback = () => {
  const { user } = useAuth();
  
  const [feedback, setFeedback] = useState({
    subject: '',
    message: '',
    type: 'GENERAL'
  });
  const [myFeedback, setMyFeedback] = useState([]);
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  useEffect(() => {
    fetchMyFeedback();
  }, []);

  const fetchMyFeedback = async () => {
    try {
      const response = await axios.get('/feedback/my');
      setMyFeedback(response.data);
    } catch (error) {
      console.error('Error fetching feedback:', error);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      await axios.post('/feedback', feedback);
      setFeedback({ subject: '', message: '', type: 'GENERAL' });
      setSubmitted(true);
      fetchMyFeedback();
      toast.success('Feedback submitted successfully!');
      
      // Reset submitted state after 3 seconds
      setTimeout(() => setSubmitted(false), 3000);
    } catch (error) {
      console.error('Error submitting feedback:', error);
      toast.error('Failed to submit feedback');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING': return 'bg-yellow-100 text-yellow-800';
      case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800';
      case 'RESOLVED': return 'bg-green-100 text-green-800';
      case 'CLOSED': return 'bg-gray-100 text-gray-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  if (submitted) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <CheckCircle className="h-16 w-16 text-green-500 mx-auto mb-4" />
          <h1 className="text-2xl font-bold text-gray-900 mb-2">Thank You!</h1>
          <p className="text-gray-600 mb-4">Your feedback has been submitted successfully.</p>
          <p className="text-sm text-gray-500">We'll review it and get back to you soon.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900">Feedback</h1>
            <p className="text-gray-600">Help us improve by sharing your thoughts</p>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Submit Feedback Form */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Submit Feedback</h2>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Type
                  </label>
                  <select
                    value={feedback.type}
                    onChange={(e) => setFeedback({...feedback, type: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                    <option value="GENERAL">General Feedback</option>
                    <option value="BUG_REPORT">Bug Report</option>
                    <option value="FEATURE_REQUEST">Feature Request</option>
                    <option value="IMPROVEMENT">Improvement Suggestion</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Subject
                  </label>
                  <input
                    type="text"
                    required
                    value={feedback.subject}
                    onChange={(e) => setFeedback({...feedback, subject: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    placeholder="Brief description of your feedback"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Message
                  </label>
                  <textarea
                    required
                    value={feedback.message}
                    onChange={(e) => setFeedback({...feedback, message: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    rows="6"
                    placeholder="Please provide detailed feedback..."
                  />
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white py-2 px-4 rounded-md flex items-center justify-center"
                >
                  {loading ? (
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                  ) : (
                    <>
                      <Send className="h-4 w-4 mr-2" />
                      Submit Feedback
                    </>
                  )}
                </button>
              </form>
            </div>

            {/* My Feedback History */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">My Feedback History</h2>
              {myFeedback.length === 0 ? (
                <div className="text-center py-8">
                  <MessageSquare className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                  <p className="text-gray-500">No feedback submitted yet</p>
                </div>
              ) : (
                <div className="space-y-4 max-h-96 overflow-y-auto">
                  {myFeedback.map((fb) => (
                    <div key={fb.id} className="border border-gray-200 rounded-lg p-4">
                      <div className="flex justify-between items-start mb-2">
                        <div>
                          <h4 className="text-sm font-medium text-gray-900">{fb.subject}</h4>
                          <p className="text-xs text-gray-500 capitalize">{fb.type.toLowerCase().replace('_', ' ')}</p>
                        </div>
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(fb.status)}`}>
                          {fb.status}
                        </span>
                      </div>
                      <p className="text-sm text-gray-700 mb-2">{fb.message}</p>
                      <div className="text-xs text-gray-500">
                        {new Date(fb.createdAt).toLocaleString()}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Feedback Guidelines */}
          <div className="mt-8 bg-blue-50 rounded-lg p-6">
            <h3 className="text-lg font-medium text-blue-900 mb-3">Feedback Guidelines</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm text-blue-800">
              <div>
                <h4 className="font-medium mb-2">For Bug Reports:</h4>
                <ul className="list-disc list-inside space-y-1">
                  <li>Describe the steps to reproduce</li>
                  <li>Include what you expected vs what happened</li>
                  <li>Mention your browser and device</li>
                </ul>
              </div>
              <div>
                <h4 className="font-medium mb-2">For Feature Requests:</h4>
                <ul className="list-disc list-inside space-y-1">
                  <li>Explain the problem you're trying to solve</li>
                  <li>Describe your ideal solution</li>
                  <li>Consider alternative approaches</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Feedback; 