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
@Document(collection = "flashcards")
public class Flashcard {
    
    @Id
    private String id;
    
    private String question;
    private String answer;
    private String deckId;
    private String userId; // Creator of the card
    private List<String> tags;
    private int orderIndex; // Position in deck
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // AI-generated metadata
    private String aiGenerated = "false";
    private String sourceText; // Original text used to generate this card
    
    // Study data
    private int correctCount = 0;
    private int incorrectCount = 0;
    private LocalDateTime lastReviewed;
    private int difficultyLevel = 1; // 1-5 scale
    
    public Flashcard(String question, String answer, String deckId, String userId) {
        this.question = question;
        this.answer = answer;
        this.deckId = deckId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter methods for frontend compatibility
    public String getFront() {
        return question;
    }
    
    public String getBack() {
        return answer;
    }
} 