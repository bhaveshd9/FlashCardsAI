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
@Document(collection = "user_activities")
public class UserActivity {
    
    @Id
    private String id;
    
    private String userId;
    private String activityType; // "deck_created", "flashcard_added", "study_session", "quiz_completed", "feedback_submitted"
    private String description;
    private String relatedId; // ID of related entity (deck, flashcard, etc.)
    private LocalDateTime createdAt;
    
    public UserActivity(String userId, String activityType, String description, String relatedId) {
        this.userId = userId;
        this.activityType = activityType;
        this.description = description;
        this.relatedId = relatedId;
        this.createdAt = LocalDateTime.now();
    }
} 