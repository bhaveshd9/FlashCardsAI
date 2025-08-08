package com.flashcards.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset Request - Flashcards AI");
        message.setText(
            "You have requested a password reset for your Flashcards AI account.\n\n" +
            "Click the following link to reset your password:\n" +
            "http://localhost:8080/reset-password?token=" + resetToken + "\n\n" +
            "If you didn't request this, please ignore this email.\n\n" +
            "This link will expire in 1 hour."
        );
        
        mailSender.send(message);
    }
} 