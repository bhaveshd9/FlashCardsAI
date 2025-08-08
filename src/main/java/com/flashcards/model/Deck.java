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
@Document(collection = "decks")
public class Deck {
    
    @Id
    private String id;
    
    private String name;
    private String description;
    private String userId; // Owner of the deck
    private boolean isPublic = false;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int cardCount = 0;
    private int flashcardCount = 0; // Number of flashcards in this deck
    private String coverImageUrl;
    
    // Study statistics
    private int totalViews = 0;
    private int totalCopies = 0;
    
    public Deck(String name, String description, String userId) {
        this.name = name;
        this.description = description;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
} 