package com.flashcards.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quizzes")
public class Quiz {
    
    @Id
    private String id;
    
    private String deckId;
    private String userId;
    private String title;
    private int totalQuestions;
    private int correctAnswers;
    private int score; // Percentage
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String status; // "in_progress", "completed", "abandoned"
    private List<QuizQuestion> questions;
    
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizQuestion {
        private String questionId;
        private String question;
        private List<String> options;
        private int correctOptionIndex;
        private int selectedOptionIndex;
        private boolean isCorrect;
        
        // Manual getters and setters
        public String getQuestionId() { return questionId; }
        public void setQuestionId(String questionId) { this.questionId = questionId; }
        
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
        
        public int getCorrectOptionIndex() { return correctOptionIndex; }
        public void setCorrectOptionIndex(int correctOptionIndex) { this.correctOptionIndex = correctOptionIndex; }
        
        public int getSelectedOptionIndex() { return selectedOptionIndex; }
        public void setSelectedOptionIndex(int selectedOptionIndex) { this.selectedOptionIndex = selectedOptionIndex; }
        
        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }
    }
    
    public Quiz(String deckId, String userId, String title, int totalQuestions) {
        this.deckId = deckId;
        this.userId = userId;
        this.title = title;
        this.totalQuestions = totalQuestions;
        this.startedAt = LocalDateTime.now();
        this.status = "in_progress";
    }
    
    // Manual getters and setters for compatibility
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getDeckId() { return deckId; }
    public void setDeckId(String deckId) { this.deckId = deckId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    
    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<QuizQuestion> getQuestions() { return questions; }
    public void setQuestions(List<QuizQuestion> questions) { this.questions = questions; }
} 