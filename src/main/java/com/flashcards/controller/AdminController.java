package com.flashcards.controller;

import com.flashcards.model.User;
import com.flashcards.model.Deck;
import com.flashcards.model.Feedback;
import com.flashcards.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication authentication) {
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(Authentication authentication) {
        try {
            List<User> users = adminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable String userId, 
                                             @RequestParam String role,
                                             Authentication authentication) {
        try {
            User user = adminService.updateUserRole(userId, role);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId, 
                                         Authentication authentication) {
        try {
            adminService.deleteUser(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/decks")
    public ResponseEntity<List<Deck>> getAllDecks(Authentication authentication) {
        try {
            List<Deck> decks = adminService.getAllDecks();
            return ResponseEntity.ok(decks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/decks/{deckId}")
    public ResponseEntity<Void> deleteDeck(@PathVariable String deckId, 
                                         Authentication authentication) {
        try {
            adminService.deleteDeck(deckId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/feedback")
    public ResponseEntity<List<Feedback>> getAllFeedback(Authentication authentication) {
        try {
            List<Feedback> feedback = adminService.getAllFeedback();
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/feedback/{feedbackId}/status")
    public ResponseEntity<Feedback> updateFeedbackStatus(@PathVariable String feedbackId, 
                                                       @RequestParam String status,
                                                       Authentication authentication) {
        try {
            Feedback feedback = adminService.updateFeedbackStatus(feedbackId, status);
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth(Authentication authentication) {
        try {
            Map<String, Object> health = adminService.getSystemHealth();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 