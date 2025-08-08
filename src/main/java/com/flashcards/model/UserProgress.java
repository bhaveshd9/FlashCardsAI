package com.flashcards.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_progress")
public class UserProgress {
    
    @Id
    private String id;
    
    private String userId;
    private String flashcardId;
    private String deckId;
    
    // Spaced repetition data
    private int correctCount = 0;
    private int incorrectCount = 0;
    private int consecutiveCorrect = 0;
    private LocalDateTime nextReviewDate;
    private int interval = 1; // Days until next review
    private double easeFactor = 2.5; // SM-2 algorithm ease factor
    
    // Study session data
    private LocalDateTime lastReviewed;
    private int lastReviewScore; // 0-5 scale (0=complete blackout, 5=perfect response)
    
    public UserProgress(String userId, String flashcardId, String deckId) {
        this.userId = userId;
        this.flashcardId = flashcardId;
        this.deckId = deckId;
        this.nextReviewDate = LocalDateTime.now();
    }
} 