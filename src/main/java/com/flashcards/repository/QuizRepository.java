package com.flashcards.repository;

import com.flashcards.model.Quiz;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {
    List<Quiz> findByUserIdOrderByStartedAtDesc(String userId);
    List<Quiz> findByDeckIdAndUserIdOrderByStartedAtDesc(String deckId, String userId);
    List<Quiz> findByUserIdAndStatusOrderByStartedAtDesc(String userId, String status);
} 