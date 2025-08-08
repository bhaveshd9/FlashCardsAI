package com.flashcards.repository;

import com.flashcards.model.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends MongoRepository<Feedback, String> {
    List<Feedback> findByUserId(String userId);
    List<Feedback> findByStatus(String status);
    List<Feedback> findByCategory(String category);
} 