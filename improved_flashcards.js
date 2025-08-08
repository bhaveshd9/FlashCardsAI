const axios = require('axios');
const fs = require('fs');
const path = require('path');

// Configuration
const API_BASE_URL = 'http://localhost:8080/api';
const TEST_DECK_ID = '68959df7b1d3eb022811a25a';
const TEST_USER_EMAIL = 'bhaveshdewan9@gmail.com';
const TEST_PASSWORD = 'carcraftr123';

// Better structured flashcards
const FLASHCARDS = [
  {
    question: "What is an agent in the context of AI?",
    answer: "An agent is a system that independently accomplishes tasks on your behalf with a high degree of independence."
  },
  {
    question: "What are the four key characteristics of an agent?",
    answer: "1. Autonomy - operates without direct human intervention\n2. Reactivity - perceives and responds to changes in the environment\n3. Proactiveness - takes initiative when appropriate\n4. Social ability - interacts with other agents/humans when needed"
  },
  {
    question: "What are the main components of an agent's architecture?",
    answer: "1. Perception - observes the environment\n2. Processing - makes decisions based on observations\n3. Action - performs actions to achieve goals\n4. Learning - improves over time based on experience"
  },
  {
    question: "What is the purpose of the practical guide to building agents?",
    answer: "To provide product and engineering teams with frameworks, patterns, and best practices for building their first agents, based on insights from customer deployments."
  },
  {
    question: "What capabilities of large language models have enabled the development of agents?",
    answer: "Advances in reasoning, multimodality, and tool use have unlocked the development of agents that can handle complex, multi-step tasks."
  }
];

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
    
    // 2. Add flashcards to deck
    const axiosInstance = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    console.log(`Adding ${FLASHCARDS.length} flashcards to deck ${TEST_DECK_ID}...`);
    
    for (const flashcard of FLASHCARDS) {
      try {
        await axiosInstance.post(`/flashcards/deck/${TEST_DECK_ID}`, {
          question: flashcard.question,
          answer: flashcard.answer,
          difficulty: 'medium',
          tags: ['ai', 'agents', 'study']
        });
        console.log(`Added: ${flashcard.question.substring(0, 50)}${flashcard.question.length > 50 ? '...' : ''}`);
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
