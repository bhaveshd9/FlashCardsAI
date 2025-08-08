import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Plus, Edit, Trash2, Play, Settings, Sparkles, ArrowLeft, Save, X, HelpCircle, FileText, Link as LinkIcon } from 'lucide-react';
import axios from 'axios';
import toast from 'react-hot-toast';
import AIContentModal from './AIContentModal';

const DeckDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [deck, setDeck] = useState(null);
  const [flashcards, setFlashcards] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [showAIModal, setShowAIModal] = useState(false);
  const [isGenerating, setIsGenerating] = useState(false);
  const [editingFlashcard, setEditingFlashcard] = useState(null);
  
  // Form states
  const [newFlashcard, setNewFlashcard] = useState({ front: '', back: '' });
  const [deckSettings, setDeckSettings] = useState({ title: '', description: '', tags: '', isPublic: false });

  useEffect(() => {
    fetchDeckData();
  }, [id]);

  const fetchDeckData = async () => {
    console.log('Starting to fetch deck data for deck ID:', id);
    try {
      console.log('Making API requests to fetch deck and flashcards...');
      const [deckResponse, flashcardsResponse] = await Promise.all([
        axios.get(`/decks/${id}`),
        axios.get(`/flashcards/deck/${id}`)
      ]);
      
      console.log('Deck response:', deckResponse.data);
      console.log('Flashcards response:', flashcardsResponse.data);
      
      setDeck(deckResponse.data);
      setFlashcards(flashcardsResponse.data);
      setDeckSettings({
        title: deckResponse.data.name || '',
        description: deckResponse.data.description || '',
        tags: deckResponse.data.tags?.join(', ') || '',
        isPublic: deckResponse.data.isPublic || false
      });
      
      console.log('Deck and flashcards state updated');
    } catch (error) {
      console.error('Error fetching deck data:', error);
      console.error('Error response:', error.response?.data);
      console.error('Error status:', error.response?.status);
      toast.error('Failed to load deck');
    } finally {
      console.log('Finished loading deck data');
      setLoading(false);
    }
  };

  const createFlashcard = useCallback(async (front, back) => {
    try {
      const response = await axios.post(`/flashcards/deck/${id}`, { front, back });
      setFlashcards(prev => [...prev, response.data]);
      return true;
    } catch (error) {
      console.error('Error creating flashcard:', error);
      throw new Error('Failed to create flashcard');
    }
  }, [id]);

  const handleCreateFlashcard = async (e) => {
    e.preventDefault();
    try {
      await createFlashcard(newFlashcard.front, newFlashcard.back);
      setShowAddModal(false);
      setNewFlashcard({ front: '', back: '' });
      toast.success('Flashcard created successfully!');
    } catch (error) {
      console.error('Error creating flashcard:', error);
      toast.error('Failed to create flashcard');
    }
  };

  const handleAIContentExtracted = async (content, contentType = 'text') => {
    try {
      setIsGenerating(true);
      
      // Call the AI service to generate flashcards from the content
      const response = await axios.post('/ai/generate', {
        text: content,
        contentType,
        topic: deck?.name || 'General',
        numberOfCards: 12,
        difficulty: 'medium',
        language: 'english'
      });
      
      // Add each generated flashcard
      const generatedFlashcards = Array.isArray(response.data) ? response.data : (response.data?.flashcards || []);
      let successCount = 0;
      
      for (const flashcard of generatedFlashcards) {
        try {
          await createFlashcard(flashcard.front, flashcard.back);
          successCount++;
        } catch (err) {
          console.error('Error saving generated flashcard:', err);
        }
      }
      
      if (successCount > 0) {
        toast.success(`Successfully generated ${successCount} flashcards!`);
      } else {
        toast.error('Failed to generate any flashcards');
      }
      
    } catch (error) {
      console.error('Error generating flashcards:', error);
      toast.error('Failed to generate flashcards from content');
    } finally {
      setIsGenerating(false);
    }
  };

  const handleUpdateFlashcard = async (e) => {
    e.preventDefault();
    if (!editingFlashcard) return;
    
    try {
      const response = await axios.put(`/flashcards/${editingFlashcard.id}`, editingFlashcard);
      setFlashcards(flashcards.map(fc => fc.id === editingFlashcard.id ? response.data : fc));
      setShowEditModal(false);
      setEditingFlashcard(null);
      toast.success('Flashcard updated successfully!');
    } catch (error) {
      console.error('Error updating flashcard:', error);
      toast.error('Failed to update flashcard');
    }
  };

  const handleDeleteFlashcard = async (flashcardId) => {
    if (window.confirm('Are you sure you want to delete this flashcard?')) {
      try {
        await axios.delete(`/flashcards/${flashcardId}`);
        setFlashcards(flashcards.filter(fc => fc.id !== flashcardId));
        toast.success('Flashcard deleted successfully!');
      } catch (error) {
        console.error('Error deleting flashcard:', error);
        toast.error('Failed to delete flashcard');
      }
    }
  };

  const handleUpdateDeck = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.put(`/decks/${id}`, {
        name: deckSettings.title,
        description: deckSettings.description,
        tags: deckSettings.tags.split(',').map(tag => tag.trim()).filter(tag => tag),
        isPublic: deckSettings.isPublic
      });
      
      setDeck(response.data);
      setShowSettingsModal(false);
      toast.success('Deck updated successfully!');
    } catch (error) {
      console.error('Error updating deck:', error);
      toast.error('Failed to update deck');
    }
  };

  const handleDeleteDeck = async () => {
    if (window.confirm('Are you sure you want to delete this deck? This action cannot be undone.')) {
      try {
        await axios.delete(`/decks/${id}`);
        toast.success('Deck deleted successfully!');
        navigate('/decks');
      } catch (error) {
        console.error('Error deleting deck:', error);
        toast.error('Failed to delete deck');
      }
    }
  };

  // Log current state on render
  console.log('Render - Loading:', loading);
  console.log('Current deck:', deck);
  console.log('Current flashcards:', flashcards);
  console.log('Flashcards count:', flashcards.length);

  if (loading) {
    console.log('Rendering loading state');
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading deck...</p>
        </div>
      </div>
    );
  }

  if (!deck) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-600">Deck not found</p>
          <button onClick={() => navigate('/decks')} className="mt-4 text-indigo-600 hover:text-indigo-500">
            Back to Decks
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center space-x-4">
              <button
                onClick={() => navigate('/decks')}
                className="text-gray-600 hover:text-gray-900"
              >
                <ArrowLeft className="h-6 w-6" />
              </button>
              <div>
                <h1 className="text-3xl font-bold text-gray-900">{deck.name}</h1>
                <p className="text-gray-600">{deck.description}</p>
              </div>
            </div>
            <div className="flex space-x-3">
              <button
                onClick={() => setShowSettingsModal(true)}
                className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-md flex items-center"
              >
                <Settings className="h-4 w-4 mr-2" />
                Settings
              </button>
              <button
                onClick={() => navigate(`/study/${id}`)}
                className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-md flex items-center"
              >
                <Play className="h-4 w-4 mr-2" />
                Study
              </button>
              <button
                onClick={() => navigate(`/quiz/${id}`)}
                className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-md flex items-center"
              >
                <HelpCircle className="h-4 w-4 mr-2" />
                Quiz
              </button>
            </div>
          </div>

          {/* Stats */}
          <div className="bg-white rounded-lg shadow p-6 mb-8">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-indigo-600">{flashcards.length}</div>
                <div className="text-sm text-gray-600">Total Cards</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-green-600">0%</div>
                <div className="text-sm text-gray-600">Completion</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-blue-600">0</div>
                <div className="text-sm text-gray-600">Studied Today</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-purple-600">0</div>
                <div className="text-sm text-gray-600">Due Cards</div>
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-semibold text-gray-900">Flashcards</h2>
                         <div className="flex space-x-3">
               <button
                 onClick={() => setShowAddModal(true)}
                 className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-md flex items-center"
               >
                 <Plus className="h-4 w-4 mr-2" />
                 Add Card
               </button>
             </div>
          </div>

          {/* Flashcards List */}
          {console.log('Rendering flashcards list. Count:', flashcards.length) || 
           flashcards.length === 0 ? (
            <div className="text-center py-12 bg-white rounded-lg shadow">
              <p className="text-gray-500 mb-4">No flashcards yet. Add your first card!</p>
              <button
                onClick={() => setShowAddModal(true)}
                className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-md"
              >
                Add First Card
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {flashcards.map((flashcard, index) => (
                <div key={flashcard.id} className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow">
                  <div className="p-6">
                    <div className="flex justify-between items-start mb-4">
                      <span className="text-sm text-gray-500">#{index + 1}</span>
                      <div className="flex space-x-2">
                        <button
                          onClick={() => {
                            setEditingFlashcard(flashcard);
                            setShowEditModal(true);
                          }}
                          className="text-indigo-600 hover:text-indigo-500"
                        >
                          <Edit className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => handleDeleteFlashcard(flashcard.id)}
                          className="text-red-600 hover:text-red-500"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </div>
                    <div className="space-y-3">
                      <div>
                        <h3 className="text-sm font-medium text-gray-700 mb-1">Front</h3>
                        <p className="text-gray-900">{flashcard.front}</p>
                      </div>
                      <div>
                        <h3 className="text-sm font-medium text-gray-700 mb-1">Back</h3>
                        <p className="text-gray-900">{flashcard.back}</p>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Add Flashcard Modal */}
      {showAddModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Add New Flashcard</h3>
              <form onSubmit={handleCreateFlashcard}>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">Front</label>
                  <textarea
                    required
                    value={newFlashcard.front}
                    onChange={(e) => setNewFlashcard({...newFlashcard, front: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    rows="3"
                    placeholder="Enter the question or prompt"
                  />
                </div>
                <div className="mb-6">
                  <label className="block text-sm font-medium text-gray-700 mb-2">Back</label>
                  <textarea
                    required
                    value={newFlashcard.back}
                    onChange={(e) => setNewFlashcard({...newFlashcard, back: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    rows="3"
                    placeholder="Enter the answer"
                  />
                </div>
                <div className="flex justify-end space-x-3">
                  <button
                    type="button"
                    onClick={() => setShowAddModal(false)}
                    className="px-4 py-2 text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
                  >
                    Add Card
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Edit Flashcard Modal */}
      {showEditModal && editingFlashcard && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Edit Flashcard</h3>
              <form onSubmit={handleUpdateFlashcard}>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">Front</label>
                  <textarea
                    required
                    value={editingFlashcard.front}
                    onChange={(e) => setEditingFlashcard({...editingFlashcard, front: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    rows="3"
                  />
                </div>
                <div className="mb-6">
                  <label className="block text-sm font-medium text-gray-700 mb-2">Back</label>
                  <textarea
                    required
                    value={editingFlashcard.back}
                    onChange={(e) => setEditingFlashcard({...editingFlashcard, back: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    rows="3"
                  />
                </div>
                <div className="flex justify-end space-x-3">
                  <button
                    type="button"
                    onClick={() => setShowEditModal(false)}
                    className="px-4 py-2 text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
                  >
                    Save Changes
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Deck Settings Modal */}
      {showSettingsModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Deck Settings</h3>
              <form onSubmit={handleUpdateDeck}>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">Title</label>
                  <input
                    type="text"
                    required
                    value={deckSettings.title}
                    onChange={(e) => setDeckSettings({...deckSettings, title: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">Description</label>
                  <textarea
                    value={deckSettings.description}
                    onChange={(e) => setDeckSettings({...deckSettings, description: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    rows="3"
                  />
                </div>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">Tags (comma-separated)</label>
                  <input
                    type="text"
                    value={deckSettings.tags}
                    onChange={(e) => setDeckSettings({...deckSettings, tags: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    placeholder="e.g., math, science, history"
                  />
                </div>
                <div className="mb-6">
                  <label className="flex items-center">
                    <input
                      type="checkbox"
                      checked={deckSettings.isPublic}
                      onChange={(e) => setDeckSettings({...deckSettings, isPublic: e.target.checked})}
                      className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                    />
                    <span className="ml-2 text-sm text-gray-700">Make deck public</span>
                  </label>
                </div>
                <div className="flex justify-between">
                  <button
                    type="button"
                    onClick={handleDeleteDeck}
                    className="px-4 py-2 text-red-700 bg-red-100 rounded-md hover:bg-red-200"
                  >
                    Delete Deck
                  </button>
                  <div className="flex space-x-3">
                    <button
                      type="button"
                      onClick={() => setShowSettingsModal(false)}
                      className="px-4 py-2 text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
                    >
                      Save Changes
                    </button>
                  </div>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DeckDetail; 