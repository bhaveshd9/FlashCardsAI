package com.flashcards.service;

import com.flashcards.model.User;
import com.flashcards.model.Deck;
import com.flashcards.model.Feedback;
import com.flashcards.repository.UserRepository;
import com.flashcards.repository.DeckRepository;
import com.flashcards.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final FeedbackRepository feedbackRepository;

    public AdminService(UserRepository userRepository, 
                       DeckRepository deckRepository, 
                       FeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.feedbackRepository = feedbackRepository;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // User stats
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.count(); // Simplified - in real app, check last login
        
        // Deck stats
        long totalDecks = deckRepository.count();
        long publicDecks = deckRepository.findByIsPublicTrue().size();
        
        // Feedback stats
        long totalFeedback = feedbackRepository.count();
        long pendingFeedback = feedbackRepository.findByStatus("pending").size();
        
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("totalDecks", totalDecks);
        stats.put("publicDecks", publicDecks);
        stats.put("totalFeedback", totalFeedback);
        stats.put("pendingFeedback", pendingFeedback);
        
        return stats;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUserRole(String userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRole(role);
        return userRepository.save(user);
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    public List<Deck> getAllDecks() {
        return deckRepository.findAll();
    }

    public void deleteDeck(String deckId) {
        deckRepository.deleteById(deckId);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    public Feedback updateFeedbackStatus(String feedbackId, String status) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        
        feedback.setStatus(status);
        feedback.setUpdatedAt(LocalDateTime.now());
        
        return feedbackRepository.save(feedback);
    }

    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Check database connectivity
            long userCount = userRepository.count();
            health.put("database", "healthy");
            health.put("userCount", userCount);
        } catch (Exception e) {
            health.put("database", "unhealthy");
            health.put("error", e.getMessage());
        }
        
        health.put("timestamp", LocalDateTime.now());
        
        return health;
    }
} 