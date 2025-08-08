package com.flashcards.repository;

import com.flashcards.model.Deck;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeckRepository extends MongoRepository<Deck, String> {
    List<Deck> findByUserId(String userId);
    List<Deck> findByIsPublicTrue();
    List<Deck> findByTagsContaining(String tag);
    
    @Query("{'$or': [{'name': {'$regex': ?0, '$options': 'i'}}, {'description': {'$regex': ?0, '$options': 'i'}}]}")
    List<Deck> searchByNameOrDescription(String searchTerm);
} 