package com.flashcards.controller;

import com.flashcards.dto.ThemeRequest;
import com.flashcards.model.UserPreferences;
import com.flashcards.service.UserPreferencesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
@CrossOrigin(origins = "*")
public class UserPreferencesController {

    private final UserPreferencesService userPreferencesService;

    public UserPreferencesController(UserPreferencesService userPreferencesService) {
        this.userPreferencesService = userPreferencesService;
    }

    @GetMapping
    public ResponseEntity<UserPreferences> getUserPreferences(Authentication authentication) {
        try {
            String userId = authentication.getName();
            UserPreferences preferences = userPreferencesService.getUserPreferences(userId);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/theme")
    public ResponseEntity<UserPreferences> updateTheme(@RequestBody ThemeRequest request, 
                                                     Authentication authentication) {
        try {
            String userId = authentication.getName();
            UserPreferences preferences = userPreferencesService.updateTheme(userId, request);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping
    public ResponseEntity<UserPreferences> updatePreferences(@RequestBody UserPreferences preferences, 
                                                           Authentication authentication) {
        try {
            String userId = authentication.getName();
            UserPreferences updatedPreferences = userPreferencesService.updatePreferences(userId, preferences);
            return ResponseEntity.ok(updatedPreferences);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 