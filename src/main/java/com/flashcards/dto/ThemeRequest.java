package com.flashcards.dto;

import lombok.Data;

@Data
public class ThemeRequest {
    private String theme = "light"; // light, dark
    private String primaryColor = "#667eea";
    private String secondaryColor = "#764ba2";
} 