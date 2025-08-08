package com.flashcards.repository;

import com.flashcards.model.UserActivity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActivityRepository extends MongoRepository<UserActivity, String> {
    List<UserActivity> findByUserIdOrderByCreatedAtDesc(String userId);
    List<UserActivity> findByUserIdAndActivityTypeOrderByCreatedAtDesc(String userId, String activityType);
} 