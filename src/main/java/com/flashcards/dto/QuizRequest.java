package com.flashcards.dto;

import lombok.Data;

@Data
public class QuizRequest {
    private String deckId;
    private int numberOfQuestions = 10;
    private String difficulty = "medium"; // easy, medium, hard
} 