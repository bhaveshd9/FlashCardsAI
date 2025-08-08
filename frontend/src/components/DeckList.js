import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Plus, BookOpen, Edit, Trash2, Play } from 'lucide-react';
import axios from 'axios';
import toast from 'react-hot-toast';

const DeckList = () => {
  const [decks, setDecks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newDeck, setNewDeck] = useState({ 
    name: '', 
    description: '', 
    tags: '',
    aiText: '',
    aiNumberOfCards: 5,
    useAI: false,
    contentType: 'text'
  });
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetchDecks();
  }, []);

  const fetchDecks = async () => {
    try {
      const response = await axios.get('/decks/my');
      setDecks(response.data);
    } catch (error) {
      console.error('Error fetching decks:', error);
      toast.error('Failed to load decks');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateDeck = async (e) => {
    e.preventDefault();
    try {
      // First create the deck
      const deckResponse = await axios.post('/decks', {
        name: newDeck.name,
        description: newDeck.description,
        tags: newDeck.tags.split(',').map(tag => tag.trim()).filter(tag => tag)
      });
      
      const createdDeck = deckResponse.data;
      
      // If AI generation is enabled, generate flashcards
      if (newDeck.useAI) {
        let contentToProcess = newDeck.aiText.trim();
        
        // If file upload is selected, process the uploaded file first
        if (newDeck.contentType === 'file' && newDeck.uploadedFile) {
          try {
            console.log('Processing uploaded file:', newDeck.uploadedFile.name);
            const { processFileUpload } = await import('../services/contentService');
            const fileResult = await processFileUpload(newDeck.uploadedFile);
            
            if (fileResult.success && fileResult.data?.content) {
              contentToProcess = fileResult.data.content;
              console.log('Extracted content from file:', contentToProcess.substring(0, 200) + '...');
            } else {
              throw new Error(fileResult.error || 'Failed to extract content from file');
            }
          } catch (fileError) {
            console.error('Error processing uploaded file:', fileError);
            toast.error('Failed to process uploaded file');
            return;
          }
        }
        
        if (contentToProcess) {
          try {
            console.log('Generating AI flashcards for new deck:', {
              text: contentToProcess.substring(0, 200) + '...',
              numberOfCards: newDeck.aiNumberOfCards,
              topic: newDeck.name,
              difficulty: "medium"
            });
            
            const aiResponse = await axios.post('/ai/generate', {
              text: contentToProcess,
              numberOfCards: newDeck.aiNumberOfCards,
              topic: newDeck.name,
              difficulty: "medium",
              contentType: (newDeck.contentType === 'file' ? 'pdf' : (newDeck.contentType || 'text')),
              language: 'english'
            });
            
            console.log('AI response:', aiResponse.data);
            console.log('Number of cards requested:', newDeck.aiNumberOfCards);
            console.log('Number of cards received:', aiResponse.data ? aiResponse.data.length : 0);
            
            // Add generated flashcards to the deck
            if (aiResponse.data && Array.isArray(aiResponse.data) && aiResponse.data.length > 0) {
              let addedCount = 0;
              for (const flashcardData of aiResponse.data) {
                if (flashcardData.front && flashcardData.back) {
                  await axios.post(`/flashcards/deck/${createdDeck.id}`, {
                    front: flashcardData.front,
                    back: flashcardData.back
                  });
                  addedCount++;
                }
              }
              console.log('Added', addedCount, 'flashcards to new deck');
              toast.success(`Deck created with ${addedCount} AI-generated flashcards!`);
            } else {
              toast.success('Deck created successfully! (No flashcards generated)');
            }
          } catch (aiError) {
            console.error('Error generating AI flashcards:', aiError);
            toast.success('Deck created successfully! (AI generation failed)');
          }
        } else {
          toast.success('Deck created successfully! (No content to process)');
        }
      } else {
        toast.success('Deck created successfully!');
      }
      
      setDecks([...decks, createdDeck]);
      setShowCreateModal(false);
      setNewDeck({ 
        name: '', 
        description: '', 
        tags: '',
        aiText: '',
        aiNumberOfCards: 5,
        useAI: false,
        contentType: 'text'
      });
    } catch (error) {
      console.error('Error creating deck:', error);
      toast.error('Failed to create deck');
    }
  };

  const handleDeleteDeck = async (deckId) => {
    if (window.confirm('Are you sure you want to delete this deck?')) {
      try {
        await axios.delete(`/decks/${deckId}`);
        setDecks(decks.filter(deck => deck.id !== deckId));
        toast.success('Deck deleted successfully!');
      } catch (error) {
        console.error('Error deleting deck:', error);
        toast.error('Failed to delete deck');
      }
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading decks...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="flex justify-between items-center mb-8">
            <h1 className="text-3xl font-bold text-gray-900">My Decks</h1>
            <button
              onClick={() => setShowCreateModal(true)}
              className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-md flex items-center"
            >
              <Plus className="h-5 w-5 mr-2" />
              Create New Deck
            </button>
          </div>

          {decks.length === 0 ? (
            <div className="text-center py-12">
              <BookOpen className="h-16 w-16 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">No decks yet</h3>
              <p className="text-gray-500 mb-6">Create your first deck to get started!</p>
              <button
                onClick={() => setShowCreateModal(true)}
                className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-md"
              >
                Create Your First Deck
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {decks.map((deck) => (
                <div key={deck.id} className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow">
                  <div className="p-6">
                    <div className="flex justify-between items-start mb-4">
                      <h3 className="text-lg font-semibold text-gray-900">{deck.name}</h3>
                      <div className="flex space-x-2">
                        <button
                          onClick={() => navigate(`/decks/${deck.id}`)}
                          className="text-indigo-600 hover:text-indigo-500"
                        >
                          <Edit className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => handleDeleteDeck(deck.id)}
                          className="text-red-600 hover:text-red-500"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </div>
                    <p className="text-gray-600 mb-4">{deck.description}</p>
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-500">
                        {deck.flashcardCount || 0} cards
                      </span>
                      <button
                        onClick={() => navigate(`/study/${deck.id}`)}
                        className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded-md text-sm flex items-center"
                      >
                        <Play className="h-3 w-3 mr-1" />
                        Study
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Create Deck Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-[500px] shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Create New Deck</h3>
              <form onSubmit={handleCreateDeck}>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Title
                  </label>
                  <input
                    type="text"
                    required
                    value={newDeck.name}
                    onChange={(e) => setNewDeck({...newDeck, name: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    placeholder="Enter deck title"
                  />
                </div>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Description
                  </label>
                  <textarea
                    value={newDeck.description}
                    onChange={(e) => setNewDeck({...newDeck, description: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    rows="3"
                    placeholder="Enter deck description"
                  />
                </div>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Tags (comma-separated)
                  </label>
                  <input
                    type="text"
                    value={newDeck.tags}
                    onChange={(e) => setNewDeck({...newDeck, tags: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    placeholder="e.g., math, science, history"
                  />
                </div>
                
                {/* AI Generation Section */}
                <div className="mb-6 p-4 border border-gray-200 rounded-lg">
                  <div className="flex items-center mb-3">
                    <input
                      type="checkbox"
                      id="useAI"
                      checked={newDeck.useAI}
                      onChange={(e) => setNewDeck({...newDeck, useAI: e.target.checked})}
                      className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                    />
                    <label htmlFor="useAI" className="ml-2 text-sm font-medium text-gray-700">
                      Generate flashcards with AI
                    </label>
                  </div>
                  
                  {newDeck.useAI && (
                    <div className="space-y-3">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Content Source</label>
                        <select
                          value={newDeck.contentType || 'text'}
                          onChange={(e) => setNewDeck({...newDeck, contentType: e.target.value})}
                          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                        >
                          <option value="text">Text Input</option>
                          <option value="url">URL/Website</option>
                          <option value="file">Upload Document (PDF/TXT)</option>
                        </select>
                      </div>
                      
                      {newDeck.contentType === 'text' && (
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2">Content for AI Generation</label>
                          <textarea
                            value={newDeck.aiText}
                            onChange={(e) => setNewDeck({...newDeck, aiText: e.target.value})}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                            rows="4"
                            placeholder="Enter text content or topic (e.g., 'car brands', 'JavaScript basics', or paste a paragraph)"
                          />
                        </div>
                      )}
                      
                      {newDeck.contentType === 'url' && (
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2">Website URL</label>
                          <input
                            type="url"
                            value={newDeck.aiText}
                            onChange={(e) => setNewDeck({...newDeck, aiText: e.target.value})}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                            placeholder="https://example.com/article"
                          />
                          <p className="text-xs text-gray-500 mt-1">Enter a URL to extract content from</p>
                        </div>
                      )}
                      
                      {newDeck.contentType === 'file' && (
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2">Upload Document</label>
                          <input
                            type="file"
                            accept=".pdf,.txt,.doc,.docx"
                            onChange={(e) => {
                              const file = e.target.files[0];
                              if (file) {
                                setNewDeck({...newDeck, uploadedFile: file});
                              }
                            }}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                          />
                          <p className="text-xs text-gray-500 mt-1">Supported formats: PDF, TXT, DOC, DOCX</p>
                        </div>
                      )}
                      
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Number of Cards</label>
                        <input
                          type="number"
                          min="1"
                          max="20"
                          value={newDeck.aiNumberOfCards}
                          onChange={(e) => setNewDeck({...newDeck, aiNumberOfCards: parseInt(e.target.value) || 5})}
                          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                          placeholder="5"
                        />
                      </div>
                    </div>
                  )}
                </div>
                
                <div className="flex justify-end space-x-3">
                  <button
                    type="button"
                    onClick={() => setShowCreateModal(false)}
                    className="px-4 py-2 text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
                  >
                    Create Deck
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DeckList; 