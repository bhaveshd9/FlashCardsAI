package com.flashcards.controller;

import com.flashcards.dto.AuthRequest;
import com.flashcards.dto.AuthResponse;
import com.flashcards.dto.UserRegistrationRequest;
import com.flashcards.dto.PasswordResetRequest;
import com.flashcards.dto.PasswordResetConfirmRequest;
import com.flashcards.dto.ProfileUpdateRequest;
import com.flashcards.service.AuthService;
import com.flashcards.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Backend is working!");
        response.put("timestamp", LocalDateTime.now());
        response.put("mongodb", "connected");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-env")
    public ResponseEntity<Map<String, Object>> testEnvironment() {
        Map<String, Object> response = new HashMap<>();
        
        // Test environment variables
        String mongoUri = System.getenv("MONGODB_URI");
        String jwtSecret = System.getenv("JWT_SECRET");
        String openaiKey = System.getenv("OPENAI_API_KEY");
        
        response.put("status", "environment_test");
        response.put("timestamp", LocalDateTime.now());
        response.put("MONGODB_URI_exists", mongoUri != null && !mongoUri.isEmpty());
        response.put("MONGODB_URI_length", mongoUri != null ? mongoUri.length() : 0);
        response.put("JWT_SECRET_exists", jwtSecret != null && !jwtSecret.isEmpty());
        response.put("OPENAI_API_KEY_exists", openaiKey != null && !openaiKey.isEmpty());
        response.put("mongo_uri_preview", mongoUri != null ? mongoUri.substring(0, Math.min(50, mongoUri.length())) + "..." : "null");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Void> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        try {
            authService.requestPasswordReset(request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@RequestBody PasswordResetConfirmRequest request) {
        try {
            authService.confirmPasswordReset(request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody ProfileUpdateRequest request, 
                                            Authentication authentication) {
        try {
            String userId = authentication.getName();
            User updatedUser = authService.updateProfile(userId, request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        try {
            String userId = authentication.getName();
            User user = authService.getCurrentUser(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 