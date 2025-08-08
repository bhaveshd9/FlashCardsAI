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
@Document(collection = "feedback")
public class Feedback {
    
    @Id
    private String id;
    
    private String userId;
    private String subject;
    private String message;
    private String category;
    private int rating;
    private String contactEmail;
    private String status = "pending"; // pending, reviewed, resolved, closed
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Feedback(String userId, String subject, String message, String category, int rating, String contactEmail) {
        this.userId = userId;
        this.subject = subject;
        this.message = message;
        this.category = category;
        this.rating = rating;
        this.contactEmail = contactEmail;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
} 