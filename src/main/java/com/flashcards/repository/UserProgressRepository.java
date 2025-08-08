package com.flashcards.repository;

import com.flashcards.model.UserProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends MongoRepository<UserProgress, String> {
    List<UserProgress> findByUserId(String userId);
    List<UserProgress> findByUserIdAndDeckId(String userId, String deckId);
    Optional<UserProgress> findByUserIdAndFlashcardId(String userId, String flashcardId);
    
    @Query("{'userId': ?0, 'nextReviewDate': {'$lte': ?1}}")
    List<UserProgress> findDueCards(String userId, LocalDateTime now);
} 