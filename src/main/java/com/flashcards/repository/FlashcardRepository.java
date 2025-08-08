package com.flashcards.repository;

import com.flashcards.model.Flashcard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardRepository extends MongoRepository<Flashcard, String> {
    List<Flashcard> findByDeckIdOrderByOrderIndex(String deckId);
    List<Flashcard> findByUserId(String userId);
    List<Flashcard> findByTagsContaining(String tag);
    long countByDeckId(String deckId);
    long countByUserId(String userId);
    
    @Query("{'$or': [{'question': {'$regex': ?0, '$options': 'i'}}, {'answer': {'$regex': ?0, '$options': 'i'}}]}")
    List<Flashcard> searchByQuestionOrAnswer(String searchTerm);
} 