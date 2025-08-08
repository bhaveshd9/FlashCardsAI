package com.flashcards.dto;

import lombok.Data;
import java.util.List;

@Data
public class FlashcardRequest {
    private String question;
    private String answer;
    private String front; // Alternative field name for question
    private String back;  // Alternative field name for answer
    private List<String> tags;
    private int orderIndex;
    
    // Getter methods that handle both field names
    public String getQuestion() {
        return question != null ? question : front;
    }
    
    public String getAnswer() {
        return answer != null ? answer : back;
    }
} 