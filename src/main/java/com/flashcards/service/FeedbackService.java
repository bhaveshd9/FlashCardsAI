package com.flashcards.service;

import com.flashcards.dto.FeedbackRequest;
import com.flashcards.model.Feedback;
import com.flashcards.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EmailService emailService;

    public FeedbackService(FeedbackRepository feedbackRepository, EmailService emailService) {
        this.feedbackRepository = feedbackRepository;
        this.emailService = emailService;
    }

    public Feedback submitFeedback(String userId, FeedbackRequest request) {
        Feedback feedback = new Feedback(
            userId,
            request.getSubject(),
            request.getMessage(),
            request.getCategory(),
            request.getRating(),
            request.getContactEmail()
        );
        
        Feedback savedFeedback = feedbackRepository.save(feedback);
        
        // Send notification email to admin (if email service is configured)
        try {
            sendFeedbackNotification(savedFeedback);
        } catch (Exception e) {
            // Log error but don't fail the feedback submission
        }
        
        return savedFeedback;
    }

    public List<Feedback> getUserFeedback(String userId) {
        return feedbackRepository.findByUserId(userId);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    public List<Feedback> getFeedbackByStatus(String status) {
        return feedbackRepository.findByStatus(status);
    }

    public List<Feedback> getFeedbackByCategory(String category) {
        return feedbackRepository.findByCategory(category);
    }

    public Feedback updateFeedbackStatus(String feedbackId, String status) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        
        feedback.setStatus(status);
        feedback.setUpdatedAt(LocalDateTime.now());
        
        return feedbackRepository.save(feedback);
    }

    private void sendFeedbackNotification(Feedback feedback) {
        // This would send an email to admin about new feedback
        // For now, we'll just log it
        System.out.println("New feedback received: " + feedback.getSubject() + " from user: " + feedback.getUserId());
    }
} 