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
@Document(collection = "password_reset_tokens")
public class PasswordResetToken {
    
    @Id
    private String id;
    
    private String email;
    private String token;
    private LocalDateTime expiryDate;
    private boolean used = false;
    
    public PasswordResetToken(String email, String token) {
        this.email = email;
        this.token = token;
        this.expiryDate = LocalDateTime.now().plusHours(1); // 1 hour expiry
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
} 