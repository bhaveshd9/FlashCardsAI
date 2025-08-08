package com.flashcards.dto;

import lombok.Data;

@Data
public class UserRegistrationRequest {
    private String email;
    private String username;
    private String password;
    private String name;
} 