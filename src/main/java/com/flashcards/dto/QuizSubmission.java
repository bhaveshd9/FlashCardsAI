package com.flashcards.dto;

import lombok.Data;
import java.util.Map;

@Data
public class QuizSubmission {
    private String deckId;
    private Map<String, Integer> answers; // questionId -> selectedOptionIndex
} 