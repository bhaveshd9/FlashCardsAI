package com.flashcards.service;

import com.flashcards.dto.ThemeRequest;
import com.flashcards.model.UserPreferences;
import com.flashcards.repository.UserPreferencesRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserPreferencesService {

    private final UserPreferencesRepository userPreferencesRepository;

    public UserPreferencesService(UserPreferencesRepository userPreferencesRepository) {
        this.userPreferencesRepository = userPreferencesRepository;
    }

    public UserPreferences getUserPreferences(String userId) {
        return userPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserPreferences preferences = new UserPreferences(userId);
                    return userPreferencesRepository.save(preferences);
                });
    }

    public UserPreferences updateTheme(String userId, ThemeRequest request) {
        UserPreferences preferences = getUserPreferences(userId);
        
        if (request.getTheme() != null) {
            preferences.setTheme(request.getTheme());
        }
        if (request.getPrimaryColor() != null) {
            preferences.setPrimaryColor(request.getPrimaryColor());
        }
        if (request.getSecondaryColor() != null) {
            preferences.setSecondaryColor(request.getSecondaryColor());
        }
        
        preferences.setUpdatedAt(LocalDateTime.now());
        return userPreferencesRepository.save(preferences);
    }

    public UserPreferences updatePreferences(String userId, UserPreferences preferences) {
        UserPreferences existing = getUserPreferences(userId);
        
        existing.setTheme(preferences.getTheme());
        existing.setPrimaryColor(preferences.getPrimaryColor());
        existing.setSecondaryColor(preferences.getSecondaryColor());
        existing.setEmailNotifications(preferences.isEmailNotifications());
        existing.setStudyReminders(preferences.isStudyReminders());
        existing.setLanguage(preferences.getLanguage());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return userPreferencesRepository.save(existing);
    }
} 