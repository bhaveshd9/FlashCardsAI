package com.flashcards.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String name;
    private String username;
    private String avatarUrl;
} 