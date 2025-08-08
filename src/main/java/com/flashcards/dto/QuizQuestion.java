package com.flashcards.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion {
    private String id;
    private String question;
    private List<String> options;
    private int correctAnswerIndex;
    private String explanation;
} 