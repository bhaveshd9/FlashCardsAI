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
@Document(collection = "user_preferences")
public class UserPreferences {
    
    @Id
    private String id;
    
    private String userId;
    private String theme = "light"; // light, dark
    private String primaryColor = "#667eea";
    private String secondaryColor = "#764ba2";
    private boolean emailNotifications = true;
    private boolean studyReminders = true;
    private String language = "en";
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public UserPreferences(String userId) {
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
} 