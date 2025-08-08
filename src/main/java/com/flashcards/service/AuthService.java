package com.flashcards.service;

import com.flashcards.dto.AuthRequest;
import com.flashcards.dto.AuthResponse;
import com.flashcards.dto.UserRegistrationRequest;
import com.flashcards.dto.PasswordResetRequest;
import com.flashcards.dto.PasswordResetConfirmRequest;
import com.flashcards.dto.ProfileUpdateRequest;
import com.flashcards.model.User;
import com.flashcards.model.PasswordResetToken;
import com.flashcards.repository.UserRepository;
import com.flashcards.repository.PasswordResetTokenRepository;
import com.flashcards.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, 
                      PasswordResetTokenRepository passwordResetTokenRepository,
                      PasswordEncoder passwordEncoder, 
                      JwtUtil jwtUtil, 
                      AuthenticationManager authenticationManager,
                      EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    public AuthResponse register(UserRegistrationRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        // Create new user
        User user = new User(
            request.getEmail(),
            request.getUsername(),
            passwordEncoder.encode(request.getPassword()),
            request.getName()
        );

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail());

        return new AuthResponse(token, savedUser.getId(), savedUser.getEmail(), 
                              savedUser.getUsername(), savedUser.getName());
    }

    public AuthResponse login(AuthRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Get user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return new AuthResponse(token, user.getId(), user.getEmail(), 
                              user.getUsername(), user.getName());
    }

    public User getCurrentUser(String userIdOrEmail) {
        // Try to find by ID first, then by email
        try {
            return userRepository.findById(userIdOrEmail)
                    .orElseGet(() -> userRepository.findByEmail(userIdOrEmail)
                            .orElseThrow(() -> new RuntimeException("User not found")));
        } catch (IllegalArgumentException e) {
            // If userIdOrEmail is not a valid ObjectId, try email
            return userRepository.findByEmail(userIdOrEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
    }

    public void requestPasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken(request.getEmail(), resetToken);
        passwordResetTokenRepository.save(token);

        // Send email
        emailService.sendPasswordResetEmail(request.getEmail(), resetToken);
    }

    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (resetToken.isExpired()) {
            throw new RuntimeException("Reset token has expired");
        }

        // Update user password
        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    public User updateProfile(String userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if username is already taken by another user
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username already taken");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        return userRepository.save(user);
    }
} 