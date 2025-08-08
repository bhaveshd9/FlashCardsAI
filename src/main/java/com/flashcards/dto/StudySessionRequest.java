package com.flashcards.dto;

import lombok.Data;

@Data
public class StudySessionRequest {
    private String flashcardId;
    private String deckId;
    private int score; // 0-5 scale
    private String studyMode; // flip, quiz, spaced
} 