package com.flashcards.controller;

import com.flashcards.dto.FeedbackRequest;
import com.flashcards.model.Feedback;
import com.flashcards.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<Feedback> submitFeedback(@RequestBody FeedbackRequest request, 
                                                 Authentication authentication) {
        try {
            String userId = authentication.getName();
            Feedback feedback = feedbackService.submitFeedback(userId, request);
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<Feedback>> getMyFeedback(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<Feedback> feedback = feedbackService.getUserFeedback(userId);
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Admin endpoints
    @GetMapping("/all")
    public ResponseEntity<List<Feedback>> getAllFeedback(Authentication authentication) {
        try {
            List<Feedback> feedback = feedbackService.getAllFeedback();
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Feedback>> getFeedbackByStatus(@PathVariable String status, 
                                                            Authentication authentication) {
        try {
            List<Feedback> feedback = feedbackService.getFeedbackByStatus(status);
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{feedbackId}/status")
    public ResponseEntity<Feedback> updateFeedbackStatus(@PathVariable String feedbackId, 
                                                       @RequestParam String status,
                                                       Authentication authentication) {
        try {
            Feedback feedback = feedbackService.updateFeedbackStatus(feedbackId, status);
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 