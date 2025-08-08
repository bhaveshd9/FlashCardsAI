package com.flashcards.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String email;
    
    private String username;
    private String password;
    private String name;
    private String avatarUrl;
    private String role = "USER"; // USER, ADMIN
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private boolean enabled = true;
    
    // Study statistics
    private int totalCardsReviewed = 0;
    private int dailyStreak = 0;
    private LocalDateTime lastStudyDate;
    
    public User(String email, String username, String password, String name) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }
} 