package com.flashcards.service;

import com.flashcards.model.UserActivity;
import com.flashcards.repository.UserActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserActivityService {
    
    private final UserActivityRepository userActivityRepository;
    
    public UserActivityService(UserActivityRepository userActivityRepository) {
        this.userActivityRepository = userActivityRepository;
    }
    
    public void logActivity(String userId, String activityType, String description, String relatedId) {
        UserActivity activity = new UserActivity(userId, activityType, description, relatedId);
        userActivityRepository.save(activity);
    }
    
    public List<UserActivity> getUserRecentActivity(String userId, int limit) {
        List<UserActivity> activities = userActivityRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return activities.stream().limit(limit).toList();
    }
    
    public List<UserActivity> getUserActivityByType(String userId, String activityType) {
        return userActivityRepository.findByUserIdAndActivityTypeOrderByCreatedAtDesc(userId, activityType);
    }
} 