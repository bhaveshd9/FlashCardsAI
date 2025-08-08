import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ArrowLeft, RotateCcw, Check, X, Forward, BarChart3, Shuffle, Play, Pause } from 'lucide-react';
import axios from 'axios';
import toast from 'react-hot-toast';

const StudyMode = () => {
  const { deckId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [deck, setDeck] = useState(null);
  const [flashcards, setFlashcards] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);
  const [loading, setLoading] = useState(true);
  const [studyMode, setStudyMode] = useState('due'); // 'due', 'all', 'new'
  const [isShuffled, setIsShuffled] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const [showStats, setShowStats] = useState(false);
  const [stats, setStats] = useState({
    total: 0,
    correct: 0,
    incorrect: 0,
    skipped: 0,
    completion: 0
  });

  useEffect(() => {
    fetchStudyData();
  }, [deckId]);

  const fetchStudyData = async () => {
    try {
      const [deckResponse, flashcardsResponse] = await Promise.all([
        axios.get(`/decks/${deckId}`),
        axios.get(`/flashcards/deck/${deckId}`)
      ]);
      
      setDeck(deckResponse.data);
      setFlashcards(flashcardsResponse.data);
      setStats(prev => ({ ...prev, total: flashcardsResponse.data.length }));
    } catch (error) {
      console.error('Error fetching study data:', error);
      toast.error('Failed to load study data');
    } finally {
      setLoading(false);
    }
  };

  const getStudyCards = () => {
    let cards = [...flashcards];
    
    if (studyMode === 'due') {
      // Filter cards that are due for review (simplified logic)
      cards = cards.filter((_, index) => index % 3 === 0); // Every 3rd card for demo
    } else if (studyMode === 'new') {
      // Filter new cards (simplified logic)
      cards = cards.filter((_, index) => index % 2 === 0); // Every 2nd card for demo
    }
    
    if (isShuffled) {
      cards = [...cards].sort(() => Math.random() - 0.5);
    }
    
    return cards;
  };

  const studyCards = getStudyCards();

  const handleFlip = () => {
    setIsFlipped(!isFlipped);
  };

  const handleGrade = async (score) => {
    if (currentIndex >= studyCards.length) return;

    const currentCard = studyCards[currentIndex];
    
    try {
      // Record study session (just mark as viewed)
      await axios.post('/study/session', {
        flashcardId: currentCard.id,
        deckId: deckId,
        score: 3 // Default score for review mode
      });

      // Update stats
      setStats(prev => ({
        ...prev,
        correct: prev.correct + 1, // Count as correct in review mode
        completion: Math.round(((currentIndex + 1) / studyCards.length) * 100)
      }));

      // Move to next card
      if (currentIndex < studyCards.length - 1) {
        setCurrentIndex(currentIndex + 1);
        setIsFlipped(false);
      } else {
        // Study session completed
        setShowStats(true);
      }
    } catch (error) {
      console.error('Error recording study session:', error);
    }
  };

  const handleSkip = () => {
    setStats(prev => ({
      ...prev,
      skipped: prev.skipped + 1
    }));

    if (currentIndex < studyCards.length - 1) {
      setCurrentIndex(currentIndex + 1);
      setIsFlipped(false);
    } else {
      toast.success('Study session completed!');
      setShowStats(true);
    }
  };

  const handleShuffle = () => {
    setIsShuffled(!isShuffled);
    setCurrentIndex(0);
    setIsFlipped(false);
  };

  const handleRestart = () => {
    setCurrentIndex(0);
    setIsFlipped(false);
    setStats({
      total: studyCards.length,
      correct: 0,
      incorrect: 0,
      skipped: 0,
      completion: 0
    });
    setShowStats(false);
  };

  const getScoreLabel = (score) => {
    switch (score) {
      case 1: return 'Again';
      case 2: return 'Hard';
      case 3: return 'Good';
      case 4: return 'Easy';
      case 5: return 'Perfect';
      default: return '';
    }
  };

  const getScoreColor = (score) => {
    switch (score) {
      case 1: return 'bg-red-500 hover:bg-red-600';
      case 2: return 'bg-orange-500 hover:bg-orange-600';
      case 3: return 'bg-yellow-500 hover:bg-yellow-600';
      case 4: return 'bg-green-500 hover:bg-green-600';
      case 5: return 'bg-blue-500 hover:bg-blue-600';
      default: return 'bg-gray-500 hover:bg-gray-600';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading study session...</p>
        </div>
      </div>
    );
  }

  if (!deck || studyCards.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-600 mb-4">No cards available for study</p>
          <button 
            onClick={() => navigate(`/decks/${deckId}`)}
            className="text-indigo-600 hover:text-indigo-500"
          >
            Back to Deck
          </button>
        </div>
      </div>
    );
  }

  if (showStats) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
          <div className="px-4 py-6 sm:px-0">
            {/* Header */}
            <div className="flex items-center justify-between mb-8">
              <div className="flex items-center space-x-4">
                <button
                  onClick={() => navigate(`/decks/${deckId}`)}
                  className="text-gray-600 hover:text-gray-900"
                >
                  <ArrowLeft className="h-6 w-6" />
                </button>
                <div>
                  <h1 className="text-3xl font-bold text-gray-900">Study Complete!</h1>
                  <p className="text-gray-600">{deck.title}</p>
                </div>
              </div>
            </div>

            {/* Stats */}
            <div className="bg-white rounded-lg shadow-lg p-8 mb-8">
              <h2 className="text-2xl font-bold text-gray-900 mb-6">Session Results</h2>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
                <div className="text-center">
                  <div className="text-3xl font-bold text-indigo-600">{stats.total}</div>
                  <div className="text-sm text-gray-600">Total Cards</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold text-green-600">{stats.correct}</div>
                  <div className="text-sm text-gray-600">Correct</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold text-red-600">{stats.incorrect}</div>
                  <div className="text-sm text-gray-600">Incorrect</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold text-yellow-600">{stats.skipped}</div>
                  <div className="text-sm text-gray-600">Skipped</div>
                </div>
              </div>
              
              <div className="mt-8">
                <div className="flex justify-between items-center mb-2">
                  <span className="text-sm font-medium text-gray-700">Completion</span>
                  <span className="text-sm font-medium text-gray-700">{stats.completion}%</span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div 
                    className="bg-indigo-600 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${stats.completion}%` }}
                  ></div>
                </div>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex justify-center space-x-4">
              <button
                onClick={handleRestart}
                className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-md flex items-center"
              >
                <RotateCcw className="h-4 w-4 mr-2" />
                Study Again
              </button>
              <button
                onClick={() => navigate(`/decks/${deckId}`)}
                className="bg-gray-600 hover:bg-gray-700 text-white px-6 py-3 rounded-md"
              >
                Back to Deck
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const currentCard = studyCards[currentIndex];

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center space-x-4">
              <button
                onClick={() => navigate(`/decks/${deckId}`)}
                className="text-gray-600 hover:text-gray-900"
              >
                <ArrowLeft className="h-6 w-6" />
              </button>
              <div>
                <h1 className="text-3xl font-bold text-gray-900">{deck.title}</h1>
                <p className="text-gray-600">Study Mode</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <button
                onClick={() => setShowStats(true)}
                className="text-gray-600 hover:text-gray-900"
              >
                <BarChart3 className="h-6 w-6" />
              </button>
              <button
                onClick={handleShuffle}
                className={`px-3 py-2 rounded-md flex items-center ${
                  isShuffled 
                    ? 'bg-indigo-600 text-white' 
                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                <Shuffle className="h-4 w-4 mr-2" />
                Shuffle
              </button>
            </div>
          </div>

          {/* Progress Bar */}
          <div className="mb-8">
            <div className="flex justify-between items-center mb-2">
              <span className="text-sm font-medium text-gray-700">
                Card {currentIndex + 1} of {studyCards.length}
              </span>
              <span className="text-sm font-medium text-gray-700">
                {Math.round(((currentIndex + 1) / studyCards.length) * 100)}%
              </span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div 
                className="bg-indigo-600 h-2 rounded-full transition-all duration-300"
                style={{ width: `${((currentIndex + 1) / studyCards.length) * 100}%` }}
              ></div>
            </div>
          </div>

          {/* Flashcard */}
          <div className="mb-8">
            <div 
              className="bg-white rounded-lg shadow-lg p-8 cursor-pointer transition-all duration-300 hover:shadow-xl"
              onClick={handleFlip}
              style={{ 
                minHeight: '300px'
              }}
            >
              <div className="text-center">
                <div className="text-sm text-gray-500 mb-4">
                  {isFlipped ? 'Answer' : 'Question'}
                </div>
                <div className="text-2xl font-medium text-gray-900 mb-4">
                  {isFlipped ? currentCard.back : currentCard.front}
                </div>
                <div className="text-sm text-gray-600">
                  Click to {isFlipped ? 'hide' : 'reveal'} answer
                </div>
              </div>
            </div>
          </div>

          {/* Study Controls */}
          {isFlipped ? (
            <div className="space-y-4">
              <div className="text-center mb-6">
                <h3 className="text-lg font-medium text-gray-900 mb-2">Review Complete</h3>
                <p className="text-sm text-gray-600">Click Next to continue or Previous to review again</p>
              </div>
              
              <div className="flex justify-center space-x-4">
                <button
                  onClick={() => {
                    setIsFlipped(false);
                    if (currentIndex > 0) {
                      setCurrentIndex(currentIndex - 1);
                    }
                  }}
                  disabled={currentIndex === 0}
                  className={`px-6 py-3 rounded-md flex items-center ${
                    currentIndex === 0
                      ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                      : 'bg-gray-600 hover:bg-gray-700 text-white'
                  }`}
                >
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Previous
                </button>
                
                <button
                  onClick={() => handleGrade(3)}
                  className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-md flex items-center"
                >
                  Next
                  <Forward className="h-4 w-4 ml-2" />
                </button>
              </div>
            </div>
          ) : (
            <div className="text-center">
              <button
                onClick={handleFlip}
                className="bg-indigo-600 hover:bg-indigo-700 text-white px-8 py-3 rounded-md font-medium"
              >
                Reveal Answer
              </button>
            </div>
          )}

          {/* Quick Stats */}
          <div className="mt-8 bg-white rounded-lg shadow p-4">
            <div className="grid grid-cols-3 gap-4 text-center">
              <div>
                <div className="text-lg font-bold text-green-600">{stats.correct}</div>
                <div className="text-sm text-gray-600">Correct</div>
              </div>
              <div>
                <div className="text-lg font-bold text-red-600">{stats.incorrect}</div>
                <div className="text-sm text-gray-600">Incorrect</div>
              </div>
              <div>
                <div className="text-lg font-bold text-yellow-600">{stats.skipped}</div>
                <div className="text-sm text-gray-600">Skipped</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StudyMode; 