package com.flashcards.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String userId;
    private String email;
    private String username;
    private String name;
} 