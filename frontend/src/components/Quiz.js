import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ArrowLeft, Check, X, BarChart3, RotateCcw } from 'lucide-react';
import axios from 'axios';
import toast from 'react-hot-toast';

const Quiz = () => {
  const { deckId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [quiz, setQuiz] = useState(null);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [answers, setAnswers] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitted, setSubmitted] = useState(false);
  const [showResults, setShowResults] = useState(false);

  useEffect(() => {
    startNewQuiz();
  }, [deckId]);

  const startNewQuiz = async () => {
    try {
      setLoading(true);
      const response = await axios.post(`/quiz/deck/${deckId}?questions=10`);
      setQuiz(response.data);
      setAnswers({});
      setCurrentQuestionIndex(0);
      setSubmitted(false);
      setShowResults(false);
    } catch (error) {
      console.error('Error starting quiz:', error);
      toast.error('Failed to start quiz');
    } finally {
      setLoading(false);
    }
  };

  const handleAnswerSelect = (questionId, optionIndex) => {
    if (submitted) return;
    setAnswers(prev => ({
      ...prev,
      [questionId]: optionIndex
    }));
  };

  const handleSubmitQuiz = async () => {
    try {
      const response = await axios.post(`/quiz/${quiz.id}/submit`, answers);
      setQuiz(response.data);
      setSubmitted(true);
      setShowResults(true);
      toast.success('Quiz submitted successfully!');
    } catch (error) {
      console.error('Error submitting quiz:', error);
      toast.error('Failed to submit quiz');
    }
  };

  const handleNextQuestion = () => {
    if (currentQuestionIndex < quiz.questions.length - 1) {
      setCurrentQuestionIndex(currentQuestionIndex + 1);
    }
  };

  const handlePreviousQuestion = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex(currentQuestionIndex - 1);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading quiz...</p>
        </div>
      </div>
    );
  }

  if (!quiz) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-600 mb-4">Failed to load quiz</p>
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

  if (showResults) {
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
                  <h1 className="text-3xl font-bold text-gray-900">Quiz Results</h1>
                  <p className="text-gray-600">{quiz.title}</p>
                </div>
              </div>
            </div>

            {/* Results */}
            <div className="bg-white rounded-lg shadow-lg p-8 mb-8">
              <h2 className="text-2xl font-bold text-gray-900 mb-6">Quiz Summary</h2>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
                <div className="text-center">
                  <div className="text-3xl font-bold text-indigo-600">{quiz.totalQuestions}</div>
                  <div className="text-sm text-gray-600">Total Questions</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold text-green-600">{quiz.correctAnswers}</div>
                  <div className="text-sm text-gray-600">Correct</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold text-red-600">{quiz.totalQuestions - quiz.correctAnswers}</div>
                  <div className="text-sm text-gray-600">Incorrect</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold text-blue-600">{quiz.score}%</div>
                  <div className="text-sm text-gray-600">Score</div>
                </div>
              </div>
              
              <div className="mt-8">
                <div className="flex justify-between items-center mb-2">
                  <span className="text-sm font-medium text-gray-700">Accuracy</span>
                  <span className="text-sm font-medium text-gray-700">{quiz.score}%</span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div 
                    className="bg-indigo-600 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${quiz.score}%` }}
                  ></div>
                </div>
              </div>
            </div>

            {/* Question Review */}
            <div className="bg-white rounded-lg shadow-lg p-8 mb-8">
              <h3 className="text-xl font-bold text-gray-900 mb-6">Question Review</h3>
              {quiz.questions.map((question, index) => (
                <div key={question.questionId} className="mb-6 p-4 border rounded-lg">
                  <div className="flex items-center mb-2">
                    <span className="text-sm font-medium text-gray-500">Question {index + 1}</span>
                    {question.isCorrect ? (
                      <Check className="h-4 w-4 text-green-500 ml-2" />
                    ) : (
                      <X className="h-4 w-4 text-red-500 ml-2" />
                    )}
                  </div>
                  <p className="text-gray-900 mb-3">{question.question}</p>
                  <div className="space-y-2">
                    {question.options.map((option, optionIndex) => (
                      <div
                        key={optionIndex}
                        className={`p-3 rounded-lg border ${
                          optionIndex === question.correctOptionIndex
                            ? 'bg-green-100 border-green-300'
                            : optionIndex === question.selectedOptionIndex && !question.isCorrect
                            ? 'bg-red-100 border-red-300'
                            : 'bg-gray-50 border-gray-200'
                        }`}
                      >
                        <span className="font-medium mr-2">{String.fromCharCode(65 + optionIndex)}.</span>
                        {option}
                        {optionIndex === question.correctOptionIndex && (
                          <span className="text-green-600 ml-2">✓ Correct</span>
                        )}
                        {optionIndex === question.selectedOptionIndex && !question.isCorrect && (
                          <span className="text-red-600 ml-2">✗ Your Answer</span>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>

            {/* Action Buttons */}
            <div className="flex justify-center space-x-4">
              <button
                onClick={startNewQuiz}
                className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-md flex items-center"
              >
                <RotateCcw className="h-4 w-4 mr-2" />
                Take Quiz Again
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

  const currentQuestion = quiz.questions[currentQuestionIndex];

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
                <h1 className="text-3xl font-bold text-gray-900">Quiz</h1>
                <p className="text-gray-600">{quiz.title}</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm font-medium text-gray-700">
                Question {currentQuestionIndex + 1} of {quiz.questions.length}
              </span>
            </div>
          </div>

          {/* Progress Bar */}
          <div className="mb-8">
            <div className="flex justify-between items-center mb-2">
              <span className="text-sm font-medium text-gray-700">Progress</span>
              <span className="text-sm font-medium text-gray-700">
                {Math.round(((currentQuestionIndex + 1) / quiz.questions.length) * 100)}%
              </span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div 
                className="bg-indigo-600 h-2 rounded-full transition-all duration-300"
                style={{ width: `${((currentQuestionIndex + 1) / quiz.questions.length) * 100}%` }}
              ></div>
            </div>
          </div>

          {/* Question */}
          <div className="bg-white rounded-lg shadow-lg p-8 mb-8">
            <h2 className="text-xl font-bold text-gray-900 mb-6">
              {currentQuestion.question}
            </h2>
            
            <div className="space-y-3">
              {currentQuestion.options.map((option, optionIndex) => (
                <button
                  key={optionIndex}
                  onClick={() => handleAnswerSelect(currentQuestion.questionId, optionIndex)}
                  className={`w-full p-4 text-left rounded-lg border transition-colors ${
                    answers[currentQuestion.questionId] === optionIndex
                      ? 'bg-indigo-100 border-indigo-300'
                      : 'bg-gray-50 border-gray-200 hover:bg-gray-100'
                  }`}
                >
                  <span className="font-medium mr-3">{String.fromCharCode(65 + optionIndex)}.</span>
                  {option}
                </button>
              ))}
            </div>
          </div>

          {/* Navigation */}
          <div className="flex justify-between items-center">
            <button
              onClick={handlePreviousQuestion}
              disabled={currentQuestionIndex === 0}
              className={`px-4 py-2 rounded-md ${
                currentQuestionIndex === 0
                  ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  : 'bg-gray-600 text-white hover:bg-gray-700'
              }`}
            >
              Previous
            </button>
            
            <div className="flex space-x-3">
              {currentQuestionIndex < quiz.questions.length - 1 ? (
                <button
                  onClick={handleNextQuestion}
                  className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2 rounded-md"
                >
                  Next
                </button>
              ) : (
                <button
                  onClick={handleSubmitQuiz}
                  disabled={Object.keys(answers).length < quiz.questions.length}
                  className={`px-6 py-2 rounded-md ${
                    Object.keys(answers).length < quiz.questions.length
                      ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                      : 'bg-green-600 text-white hover:bg-green-700'
                  }`}
                >
                  Submit Quiz
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Quiz; 