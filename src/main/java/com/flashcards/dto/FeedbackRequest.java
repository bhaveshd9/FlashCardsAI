package com.flashcards.dto;

import lombok.Data;

@Data
public class FeedbackRequest {
    private String subject;
    private String message;
    private String category; // bug, feature, general, other
    private int rating; // 1-5 stars
    private String contactEmail; // optional
} 