package com.flashcards.repository;

import com.flashcards.model.UserPreferences;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferencesRepository extends MongoRepository<UserPreferences, String> {
    Optional<UserPreferences> findByUserId(String userId);
} 