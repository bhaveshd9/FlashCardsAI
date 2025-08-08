const axios = require('axios');
const fs = require('fs');
const path = require('path');

// Configuration
const API_BASE_URL = 'http://localhost:8080/api';
const TEST_DECK_ID = '68959df7b1d3eb022811a25a'; // Your deck ID
const TEST_USER_EMAIL = 'bhaveshdewan9@gmail.com';
const TEST_PASSWORD = 'carcraftr123';

// The content to create flashcards from
const CONTENT = `A practical guide to building agents

What is an agent?
An agent is a system that independently accomplishes tasks on your behalf.

What are the key characteristics of an agent?
1. Autonomy - operates without direct human intervention
2. Reactivity - perceives and responds to changes in the environment
3. Proactiveness - takes initiative when appropriate
4. Social ability - interacts with other agents/humans when needed

What are the main components of an agent architecture?
1. Perception - observes the environment
2. Processing - makes decisions based on observations
3. Action - performs actions to achieve goals
4. Learning - improves over time based on experience`;

async function createFlashcards() {
  try {
    // 1. Login to get JWT token
    console.log('Logging in...');
    const loginResponse = await axios.post(`${API_BASE_URL}/auth/login`, {
      email: TEST_USER_EMAIL,
      password: TEST_PASSWORD
    });
    
    const token = loginResponse.data.token;
    console.log('Login successful, token received');
    
    // 2. Create flashcards directly from content
    console.log('Creating flashcards from content...');
    const lines = CONTENT.split('\n').filter(line => line.trim() !== '');
    
    const flashcards = [];
    let currentQuestion = null;
    
    for (const line of lines) {
      if (line.endsWith('?')) {
        // This looks like a question
        if (currentQuestion) {
          flashcards.push(currentQuestion);
        }
        currentQuestion = { question: line, answer: '' };
      } else if (currentQuestion) {
        // Add to current answer
        currentQuestion.answer += (currentQuestion.answer ? '\n' : '') + line;
      }
    }
    
    if (currentQuestion) {
      flashcards.push(currentQuestion);
    }
    
    console.log(`Created ${flashcards.length} flashcards`);
    
    // 3. Add flashcards to deck
    const axiosInstance = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    console.log(`Adding ${flashcards.length} flashcards to deck ${TEST_DECK_ID}...`);
    
    for (const flashcard of flashcards) {
      try {
        await axiosInstance.post(`/flashcards/deck/${TEST_DECK_ID}`, {
          question: flashcard.question,
          answer: flashcard.answer,
          difficulty: 'medium',
          tags: ['auto-generated']
        });
        console.log(`Added: ${flashcard.question.substring(0, 50)}...`);
      } catch (error) {
        console.error('Error adding flashcard:', error.response?.data || error.message);
      }
    }
    
    console.log('Test completed successfully!');
    
  } catch (error) {
    console.error('Test failed:');
    console.error('Error:', error.response?.data || error.message);
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Headers:', error.response.headers);
    }
  }
}

createFlashcards();