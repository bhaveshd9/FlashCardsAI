package com.flashcards.service;

import com.flashcards.model.Flashcard;
import com.flashcards.model.Quiz;
import com.flashcards.repository.FlashcardRepository;
import com.flashcards.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final FlashcardRepository flashcardRepository;

    public QuizService(QuizRepository quizRepository, FlashcardRepository flashcardRepository) {
        this.quizRepository = quizRepository;
        this.flashcardRepository = flashcardRepository;
    }

    public Quiz createQuiz(String deckId, String userId, int numberOfQuestions) {
        // Get flashcards from the deck
        List<Flashcard> flashcards = flashcardRepository.findByDeckIdOrderByOrderIndex(deckId);
        
        if (flashcards.isEmpty()) {
            throw new RuntimeException("No flashcards available for quiz");
        }

        // Shuffle flashcards and take the requested number
        Collections.shuffle(flashcards);
        int questionsToUse = Math.min(numberOfQuestions, flashcards.size());
        List<Flashcard> selectedCards = flashcards.subList(0, questionsToUse);

        // Create quiz questions
        List<Quiz.QuizQuestion> quizQuestions = new ArrayList<>();
        for (Flashcard card : selectedCards) {
            Quiz.QuizQuestion question = createQuizQuestion(card, flashcards);
            quizQuestions.add(question);
        }

        // Create quiz
        Quiz quiz = new Quiz(deckId, userId, "Quiz on " + deckId, questionsToUse);
        quiz.setQuestions(quizQuestions);
        
        return quizRepository.save(quiz);
    }

    private Quiz.QuizQuestion createQuizQuestion(Flashcard correctCard, List<Flashcard> allCards) {
        // Create 4 options: 1 correct + 3 random incorrect
        List<String> options = new ArrayList<>();
        options.add(correctCard.getBack()); // Correct answer
        
        // Get 3 random incorrect answers
        List<String> incorrectAnswers = allCards.stream()
                .filter(card -> !card.getId().equals(correctCard.getId()))
                .map(Flashcard::getBack)
                .distinct()
                .collect(Collectors.toList());
        
        Collections.shuffle(incorrectAnswers);
        for (int i = 0; i < Math.min(3, incorrectAnswers.size()); i++) {
            options.add(incorrectAnswers.get(i));
        }
        
        // If we don't have enough incorrect answers, add generic ones
        while (options.size() < 4) {
            options.add("None of the above");
        }
        
        // Shuffle options
        Collections.shuffle(options);
        
        // Find the index of the correct answer
        int correctIndex = options.indexOf(correctCard.getBack());
        
        Quiz.QuizQuestion question = new Quiz.QuizQuestion();
        question.setQuestionId(correctCard.getId());
        question.setQuestion(correctCard.getFront());
        question.setOptions(options);
        question.setCorrectOptionIndex(correctIndex);
        question.setSelectedOptionIndex(-1);
        question.setCorrect(false);
        
        return question;
    }

    public Quiz submitQuiz(String quizId, Map<String, Integer> answers) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        int correctCount = 0;
        
        // Process answers
        for (Quiz.QuizQuestion question : quiz.getQuestions()) {
            Integer selectedIndex = answers.get(question.getQuestionId());
            if (selectedIndex != null) {
                question.setSelectedOptionIndex(selectedIndex);
                question.setCorrect(selectedIndex == question.getCorrectOptionIndex());
                if (question.isCorrect()) {
                    correctCount++;
                }
            }
        }
        
        // Calculate score
        quiz.setCorrectAnswers(correctCount);
        quiz.setScore((int) Math.round((double) correctCount / quiz.getTotalQuestions() * 100));
        quiz.setCompletedAt(LocalDateTime.now());
        quiz.setStatus("completed");
        
        return quizRepository.save(quiz);
    }

    public List<Quiz> getUserQuizHistory(String userId) {
        return quizRepository.findByUserIdOrderByStartedAtDesc(userId);
    }

    public List<Quiz> getDeckQuizHistory(String deckId, String userId) {
        return quizRepository.findByDeckIdAndUserIdOrderByStartedAtDesc(deckId, userId);
    }

    public Quiz getQuiz(String quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
    }

    public void deleteQuiz(String quizId, String userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        if (!quiz.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this quiz");
        }
        
        quizRepository.delete(quiz);
    }
} 